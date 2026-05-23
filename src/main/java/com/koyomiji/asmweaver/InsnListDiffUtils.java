package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.heuristic.Heuristic;
import com.koyomiji.asmweaver.heuristic.MyersFuzzyDistanceHeuristic;
import com.koyomiji.asmweaver.io.CustomDataInput;
import com.koyomiji.asmweaver.io.CustomDataOutput;
import com.koyomiji.asmweaver.util.BiPersistentHashMap;
import com.koyomiji.asmweaver.util.PeekableIterator;
import com.koyomiji.asmweaver.util.PersistentHashMap;
import com.koyomiji.asmweaver.util.UnionFind;
import com.koyomiji.asmweaver.util.tuple.Pair;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class InsnListDiffUtils {
  public static InsnListDiff invert(InsnListDiff diff) {
    List<InsnListDiff.Operation> invertedOperations = new ArrayList<>();

    for (InsnListDiff.Operation op : diff.operations) {
      InsnListDiff.Operation invertedOp;

      switch (op.type) {
        case MATCH:
          invertedOp = new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, InsnListDiff.Operation.Mode.BETWEEN, op.operand2, op.operand1);
          break;
        case INSERT:
          invertedOp = new InsnListDiff.Operation(InsnListDiff.Operation.Type.DELETE, op.mode, op.operand2, op.operand1);
          break;
        case DELETE:
          invertedOp = new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT, op.mode, op.operand2, op.operand1);
          break;
        default:
          throw new IllegalStateException("Unexpected operation type: " + op.type);
      }

      invertedOperations.add(invertedOp);
    }

    return new InsnListDiff(invertedOperations);
  }

  /**
   * Attempts to commute p and q. If successful, returns a pair of diffs (q', p') such that applying q' then p' yields the same result as applying p then q.
   *
   * @param p
   * @param q
   * @throws ConflictException
   */
  public static Pair<InsnListDiff, InsnListDiff> commute(InsnListDiff p, InsnListDiff q) throws ConflictException {
    Pair<InsnListDiff, InsnListDiff> normalized = normalizeLabels(p, q);
    p = normalized.first;
    q = normalized.second;

    List<InsnListDiff.Operation> qPrimeOps = new ArrayList<>();
    List<InsnListDiff.Operation> pPrimeOps = new ArrayList<>();

    Set<AbstractInsnNode> pInserted = collectInserted(p);

    // 新しい同時イテレータを適用
    InsnListDiffPairIterator it = new InsnListDiffPairIterator(p.operations.iterator(), q.operations.iterator());

    while (it.hasNext()) {
      Pair<InsnListDiff.Operation, InsnListDiff.Operation> pair = it.next();
      InsnListDiff.Operation opP = pair.first;
      InsnListDiff.Operation opQ = pair.second;

      // パターン1: P が単独で DELETE（Q側は進めない）
      if (opP != null && opQ == null) {
        qPrimeOps.add(new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, opP.mode, opP.operand1, opP.operand1));
        pPrimeOps.add(new InsnListDiff.Operation(InsnListDiff.Operation.Type.DELETE, opP.mode, opP.operand1, null));
      }
      // パターン2: Q が単独で INSERT（P側は進めない）
      else if (opP == null && opQ != null) {
        // FIXME: verifyNoInternalDependency(opQ.operand, pInsertedNodes);
        qPrimeOps.add(opQ);
        pPrimeOps.add(new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, opQ.mode, opQ.operand2, opQ.operand2));
      }
      // パターン3: 両方のタイムラインが揃った（MATCH/INSERT vs MATCH/DELETE）
      else if (opP != null && opQ != null) {
        if (!AbstractInsnNodeHelper.equals(opP.operand2, opQ.operand1)) {
          throw new IllegalDiffException("p and q disagree on node identity");
        }

        if (opP.type == InsnListDiff.Operation.Type.MATCH) {
          qPrimeOps.add(opQ);
          if (opQ.type == InsnListDiff.Operation.Type.MATCH) {
            pPrimeOps.add(new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, opP.mode, opP.operand1, opP.operand1));
          }
        } else { // opP.type == INSERT
          if (opQ.type == InsnListDiff.Operation.Type.DELETE) {
            throw new ConflictException("p inserts a node that q deletes");
          }
          pPrimeOps.add(new InsnListDiff.Operation(opP.type, opP.mode, null, opP.operand2));
        }
      }
    }

    return new Pair<>(new InsnListDiff(qPrimeOps), new InsnListDiff(pPrimeOps));
  }

  private static boolean isInsert(InsnListDiff.Operation op) {
    return op.type == InsnListDiff.Operation.Type.INSERT;
  }

  private static Set<AbstractInsnNode> collectInserted(InsnListDiff diff) {
    Set<AbstractInsnNode> inserted = new HashSet<>();

    for (InsnListDiff.Operation op : diff.operations) {
      if (isInsert(op)) {
        inserted.add(op.operand2);
      }
    }

    return inserted;
  }

  private static List<InsnListDiff.Operation> mergeInsertionSlot(
          List<InsnListDiff.Operation> ins1,
          List<InsnListDiff.Operation> ins2) throws ConflictException {

    List<InsnListDiff.Operation> result = new ArrayList<>();
    result.addAll(ins1);
    result.addAll(ins2);
    return result;
  }

  private static List<InsnListDiff.Operation> collectInsertions(PeekableIterator<InsnListDiff.Operation> it) {
    List<InsnListDiff.Operation> insertions = new ArrayList<>();

    while (it.hasNext() && isInsert(it.peek())) {
      insertions.add(it.next());
    }

    return insertions;
  }

  public static InsnListDiff compose(InsnListDiff p, InsnListDiff q) throws ConflictException {
    List<InsnListDiff.Operation> result = new ArrayList<>();

    PeekableIterator<InsnListDiff.Operation> itP = new PeekableIterator<>(p.operations.iterator());
    PeekableIterator<InsnListDiff.Operation> itQ = new PeekableIterator<>(q.operations.iterator());

    List<InsnListDiff.Operation> ins1 = new ArrayList<>();
    List<InsnListDiff.Operation> ins2 = new ArrayList<>();

    while (itP.hasNext()) {
      InsnListDiff.Operation opP = itP.next();

      if (opP.type == InsnListDiff.Operation.Type.INSERT) {
        ins2.addAll(collectInsertions(itQ));

        InsnListDiff.Operation opQ = IteratorHelper.nextOrThrow(itQ, () -> new IllegalDiffException("Composition Error: q is shorter than intermediate B."));

        if (!AbstractInsnNodeHelper.equals(opP.operand2, opQ.operand1)) {
          throw new IllegalDiffException("Composition Error: Operand mismatch at B.");
        }

        if (opQ.type == InsnListDiff.Operation.Type.MATCH) {
          ins1.add(opP);
        }
      } else if (opP.type == InsnListDiff.Operation.Type.DELETE) {
        result.addAll(mergeInsertionSlot(ins1, ins2));
        ins1.clear();
        ins2.clear();
        result.add(opP);
      } else { // MATCH
        ins2.addAll(collectInsertions(itQ));

        InsnListDiff.Operation opQ = IteratorHelper.nextOrThrow(itQ, () -> new IllegalDiffException("Composition Error: q is shorter than intermediate B."));

        if (!AbstractInsnNodeHelper.equals(opP.operand2, opQ.operand1)) {
          throw new IllegalDiffException("Composition Error: Operand mismatch at C.");
        }

        result.addAll(mergeInsertionSlot(ins1, ins2));
        ins1.clear();
        ins2.clear();

        result.add(new InsnListDiff.Operation(opQ.type, opQ.mode, opP.operand1, opQ.operand2));
      }
    }

    ins2.addAll(collectInsertions(itQ));
    result.addAll(mergeInsertionSlot(ins1, ins2));

    IteratorHelper.throwIfNext(itQ, () -> new IllegalDiffException("Composition Error: q has remaining operations after p is exhausted."));

    return new InsnListDiff(result);
  }

  public static Map<LabelNode, LabelNode> extractLabelMap(List<AbstractInsnNode> list1, List<AbstractInsnNode> list2, InsnListDiff diff) {
    int i = 0, j = 0;
    Map<LabelNode, LabelNode> labelMap = new HashMap<>();

    for (InsnListDiff.Operation op : diff.operations) {
      switch (op.type) {
        case MATCH:
          List<LabelNode> labels1 = AbstractInsnNodeHelper.getLabelTargets(list1.get(i));
          List<LabelNode> labels2 = AbstractInsnNodeHelper.getLabelTargets(list2.get(j));
          for (int k = 0; k < Math.min(labels1.size(), labels2.size()); k++) {
            labelMap.put(labels1.get(k), labels2.get(k));
          }
          i++;
          j++;
          break;
        case DELETE:
          i++;
          break;
        case INSERT:
          j++;
          break;
      }
    }

    return labelMap;
  }

  public static InsnListDiff.Operation mapLabels(InsnListDiff.Operation op, Function<LabelNode, LabelNode> labelMap) {
    return new InsnListDiff.Operation(
            op.type,
            op.mode,
            AbstractInsnNodeHelper.mapLabelTargets(op.operand1, labelMap),
            AbstractInsnNodeHelper.mapLabelTargets(op.operand2, labelMap)
    );
  }

  private static void unionLabels(UnionFind<LabelNode> uf, AbstractInsnNode node1, AbstractInsnNode node2) {
    List<LabelNode> labels1 = AbstractInsnNodeHelper.getLabelTargets(node1);
    List<LabelNode> labels2 = AbstractInsnNodeHelper.getLabelTargets(node2);

    if (labels1.size() != labels2.size()) {
      throw new IllegalDiffException("Cannot normalize labels: Mismatched number of label targets.");
    }

    for (int k = 0; k < labels1.size(); k++) {
      uf.union(labels1.get(k), labels2.get(k));
    }
  }

  public static Pair<InsnListDiff, InsnListDiff> normalizeLabels(InsnListDiff diff1, InsnListDiff diff2) {
    UnionFind<LabelNode> uf = new UnionFind<>();

    InsnListDiffPairIterator it = new InsnListDiffPairIterator(diff1.operations.iterator(), diff2.operations.iterator());

    while (it.hasNext()) {
      Pair<InsnListDiff.Operation, InsnListDiff.Operation> pair = it.next();
      InsnListDiff.Operation op1 = pair.first;
      InsnListDiff.Operation op2 = pair.second;

      if (op1 != null && op2 != null) {
        AbstractInsnNode nodeJFromDiff1 = op1.operand2;
        AbstractInsnNode nodeJFromDiff2 = op2.operand1;

        if (op1.type == InsnListDiff.Operation.Type.MATCH) {
          unionLabels(uf, op1.operand1, op1.operand2);
        }

        unionLabels(uf, nodeJFromDiff1, nodeJFromDiff2);

        if (op2.type == InsnListDiff.Operation.Type.MATCH) {
          unionLabels(uf, op2.operand1, op2.operand2);
        }
      }
    }

    List<InsnListDiff.Operation> normalizedOps1 = new ArrayList<>();
    for (InsnListDiff.Operation op : diff1.operations) {
      normalizedOps1.add(mapLabels(op, uf::find));
    }

    List<InsnListDiff.Operation> normalizedOps2 = new ArrayList<>();
    for (InsnListDiff.Operation op : diff2.operations) {
      normalizedOps2.add(mapLabels(op, uf::find));
    }

    return Pair.of(new InsnListDiff(normalizedOps1), new InsnListDiff(normalizedOps2));
  }

  public static class State implements Comparable<State> {
    final int idxA;
    final int idxB;
    final int g;
    final int h; // Heuristic

    // Mapping from labels in A to labels in B
//    BiHashMap<LabelNode, LabelNode> labelMap;
    BiPersistentHashMap<LabelNode, LabelNode> labelMap;
    PersistentHashMap<Integer, Integer> duAToB;
    PersistentHashMap<Integer, Integer> duBToA;

    //    final List<InsnListDiff.Operation> operations;
    final State previous;
    final InsnListDiff.Operation operation;

    public State(int idxA, int idxB, int g, int h,
                 BiPersistentHashMap<LabelNode, LabelNode> labelMap,
                 PersistentHashMap<Integer, Integer> duAToB, PersistentHashMap<Integer, Integer> duBToA,
                 State previous, InsnListDiff.Operation operation) {
      this.idxA = idxA;
      this.idxB = idxB;
      this.g = g;
      this.h = h;
      this.labelMap = labelMap;
      this.duAToB = duAToB;
      this.duBToA = duBToA;
      this.previous = previous;
      this.operation = operation;
    }

    public static State create(int idxA, int idxB, BiPersistentHashMap<LabelNode, LabelNode> labelMap,
                               PersistentHashMap<Integer, Integer> duAToB, PersistentHashMap<Integer, Integer> duBToA,
                               State previous, InsnListDiff.Operation operation, Heuristic heuristicProvider) {
      int h = heuristicProvider.calculate(idxA, idxB, labelMap);
//      int g = previous.g() + (operation.type == InsnListDiff.Operation.Type.MATCH ? 0 : 1);

      int g;

      if (previous == null) {
        g = 0;
      } else {
        g = previous.g() + (operation.type == InsnListDiff.Operation.Type.MATCH ? 0 : 1);
      }

//      for (InsnListDiff.Operation op : operations) {
//        if (op.type == InsnListDiff.Operation.Type.INSERT || op.type == InsnListDiff.Operation.Type.DELETE) {
//          g++;
//        }
//      }

//      return new State(idxA, idxB, h, labelMap, operations);
//      return new State(idxA, idxB, g, h, labelMap, operations);
      return new State(idxA, idxB, g, h, labelMap, duAToB, duBToA, previous, operation);
    }

    public int g() {
      return g;
    }

    public int f() {
      return g() + h;
    }

    public int getDistance() {
      return g();
    }

//    public List<InsnListDiff.Operation> getOperations() {
//      return operations;
//    }

    @Override
    public int compareTo(State o) {
      return Integer.compare(this.f(), o.f());
    }
  }

  public static class StateKey {
    public final int idxA;
    public final int idxB;
    public final PersistentHashMap<LabelNode, LabelNode> aToB;
    public final PersistentHashMap<Integer, Integer> duAToB;
    public final PersistentHashMap<Integer, Integer> duBToA;

    public StateKey(int idxA, int idxB, PersistentHashMap<LabelNode, LabelNode> aToB, PersistentHashMap<Integer, Integer> duAToB, PersistentHashMap<Integer, Integer> duBToA) {
      this.idxA = idxA;
      this.idxB = idxB;
      this.aToB = aToB;
      this.duAToB = duAToB;
      this.duBToA = duBToA;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      StateKey stateKey = (StateKey) o;
      return idxA == stateKey.idxA && idxB == stateKey.idxB && Objects.equals(aToB, stateKey.aToB) && Objects.equals(duAToB, stateKey.duAToB) && Objects.equals(duBToA, stateKey.duBToA);
    }

    @Override
    public int hashCode() {
      return Objects.hash(idxA, idxB, aToB, duAToB, duBToA);
    }
  }

  private static Map<LabelNode, Integer> lastLabelOccurrenceMap(AbstractInsnNode[] arr) {
    Map<LabelNode, Integer> map = new HashMap<>();

//    for (AbstractInsnNode insn : arr) {
//      var labels = getLabelTargets(insn);
//
//      for (var label : labels) {
//        map.put(label, insn);
//      }
//    }

    for (int i = 0; i < arr.length; i++) {
      var insn = arr[i];
      var labels = AbstractInsnNodeHelper.getLabelTargets(insn);

      for (var label : labels) {
        map.put(label, i);
      }
    }

    return map;
  }

  private static void removeDeadLabels(
          State state,
          Map<LabelNode, Integer> lastOccurrenceMapA,
          Map<LabelNode, Integer> lastOccurrenceMapB,
          List<AbstractInsnNode> insnsA,
          List<AbstractInsnNode> insnsB
  ) {
    var currentLabelMap = state.labelMap;

    // --- Process A ---
    int prevIdxA = state.idxA - 1;

    if (prevIdxA >= 0) {
      var prevA = insnsA.get(prevIdxA);
      var labels = AbstractInsnNodeHelper.getLabelTargets(prevA);
      for (var label : labels) {
        if (lastOccurrenceMapA.get(label) == prevIdxA) {
          var mappedB = currentLabelMap.get(label);
          if (mappedB != null) {
            int lastOccIdxB = lastOccurrenceMapB.get(mappedB);
            if (lastOccIdxB < state.idxB) {
              currentLabelMap = currentLabelMap.remove(label);
            }
          }
        }
      }
    }

    // --- Process B ---
    int prevIdxB = state.idxB - 1;
    if (prevIdxB >= 0) {
      var prevB = insnsB.get(prevIdxB);
      var labels = AbstractInsnNodeHelper.getLabelTargets(prevB);
      for (var label : labels) {
        if (lastOccurrenceMapB.get(label) == prevIdxB) {
          var mappedA = currentLabelMap.getKey(label);
          if (mappedA != null) {
            int lastOccIdxA = lastOccurrenceMapA.get(mappedA);
            if (lastOccIdxA < state.idxA) {
              currentLabelMap = currentLabelMap.remove(mappedA);
            }
          }
        }
      }
    }

    state.labelMap = currentLabelMap;
  }

  public static InsnListDiff diff(List<AbstractInsnNode> listA, Function<AbstractInsnNode, Integer> duChainsA, List<AbstractInsnNode> listB, Function<AbstractInsnNode, Integer> duChainsB) {
    AbstractInsnNode[] insnsA = listA.toArray(new AbstractInsnNode[0]);
    AbstractInsnNode[] insnsB = listB.toArray(new AbstractInsnNode[0]);

    var heuristicProvider = new MyersFuzzyDistanceHeuristic(listA, listB);

    PriorityQueue<State> pq = new PriorityQueue<>();
    Map<StateKey, Integer> visited = new HashMap<>();

    var lastOccurrenceA = lastLabelOccurrenceMap(insnsA);
    var lastOccurrenceB = lastLabelOccurrenceMap(insnsB);

    // Initial state
    {
      var nextRealIdxA = 0;
      var nextRealIdxB = 0;

      tryAdd(pq, visited,
              State.create(
                      nextRealIdxA, nextRealIdxB,
                      new BiPersistentHashMap<>(),
                      new PersistentHashMap<>(), new PersistentHashMap<>(),
                      null,
                      null,
                      heuristicProvider
              )
      );
    }

    int j = 0;

    while (!pq.isEmpty()) {
      State current = pq.poll();

      if (current.idxA >= insnsA.length && current.idxB >= insnsB.length) {
//        return new InsnListDiff(current.operations);
        // Backtrack to construct the diff

        List<InsnListDiff.Operation> operations = new ArrayList<>();

        for (State s = current; s.previous != null; s = s.previous) {
          operations.add(s.operation);
        }

        Collections.reverse(operations);
        return new InsnListDiff(operations);
      }

      var currentKey = new StateKey(current.idxA, current.idxB, current.labelMap.forwardMap(), current.duAToB, current.duBToA);
      if (visited.containsKey(currentKey) && visited.get(currentKey) < current.g()) {
        continue;
      }

      if (1 == 0) {
//        Logger.getInstance().log(
        System.out.println(
                String.format("State: idxA=%d, idxB=%d, g=%d, h=%d, f=%d",
                        current.idxA, current.idxB,
                        current.g(), current.h, current.f()
//                        current.operations.size()
                )
        );
        System.out.println(
                String.format("  aToB mappings: %d", current.labelMap.size())
        );
        System.out.println(
                String.format("  queue size: %d, visited size: %d",
                        pq.size(), visited.size()
                )
        );
        System.out.println(
                String.format("  iteration: %d", j)
        );
      }

      // Transition 1: Delete (consumes A)
      if (current.idxA < insnsA.length) {
        var nextRealIdxA = current.idxA + 1;

        var state = State.create(
                nextRealIdxA, current.idxB,
                current.labelMap,
                current.duAToB, current.duBToA,
//                concat(
//                        current.operations,
//                        new InsnListDiff.Operation(InsnListDiff.Operation.Type.DELETE, InsnListDiff.Operation.Mode.BETWEEN, insnsA[current.idxA])
//                ),
                current,
                new InsnListDiff.Operation(InsnListDiff.Operation.Type.DELETE, InsnListDiff.Operation.Mode.BETWEEN, insnsA[current.idxA], null),
                heuristicProvider
        );
        removeDeadLabels(state, lastOccurrenceA, lastOccurrenceB, listA, listB);
        tryAdd(pq, visited, state);
      }

      // Transition 2: Insert (consumes B)
      if (current.idxB < insnsB.length) {
        var nextRealIdxB = current.idxB + 1;

        var state = State.create(
                current.idxA, nextRealIdxB,
                current.labelMap,
                current.duAToB, current.duBToA,
//                concat(
//                        current.operations,
//                        new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT, InsnListDiff.Operation.Mode.BETWEEN, insnsB[current.idxB])
//
//                ),
                current,
                new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT, InsnListDiff.Operation.Mode.BETWEEN, null, insnsB[current.idxB]),
                heuristicProvider
        );
        removeDeadLabels(state, lastOccurrenceA, lastOccurrenceB, listA, listB);
        tryAdd(pq, visited, state);
      }

      // Transition 3: Match (consumes both A and B)
      match:
      if (current.idxA < insnsA.length && current.idxB < insnsB.length) {
        AbstractInsnNode insnA = insnsA[current.idxA];
        AbstractInsnNode insnB = insnsB[current.idxB];

        List<LabelNode> targetsA = AbstractInsnNodeHelper.getLabelTargets(insnA);
        List<LabelNode> targetsB = AbstractInsnNodeHelper.getLabelTargets(insnB);

        boolean contentMatch = AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(insnA, insnB);

        // Having different number of target labels implies mismatch
        if (targetsA.size() != targetsB.size()) {
          contentMatch = false;
        }

        var nextRealIdxA = current.idxA + 1;
        var nextRealIdxB = current.idxB + 1;

        if (contentMatch) {
//          BiHashMap<LabelNode, LabelNode> newAToB = targetsA.size() > 0 ? new BiHashMap<>(current.labelMap) : current.labelMap;
          BiPersistentHashMap<LabelNode, LabelNode> newAToB = current.labelMap;

          for (int i = 0; i < targetsA.size(); i++) {
            if (!newAToB.canPut(targetsA.get(i), targetsB.get(i))) {
              break match;
            }

//            newAToB.put(targetsA.get(i), targetsB.get(i));
            newAToB = newAToB.put(targetsA.get(i), targetsB.get(i));
          }

//          HashMap<Integer, Integer> newDuAToB = new HashMap<>(current.duAToB);
//          HashMap<Integer, Integer> newDuBToA = new HashMap<>(current.duBToA);
          PersistentHashMap<Integer, Integer> newDuAToB = current.duAToB;
          PersistentHashMap<Integer, Integer> newDuBToA = current.duBToA;

          // fixme: iinc
          /* Disabled*/
          /*
          if (insnA instanceof VarInsnNode && insnB instanceof VarInsnNode) {
            int varA = ((VarInsnNode) insnA).var;
            int varB = ((VarInsnNode) insnB).var;
            Integer duChainA = duChainsA.apply(insnA);

            if (duChainA == null) {
              duChainA = -1;
            }

            Integer duChainB = duChainsB.apply(insnB);

            if (duChainB == null) {
              duChainB = -1;
            }

            if (newDuAToB.containsKey(duChainA) && newDuAToB.get(duChainA) != varB) {
              break match;
            }

            if (newDuBToA.containsKey(duChainB) && newDuBToA.get(duChainB) != varA) {
              break match;
            }

            newDuAToB = newDuAToB.put(duChainA, varB);
            newDuBToA = newDuBToA.put(duChainB, varA);
          }
           */

          var state = State.create(
                  nextRealIdxA, nextRealIdxB,
                  newAToB,
                  newDuAToB, newDuBToA,
//                  current.operations,
//                  concat(
//                          current.operations,
//                          new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, null, insnA)
//                  ),
                  current,
                  new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, InsnListDiff.Operation.Mode.BETWEEN, insnA, insnB),
                  heuristicProvider
          );
          removeDeadLabels(state, lastOccurrenceA, lastOccurrenceB, listA, listB);
          tryAdd(pq, visited, state);
        }
      }
    }
    throw new IllegalStateException("Unreachable");
  }

  private static void tryAdd(PriorityQueue<State> pq, Map<StateKey, Integer> visited, State newState) {
    var newStateKey = new StateKey(newState.idxA, newState.idxB, newState.labelMap.forwardMap(), newState.duAToB, newState.duBToA);

    if (visited.containsKey(newStateKey)) {
      int prevG = visited.get(newStateKey);
      if (prevG <= newState.g()) {
        return;
      }
    }

    visited.put(newStateKey, newState.g());
    pq.add(newState);
  }

  public static List<AbstractInsnNode> patch(List<AbstractInsnNode> insns, InsnListDiff diff, Map<LabelNode, LabelNode> labelMap) {

    // labels in diff to labels in insns
    // For labels that match at least once, keep the original label.
    // Otherwise, create a new label.
    List<AbstractInsnNode> patched = new ArrayList<>();
    int i = 0;
    int j = 0;

    for (InsnListDiff.Operation op : diff.operations) {
      switch (op.type) {
        case MATCH:
          List<LabelNode> extracted1 = AbstractInsnNodeHelper.getLabelTargets(insns.get(i));
          List<LabelNode> extracted2 = AbstractInsnNodeHelper.getLabelTargets(op.operand2);

          for (int k = 0; k < extracted1.size(); k++) {
            labelMap.put(extracted2.get(k), extracted1.get(k));
          }

          i++;
          j++;
          break;
        case INSERT:
          j++;
          break;
        case DELETE:
          i++;
          break;
      }
    }

    i = j = 0;

    for (InsnListDiff.Operation op : diff.operations) {
      switch (op.type) {
        case MATCH:
          patched.add(insns.get(i));
          i++;
          j++;
          break;
        case INSERT:
          List<LabelNode> extracted2 = AbstractInsnNodeHelper.getLabelTargets(op.operand2);

          for (int k = 0; k < extracted2.size(); k++) {
            labelMap.putIfAbsent(extracted2.get(k), new LabelNode());
          }

          patched.add(op.operand2.clone(labelMap));
          j++;
          break;
        case DELETE:
          i++;
          break;
      }
    }

    return patched;
  }

  public static void write(InsnListDiff diff, CustomDataOutput out, Function<LabelNode, Integer> labelToIndex) throws IOException {
    out.writeInt(diff.operations.size());

    for (InsnListDiff.Operation op : diff.operations) {
      out.writeByte(op.type.ordinal());
      out.writeByte(op.mode.ordinal());

      if (op.operand1 != null) {
        NullableHelper.write(op.operand1, out, (o, outStream) -> AbstractInsnNodeHelper.write(o, outStream, labelToIndex));
      }

      if (op.operand2 != null) {
        NullableHelper.write(op.operand2, out, (o, outStream) -> AbstractInsnNodeHelper.write(o, outStream, labelToIndex));
      }
    }
  }

  public static InsnListDiff read(CustomDataInput in, Function<Integer, LabelNode> indexToLabel) throws IOException {
    List<InsnListDiff.Operation> operations = new ArrayList<>();
    int size = in.readInt();

    for (int i = 0; i < size; i++) {
      InsnListDiff.Operation.Type type = InsnListDiff.Operation.Type.values()[in.readByte()];
      InsnListDiff.Operation.Mode mode = InsnListDiff.Operation.Mode.values()[in.readByte()];
      AbstractInsnNode operand1 = NullableHelper.read(in, (inStream) -> AbstractInsnNodeHelper.read(inStream, indexToLabel));
      AbstractInsnNode operand2 = NullableHelper.read(in, (inStream) -> AbstractInsnNodeHelper.read(inStream, indexToLabel));
      operations.add(new InsnListDiff.Operation(type, mode, operand1, operand2));
    }

    return new InsnListDiff(operations);
  }
}
