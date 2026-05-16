package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.heuristic.Heuristic;
import com.koyomiji.asmweaver.heuristic.MyersFuzzyDistanceHeuristic;
import com.koyomiji.asmweaver.util.BiPersistentHashMap;
import com.koyomiji.asmweaver.util.PeekableIterator;
import com.koyomiji.asmweaver.util.PersistentHashMap;
import com.koyomiji.asmweaver.util.tuple.Pair;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.util.function.Function;

public class InsnListDiffUtils {
  public static InsnListDiff invert(InsnListDiff diff) {
    List<InsnListDiff.Operation> invertedOperations = new ArrayList<>();

    for (InsnListDiff.Operation op : diff.operations) {
      InsnListDiff.Operation invertedOp;

      switch (op.type) {
        case MATCH:
          invertedOp = new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, null, op.operand);
          break;
        case INSERT:
          invertedOp = new InsnListDiff.Operation(InsnListDiff.Operation.Type.DELETE, op.mode, op.operand);
          break;
        case DELETE:
          invertedOp = new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT, op.mode, op.operand);
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
    List<InsnListDiff.Operation> qPrimeOps = new ArrayList<>();
    List<InsnListDiff.Operation> pPrimeOps = new ArrayList<>();

    Set<AbstractInsnNode> pInserted = collectInserted(p);
    Iterator<InsnListDiff.Operation> itP = p.operations.iterator();
    PeekableIterator<InsnListDiff.Operation> itQ = new PeekableIterator<>(q.operations.iterator());

    while (itP.hasNext()) {
      InsnListDiff.Operation opP = itP.next();

      if (opP.type == InsnListDiff.Operation.Type.DELETE) {
        qPrimeOps.add(new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, opP.mode, opP.operand));
        pPrimeOps.add(new InsnListDiff.Operation(InsnListDiff.Operation.Type.DELETE, opP.mode, opP.operand));
      } else if (opP.type == InsnListDiff.Operation.Type.MATCH || isInsert(opP)) {
        while (itQ.hasNext() && isInsert(itQ.peek())) {
          InsnListDiff.Operation opQIns = itQ.next();
          // FIXME
//          verifyNoInternalDependency(opQIns.operand, pInsertedNodes);

          qPrimeOps.add(opQIns);
          pPrimeOps.add(new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, opQIns.mode, opQIns.operand));
        }

        if (!itQ.hasNext()) {
          throw new IllegalDiffException("p has remaining operations after q is exhausted");
        }
        InsnListDiff.Operation opQBase = itQ.next();

        // FIXME
//        if (!compareInsns(opP.operand, opQBase.operand, Function.identity())) {
        if (!AbstractInsnNodeHelper.equalsIgnoreLabelsIgnoreLocals(opP.operand, opQBase.operand)) {
          throw new IllegalDiffException("p and q disagree on node identity");
        }

        if (opP.type == InsnListDiff.Operation.Type.MATCH) {
          qPrimeOps.add(opQBase);
          if (opQBase.type == InsnListDiff.Operation.Type.MATCH) {
            pPrimeOps.add(new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, opP.mode, opP.operand));
          }
        } else {
          if (opQBase.type == InsnListDiff.Operation.Type.DELETE) {
            throw new ConflictException("p inserts a node that q deletes");
          }

          pPrimeOps.add(new InsnListDiff.Operation(opP.type, opP.mode, opP.operand));
        }
      }
    }

    while (itQ.hasNext()) {
      InsnListDiff.Operation opQ = itQ.next();

      if (isInsert(opQ)) {
        qPrimeOps.add(opQ);
        pPrimeOps.add(new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, opQ.mode, opQ.operand));
      } else {
        throw new IllegalDiffException("q has remaining operations after p is exhausted");
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
        inserted.add(op.operand);
      }
    }

    return inserted;
  }

  private static List<InsnListDiff.Operation> mergeInsertionSlot(
          List<InsnListDiff.Operation> ins1,
          List<InsnListDiff.Operation> ins2) throws ConflictException {

    List<InsnListDiff.Operation> result = new ArrayList<>();

    boolean hasBetween1 = ins1.stream().anyMatch(o -> o.type == InsnListDiff.Operation.Type.INSERT && o.mode == InsnListDiff.Operation.Mode.BETWEEN);
    boolean hasBetween2 = ins2.stream().anyMatch(o -> o.type == InsnListDiff.Operation.Type.INSERT && o.mode == InsnListDiff.Operation.Mode.BETWEEN);

    if (hasBetween1 && hasBetween2) {
      throw new ConflictException("Both diffs have BETWEEN insertions at the same position");
    }

    for (InsnListDiff.Operation o : ins2) {
      if (o.type == InsnListDiff.Operation.Type.INSERT && o.mode == InsnListDiff.Operation.Mode.AFTER) result.add(o);
    }
    for (InsnListDiff.Operation o : ins1) {
      if (o.type == InsnListDiff.Operation.Type.INSERT && o.mode == InsnListDiff.Operation.Mode.AFTER) result.add(o);
    }

    for (InsnListDiff.Operation o : ins1) {
      if (o.type == InsnListDiff.Operation.Type.INSERT && o.mode == InsnListDiff.Operation.Mode.BETWEEN) result.add(o);
    }
    for (InsnListDiff.Operation o : ins2) {
      if (o.type == InsnListDiff.Operation.Type.INSERT && o.mode == InsnListDiff.Operation.Mode.BETWEEN) result.add(o);
    }

    for (InsnListDiff.Operation o : ins1) {
      if (o.type == InsnListDiff.Operation.Type.INSERT && o.mode == InsnListDiff.Operation.Mode.BEFORE) result.add(o);
    }
    for (InsnListDiff.Operation o : ins2) {
      if (o.type == InsnListDiff.Operation.Type.INSERT && o.mode == InsnListDiff.Operation.Mode.BEFORE) result.add(o);
    }

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

        // FIXME: should not ignore
        if (!AbstractInsnNodeHelper.equalsIgnoreLabelsIgnoreLocals(opP.operand, opQ.operand)) {
          throw new IllegalDiffException("Composition Error: Operand mismatch at B.");
        }

        if (opQ.type == InsnListDiff.Operation.Type.MATCH) {
          ins1.add(opP);
        }
      } else if (opP.type == InsnListDiff.Operation.Type.DELETE) {
        List<InsnListDiff.Operation> qInsertions = collectInsertions(itQ);

        int matchIndex = -1;
        for (int i = 0; i < qInsertions.size(); i++) {
          // FIXME: should not ignore
          if (AbstractInsnNodeHelper.equalsIgnoreLabelsIgnoreLocals(qInsertions.get(i).operand, opP.operand)) {
            matchIndex = i;
            break;
          }
        }

        if (matchIndex != -1) {
          InsnListDiff.Operation matchingInsert = qInsertions.remove(matchIndex);
          ins2.addAll(qInsertions);

          result.addAll(mergeInsertionSlot(ins1, ins2));
          ins1.clear();
          ins2.clear();

          result.add(new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, matchingInsert.mode, opP.operand));
        } else {
          ins2.addAll(qInsertions);
          result.addAll(mergeInsertionSlot(ins1, ins2));
          ins1.clear();
          ins2.clear();
          result.add(opP);
        }
      } else { // MATCH
        ins2.addAll(collectInsertions(itQ));

        InsnListDiff.Operation opQ = IteratorHelper.nextOrThrow(itQ, () -> new IllegalDiffException("Composition Error: q is shorter than intermediate B."));

        // FIXME: should not ignore
        if (!AbstractInsnNodeHelper.equalsIgnoreLabelsIgnoreLocals(opP.operand, opQ.operand)) {
          throw new IllegalDiffException("Composition Error: Operand mismatch at C.");
        }

        result.addAll(mergeInsertionSlot(ins1, ins2));
        ins1.clear();
        ins2.clear();

        result.add(new InsnListDiff.Operation(opQ.type, opQ.mode, opP.operand));
      }
    }

    ins2.addAll(collectInsertions(itQ));
    result.addAll(mergeInsertionSlot(ins1, ins2));

    IteratorHelper.throwIfNext(itQ, () -> new IllegalDiffException("Composition Error: q has remaining operations after p is exhausted."));

    return new InsnListDiff(result);
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
      var labels = getLabelTargets(insn);

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
    // 実際に変更があったかどうかを追跡
//    boolean isCopied = false;
    var currentLabelMap = state.labelMap;

    // --- Process A ---
//    var prevA = getOrNull(insnsA, state.idxA - 1);
//    if (prevA != null) {
//      var labels = getLabelTargets(prevA);
//      for (var label : labels) {
//        if (lastOccurrenceMapA.get(label) == prevA) {
//          var mappedB = currentLabelMap.get(label);
//          if (mappedB != null) {
//            var lastOccInsnB = lastOccurrenceMapB.get(mappedB);
//            if (insnsB.indexOf(lastOccInsnB) < state.idxB) {
//              // 初回変更時のみコピーを作成
////              if (!isCopied) {
////                currentLabelMap = new BiHashMap<>(currentLabelMap);
////                isCopied = true;
////              }
////              currentLabelMap.remove(label);
//              currentLabelMap = currentLabelMap.remove(label);
//            }
//          }
//        }
//      }
//    }

    int prevIdxA = state.idxA - 1;

    if (prevIdxA >= 0) {
      var prevA = insnsA.get(prevIdxA);
      var labels = getLabelTargets(prevA);
      for (var label : labels) {
        if (lastOccurrenceMapA.get(label) == prevIdxA) {
          var mappedB = currentLabelMap.get(label);
          if (mappedB != null) {
//            var lastOccInsnB = getOrNull(insnsB, lastOccurrenceMapB.get(mappedB));
//            if (lastOccInsnB != null && insnsB.indexOf(lastOccInsnB) < state.idxB) {
//              currentLabelMap = currentLabelMap.remove(label);
//            }

            int lastOccIdxB = lastOccurrenceMapB.get(mappedB);
            if (lastOccIdxB < state.idxB) {
              currentLabelMap = currentLabelMap.remove(label);
            }
          }
        }
      }
    }

    // --- Process B ---
//    var prevB = getOrNull(insnsB, state.idxB - 1);
//    if (prevB != null) {
//      var labels = getLabelTargets(prevB);
//      for (var label : labels) {
//        if (lastOccurrenceMapB.get(label) == prevB) {
//          var mappedA = currentLabelMap.getKey(label);
//          if (mappedA != null) {
//            var lastOccInsnA = lastOccurrenceMapA.get(mappedA);
//            if (insnsA.indexOf(lastOccInsnA) < state.idxA) {
//              // 初回変更時のみコピーを作成
////              if (!isCopied) {
////                currentLabelMap = new BiHashMap<>(currentLabelMap);
////                isCopied = true;
////              }
////              currentLabelMap.remove(mappedA);
//              currentLabelMap = currentLabelMap.remove(mappedA);
//            }
//          }
//        }
//      }
//    }

    int prevIdxB = state.idxB - 1;
    if (prevIdxB >= 0) {
      var prevB = insnsB.get(prevIdxB);
      var labels = getLabelTargets(prevB);
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

    // 変更があった場合のみ、元のStateに反映
//    if (isCopied) {
    state.labelMap = currentLabelMap;
//    }
  }

  public static List<LabelNode> getLabelTargets(AbstractInsnNode insn) {
    List<LabelNode> targets = new ArrayList<>();

    if (insn instanceof JumpInsnNode) {
      targets.add(((JumpInsnNode) insn).label);
    } else if (insn instanceof LabelNode) {
      targets.add((LabelNode) insn);
    } else if (insn instanceof TableSwitchInsnNode) {
      TableSwitchInsnNode tsw = (TableSwitchInsnNode) insn;
      targets.add(tsw.dflt);
      targets.addAll(tsw.labels);
    } else if (insn instanceof LookupSwitchInsnNode) {
      LookupSwitchInsnNode lsw = (LookupSwitchInsnNode) insn;
      targets.add(lsw.dflt);
      targets.addAll(lsw.labels);
    } else if (insn instanceof LineNumberNode) {
      targets.add(((LineNumberNode) insn).start);
    } else if (insn instanceof IHasLabelNodes) {
      targets.addAll(((IHasLabelNodes) insn).getLabels());
    }

    return targets;
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

      if (j++ % 10000 == 0) {
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
                new InsnListDiff.Operation(InsnListDiff.Operation.Type.DELETE, InsnListDiff.Operation.Mode.BETWEEN, insnsA[current.idxA]),
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
                new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT, InsnListDiff.Operation.Mode.BETWEEN, insnsB[current.idxB]),
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

        List<LabelNode> targetsA = getLabelTargets(insnA);
        List<LabelNode> targetsB = getLabelTargets(insnB);

        // TODO: local
        boolean contentMatch = AbstractInsnNodeHelper.equalsIgnoreLabelsIgnoreLocals(insnA, insnB);

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

//            newDuAToB.put(duChainA, varB);
//            newDuBToA.put(duChainB, varA);
            newDuAToB = newDuAToB.put(duChainA, varB);
            newDuBToA = newDuBToA.put(duChainB, varA);
          }

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
                  new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, null, insnA),
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
}
