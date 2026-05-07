package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.BiHashMap;
import com.koyomiji.asmweaver.util.PeekableIterator;
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

  public static List<LabelNode> extractLabels(List<AbstractInsnNode> insnList) {
    List<LabelNode> labels = new ArrayList<>();

    for (AbstractInsnNode insn : insnList) {
      if (insn instanceof LabelNode) {
        labels.add((LabelNode) insn);
      }
    }

    return labels;
  }

  public static boolean compareLabels(LabelNode label1, LabelNode label2, Function<LabelNode, LabelNode> labelMap) {
    return labelMap.apply(label1) == label2;
  }

  public static boolean compareLabels(List<LabelNode> labels1, List<LabelNode> labels2, Function<LabelNode, LabelNode> labelMap) {
    if (labels1.size() != labels2.size()) {
      return false;
    }

    for (int i = 0; i < labels1.size(); i++) {
      if (!compareLabels(labels1.get(i), labels2.get(i), labelMap)) {
        return false;
      }
    }

    return true;
  }

  public static boolean compareInsns(AbstractInsnNode insn1, AbstractInsnNode insn2, Function<LabelNode, LabelNode> labelMap) {
    if (insn1.getOpcode() != insn2.getOpcode()) {
      return false;
    }

    if (insn1 instanceof InsnNode && insn2 instanceof InsnNode) {
      return true;
    } else if (insn1 instanceof IntInsnNode && insn2 instanceof IntInsnNode) {
      return ((IntInsnNode) insn1).operand == ((IntInsnNode) insn2).operand;
    } else if (insn1 instanceof VarInsnNode && insn2 instanceof VarInsnNode) {
      return ((VarInsnNode) insn1).var == ((VarInsnNode) insn2).var;
    } else if (insn1 instanceof TypeInsnNode && insn2 instanceof TypeInsnNode) {
      return Objects.equals(((TypeInsnNode) insn1).desc, ((TypeInsnNode) insn2).desc);
    } else if (insn1 instanceof FieldInsnNode && insn2 instanceof FieldInsnNode) {
      FieldInsnNode field1 = (FieldInsnNode) insn1;
      FieldInsnNode field2 = (FieldInsnNode) insn2;
      return Objects.equals(field1.owner, field2.owner)
              && Objects.equals(field1.name, field2.name)
              && Objects.equals(field1.desc, field2.desc);
    } else if (insn1 instanceof MethodInsnNode && insn2 instanceof MethodInsnNode) {
      MethodInsnNode method1 = (MethodInsnNode) insn1;
      MethodInsnNode method2 = (MethodInsnNode) insn2;
      return Objects.equals(method1.owner, method2.owner)
              && Objects.equals(method1.name, method2.name)
              && Objects.equals(method1.desc, method2.desc)
              && method1.itf == method2.itf;
    } else if (insn1 instanceof InvokeDynamicInsnNode && insn2 instanceof InvokeDynamicInsnNode) {
      InvokeDynamicInsnNode indy1 = (InvokeDynamicInsnNode) insn1;
      InvokeDynamicInsnNode indy2 = (InvokeDynamicInsnNode) insn2;
      return Objects.equals(indy1.name, indy2.name)
              && Objects.equals(indy1.desc, indy2.desc)
              && Objects.equals(indy1.bsm, indy2.bsm)
              && Arrays.equals(indy1.bsmArgs, indy2.bsmArgs);
    } else if (insn1 instanceof JumpInsnNode && insn2 instanceof JumpInsnNode) {
      JumpInsnNode jump1 = (JumpInsnNode) insn1;
      JumpInsnNode jump2 = (JumpInsnNode) insn2;
      return compareLabels(jump1.label, jump2.label, labelMap);
    } else if (insn1 instanceof LabelNode && insn2 instanceof LabelNode) {
      return compareLabels((LabelNode) insn1, (LabelNode) insn2, labelMap);
    } else if (insn1 instanceof LdcInsnNode && insn2 instanceof LdcInsnNode) {
      return Objects.equals(((LdcInsnNode) insn1).cst, ((LdcInsnNode) insn2).cst);
    } else if (insn1 instanceof IincInsnNode && insn2 instanceof IincInsnNode) {
      IincInsnNode iinc1 = (IincInsnNode) insn1;
      IincInsnNode iinc2 = (IincInsnNode) insn2;
      return iinc1.var == iinc2.var && iinc1.incr == iinc2.incr;
    } else if (insn1 instanceof TableSwitchInsnNode && insn2 instanceof TableSwitchInsnNode) {
      TableSwitchInsnNode switch1 = (TableSwitchInsnNode) insn1;
      TableSwitchInsnNode switch2 = (TableSwitchInsnNode) insn2;
      return switch1.min == switch2.min
              && switch1.max == switch2.max
              && compareLabels(switch1.dflt, switch2.dflt, labelMap)
              && compareLabels(switch1.labels, switch2.labels, labelMap);
    } else if (insn1 instanceof LookupSwitchInsnNode && insn2 instanceof LookupSwitchInsnNode) {
      LookupSwitchInsnNode switch1 = (LookupSwitchInsnNode) insn1;
      LookupSwitchInsnNode switch2 = (LookupSwitchInsnNode) insn2;
      return compareLabels(switch1.dflt, switch2.dflt, labelMap)
              && Objects.equals(switch1.keys, switch2.keys)
              && compareLabels(switch1.labels, switch2.labels, labelMap);
    } else if (insn1 instanceof MultiANewArrayInsnNode && insn2 instanceof MultiANewArrayInsnNode) {
      MultiANewArrayInsnNode array1 = (MultiANewArrayInsnNode) insn1;
      MultiANewArrayInsnNode array2 = (MultiANewArrayInsnNode) insn2;
      return Objects.equals(array1.desc, array2.desc) && array1.dims == array2.dims;
    } else if (insn1 instanceof FrameNode && insn2 instanceof FrameNode) {
      FrameNode frame1 = (FrameNode) insn1;
      FrameNode frame2 = (FrameNode) insn2;
      return frame1.type == frame2.type
              && Objects.equals(frame1.local, frame2.local)
              && Objects.equals(frame1.stack, frame2.stack);
    } else if (insn1 instanceof LineNumberNode && insn2 instanceof LineNumberNode) {
      LineNumberNode line1 = (LineNumberNode) insn1;
      LineNumberNode line2 = (LineNumberNode) insn2;
      return line1.line == line2.line && compareLabels(line1.start, line2.start, labelMap);
    }

    // Non-structural instructions
    return Objects.equals(insn1, insn2);
  }

  public static boolean compareInsnsIgnoreLabels(AbstractInsnNode insn1, AbstractInsnNode insn2) {
    return compareInsns(insn1, insn2, Function.identity());
  }

  /**
   * Compare two instruction lists taking labels into account.
   *
   * @param list1
   * @param list2
   * @param labelMap
   * @return
   */
  public static boolean compareInsnLists(List<AbstractInsnNode> list1, List<AbstractInsnNode> list2, Map<LabelNode, LabelNode> labelMap) {
    Iterator<AbstractInsnNode> iter1 = list1.iterator();
    Iterator<AbstractInsnNode> iter2 = list2.iterator();

    while (iter1.hasNext() && iter2.hasNext()) {
      if (!compareInsns(iter1.next(), iter2.next(), labelMap::get)) {
        return false;
      }
    }

    return !iter1.hasNext() && !iter2.hasNext();
  }

  public static boolean compareInsnLists(List<AbstractInsnNode> list1, List<AbstractInsnNode> list2) {
    List<LabelNode> labels1 = extractLabels(list1);
    List<LabelNode> labels2 = extractLabels(list2);

    if (labels1.size() != labels2.size()) {
      return false;
    }

    Map<LabelNode, LabelNode> labelMap = new HashMap<>();

    for (int i = 0; i < labels1.size(); i++) {
      labelMap.put(labels1.get(i), labels2.get(i));
    }

    return compareInsnLists(list1, list2, labelMap);
  }

  /**
   * Extracts the base instructions from the diff.
   *
   * @param diff
   * @return
   */
  public static List<AbstractInsnNode> extractBase(InsnListDiff diff) {
    List<AbstractInsnNode> baseInstructions = new ArrayList<>();

    for (InsnListDiff.Operation op : diff.operations) {
      if (op.type == InsnListDiff.Operation.Type.MATCH || op.type == InsnListDiff.Operation.Type.DELETE) {
        baseInstructions.add(op.operand);
      }
    }

    return baseInstructions;
  }

  /**
   * Returns true if diff1 and diff2 have the same base instructions.
   *
   * @param diff1
   * @param diff2
   * @return
   */
  public static boolean compareBases(InsnListDiff diff1, InsnListDiff diff2) {
    List<AbstractInsnNode> base1 = extractBase(diff1);
    List<AbstractInsnNode> base2 = extractBase(diff2);
    return compareInsnLists(base1, base2);
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
        if (!compareInsns(opP.operand, opQBase.operand, Function.identity())) {
          throw new IllegalDiffException("p and q disagree on node identity");
        }

        if (opP.type == InsnListDiff.Operation.Type.MATCH) {
          qPrimeOps.add(opQBase);
          pPrimeOps.add(new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, opP.mode, opP.operand));
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

  /**
   * Note that this merge is not symmetric with respect to diff1 and diff2; directional insertions (INSERT_BEFORE/INSERT_AFTER) will be sorted according to the order of diff1 and diff2.
   */
  public static InsnListDiff merge(InsnListDiff diff1, InsnListDiff diff2) throws ConflictException {
    if (!compareBases(diff1, diff2)) {
      throw new IllegalDiffException("Cannot merge diffs with different bases");
    }

    List<InsnListDiff.Operation> mergedOps = new ArrayList<>();
    PeekableIterator<InsnListDiff.Operation> it1 = new PeekableIterator<>(diff1.operations.iterator());
    PeekableIterator<InsnListDiff.Operation> it2 = new PeekableIterator<>(diff2.operations.iterator());

    while (it1.hasNext() || it2.hasNext()) {
      List<InsnListDiff.Operation> ins1 = collectInsertions(it1);
      List<InsnListDiff.Operation> ins2 = collectInsertions(it2);

      mergedOps.addAll(mergeInsertionSlot(ins1, ins2));

      if (it1.hasNext() && it2.hasNext()) {
        InsnListDiff.Operation op1 = it1.next();
        InsnListDiff.Operation op2 = it2.next();

        // FIXME:
        if (!compareInsns(op1.operand, op2.operand, Function.identity())) {
          throw new IllegalDiffException("Diffs have different base instructions at the same position");
        }

        if (op1.type == InsnListDiff.Operation.Type.DELETE && op2.type == InsnListDiff.Operation.Type.DELETE) {
          throw new ConflictException("Both diffs delete the same instruction");
        }

        InsnListDiff.Operation.Type targetType =
                (op1.type == InsnListDiff.Operation.Type.DELETE || op2.type == InsnListDiff.Operation.Type.DELETE)
                        ? InsnListDiff.Operation.Type.DELETE : InsnListDiff.Operation.Type.MATCH;

        mergedOps.add(new InsnListDiff.Operation(targetType, op1.mode, op1.operand));
      }
    }
    return new InsnListDiff(mergedOps);
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

        if (!itQ.hasNext()) {
          throw new IllegalDiffException("Composition Error: q is shorter than intermediate B.");
        }
        InsnListDiff.Operation opQ = itQ.next();

        // FIXME:
        if (!compareInsns(opP.operand, opQ.operand, Function.identity())) {
          throw new IllegalDiffException("Composition Error: Operand mismatch at B.");
        }

        if (opQ.type == InsnListDiff.Operation.Type.MATCH) {
          ins1.add(opP);
        }
      } else if (opP.type == InsnListDiff.Operation.Type.DELETE) {
        List<InsnListDiff.Operation> qInsertions = collectInsertions(itQ);

        int matchIndex = -1;
        for (int i = 0; i < qInsertions.size(); i++) {
          // FIXME:
          if (compareInsns(qInsertions.get(i).operand, opP.operand, Function.identity())) {
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

        if (!itQ.hasNext()) {
          throw new IllegalDiffException("Composition Error: q is shorter than intermediate B.");
        }
        InsnListDiff.Operation opQ = itQ.next();

        // FIXME:
        if (!compareInsns(opP.operand, opQ.operand, Function.identity())) {
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

    return new InsnListDiff(result);
  }

  private static abstract class Heuristic {
    public abstract int calculate(int indexA, int indexB, BiHashMap<LabelNode, LabelNode> labelMap);
  }

  private static class FuzzyDistanceHeuristic extends Heuristic {
    private int[][] table;

    public FuzzyDistanceHeuristic(List<AbstractInsnNode> insnsA, List<AbstractInsnNode> insnsB) {
      this.table = new int[insnsA.size() + 1][insnsB.size() + 1];

      for (int j = 0; j <= insnsB.size(); j++) {
        table[insnsA.size()][j] = insnsB.size() - j;
      }

      for (int i = 0; i <= insnsA.size(); i++) {
        table[i][insnsB.size()] = insnsA.size() - i;
      }

      for (int i = insnsA.size() - 1; i >= 0; i--) {
        for (int j = insnsB.size() - 1; j >= 0; j--) {
          AbstractInsnNode a = insnsA.get(i);
          AbstractInsnNode b = insnsB.get(j);

          table[i][j] = 1 + Math.min(table[i + 1][j], table[i][j + 1]);

          if (compareInsnsIgnoreLabels(a, b)) {
            table[i][j] = Math.min(table[i][j], table[i + 1][j + 1]);
          }
        }
      }
    }

    @Override
    public int calculate(int indexA, int indexB, BiHashMap<LabelNode, LabelNode> labelMap) {
      return table[indexA][indexB];
    }
  }

  public static class MyersFuzzyDistanceHeuristic extends Heuristic {
    private final List<AbstractInsnNode> src;
    private final List<AbstractInsnNode> dst;
    private final int n;
    private final int m;

    // 対角線ごとの到達履歴を保持する: key = k, value = DiagonalHistory
    private final Map<Integer, DiagonalHistory> historyMap = new HashMap<>();

    // Myersの状態管理
    private final Map<Integer, Integer> v = new HashMap<>();
    private int currentD = -1;

    public MyersFuzzyDistanceHeuristic(List<AbstractInsnNode> src, List<AbstractInsnNode> dst) {
      this.src = src;
      this.dst = dst;
      this.n = src.size();
      this.m = dst.size();
      this.v.put(1, 0); // 初期値
    }

    /**
     * 終端から逆向きに計算した際の到達履歴を管理するクラス
     */
    private static class DiagonalHistory {
      int[] us = new int[4];
      int[] ds = new int[4];
      int size = 0;

      void add(int u, int d) {
        if (size > 0 && us[size - 1] >= u) {
          return;
        }
        if (size == us.length) {
          us = Arrays.copyOf(us, size * 2);
          ds = Arrays.copyOf(ds, size * 2);
        }
        us[size] = u;
        ds[size] = d;
        size++;
      }

      int getD(int targetU) {
        int left = 0;
        int right = size - 1;
        int ans = -1;
        while (left <= right) {
          int mid = (left + right) >>> 1;
          if (us[mid] >= targetU) {
            ans = ds[mid];
            right = mid - 1;
          } else {
            left = mid + 1;
          }
        }
        return ans;
      }
    }

    @Override
    public int calculate(int targetX, int targetY, BiHashMap<LabelNode, LabelNode> labelMap) {
      if (targetX < 0 || targetY < 0 || targetX > n || targetY > m) {
        throw new IndexOutOfBoundsException();
      }

      // 元のFuzzyDistanceHeuristicに合わせるため、終端 (n, m) を始点 (0, 0) とみなす座標変換
      int targetU = n - targetX;
      int targetW = m - targetY;
      int targetK = targetU - targetW;

      DiagonalHistory hist = historyMap.get(targetK);
      if (hist != null) {
        int d = hist.getD(targetU);
        if (d != -1) {
          return d;
        }
      }

      while (currentD < n + m) {
        currentD++;
        boolean targetReachedInCurrentD = false;

        for (int k = -currentD; k <= currentD; k += 2) {
          int u; // 逆向きのX座標（終端からの距離）
          int vPrev = v.getOrDefault(k - 1, -1);
          int vNext = v.getOrDefault(k + 1, -1);

          if (k == -currentD || (k != currentD && vPrev < vNext)) {
            u = vNext;
          } else {
            u = vPrev + 1;
          }

          int w = u - k; // 逆向きのY座標

          // スネーク（一致移動）を処理
          // 配列の後ろ（n-1, m-1）から前へ向かって比較する
          while (u < n && w < m && compareInsnsIgnoreLabels(src.get(n - 1 - u), dst.get(m - 1 - w))) {
            u++;
            w++;
          }
          v.put(k, u);

          int maxUForK = Math.min(n, m + k);
          int recordU = Math.min(u, maxUForK);
          if (recordU >= 0) {
            DiagonalHistory h = historyMap.computeIfAbsent(k, key -> new DiagonalHistory());
            h.add(recordU, currentD);
          }

          if (k == targetK && recordU >= targetU) {
            targetReachedInCurrentD = true;
          }
        }

        if (targetReachedInCurrentD) {
          return currentD;
        }
      }
      return -1;
    }
  }

  public static class State implements Comparable<State> {
    final int idxA;
    final int idxB;
    final int g;
    final int h; // Heuristic

    // Mapping from labels in A to labels in B
    BiHashMap<LabelNode, LabelNode> labelMap;

    final List<InsnListDiff.Operation> operations;

    public State(int idxA, int idxB, int g, int h,
                 BiHashMap<LabelNode, LabelNode> labelMap,
                 List<InsnListDiff.Operation> operations) {
      this.idxA = idxA;
      this.idxB = idxB;
      this.g = g;
      this.h = h;
      this.labelMap = labelMap;
      this.operations = operations;
    }

    public static State create(int idxA, int idxB, BiHashMap<LabelNode, LabelNode> labelMap, List<InsnListDiff.Operation> operations, Heuristic heuristicProvider) {
      int h = heuristicProvider.calculate(idxA, idxB, labelMap);
      int g = 0;

      for (InsnListDiff.Operation op : operations) {
        if (op.type == InsnListDiff.Operation.Type.INSERT || op.type == InsnListDiff.Operation.Type.DELETE) {
          g++;
        }
      }

//      return new State(idxA, idxB, h, labelMap, operations);
      return new State(idxA, idxB, g, h, labelMap, operations);
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

    public List<InsnListDiff.Operation> getOperations() {
      return operations;
    }

    @Override
    public int compareTo(State o) {
      return Integer.compare(this.f(), o.f());
    }
  }

  public static class StateKey {
    public final int idxA;
    public final int idxB;
    public final Map<LabelNode, LabelNode> aToB;

    public StateKey(int idxA, int idxB, Map<LabelNode, LabelNode> aToB) {
      this.idxA = idxA;
      this.idxB = idxB;
      this.aToB = aToB;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      StateKey stateKey = (StateKey) o;
      return idxA == stateKey.idxA && idxB == stateKey.idxB && Objects.equals(aToB, stateKey.aToB);
    }

    @Override
    public int hashCode() {
      return Objects.hash(idxA, idxB, aToB);
    }
  }

  private static ArrayList<InsnListDiff.Operation> concat(
          List<InsnListDiff.Operation> ops,
          InsnListDiff.Operation op
  ) {
    var newOps = new ArrayList<>(ops);
    newOps.add(op);
    return newOps;
  }

  private static AbstractInsnNode getOrNull(AbstractInsnNode[] arr, int idx) {
    if (idx < 0 || idx >= arr.length) return null;
    return arr[idx];
  }

  private static AbstractInsnNode getOrNull(InsnList insns, int idx) {
    if (idx < 0 || idx >= insns.size()) return null;
    return insns.get(idx);
  }

  private static AbstractInsnNode getOrNull(List<AbstractInsnNode> insns, int idx) {
    if (idx < 0 || idx >= insns.size()) return null;
    return insns.get(idx);
  }

  private static Map<LabelNode, AbstractInsnNode> lastLabelOccurrenceMap(AbstractInsnNode[] arr) {
    Map<LabelNode, AbstractInsnNode> map = new HashMap<>();

    for (AbstractInsnNode insn : arr) {
      var labels = getLabelTargets(insn);

      for (var label : labels) {
        map.put(label, insn);
      }
    }

    return map;
  }

  private static AbstractInsnNode getPrevious(InsnList insns, AbstractInsnNode insn) {
    if (insn == null) return insns.getLast();
    return insn.getPrevious();
  }

  private static void removeDeadLabels(
          State state,
          Map<LabelNode, AbstractInsnNode> lastOccurrenceMapA,
          Map<LabelNode, AbstractInsnNode> lastOccurrenceMapB,
          List<AbstractInsnNode> insnsA,
          List<AbstractInsnNode> insnsB
  ) {
    var insnA = getOrNull(insnsA, state.idxA);
    var insnB = getOrNull(insnsB, state.idxB);

    // 実際に変更があったかどうかを追跡
    boolean isCopied = false;
    var currentLabelMap = state.labelMap;

    // --- Process A ---
    var prevA = getOrNull(insnsA, state.idxA - 1);
    if (prevA != null) {
      var labels = getLabelTargets(prevA);
      for (var label : labels) {
        if (lastOccurrenceMapA.get(label) == prevA) {
          var mappedB = currentLabelMap.get(label);
          if (mappedB != null) {
            var lastOccInsnB = lastOccurrenceMapB.get(mappedB);
            if (insnsB.indexOf(lastOccInsnB) < state.idxB) {
              // 初回変更時のみコピーを作成
              if (!isCopied) {
                currentLabelMap = new BiHashMap<>(currentLabelMap);
                isCopied = true;
              }
              currentLabelMap.remove(label);
            }
          }
        }
      }
    }

    // --- Process B ---
    var prevB = getOrNull(insnsB, state.idxB - 1);
    if (prevB != null) {
      var labels = getLabelTargets(prevB);
      for (var label : labels) {
        if (lastOccurrenceMapB.get(label) == prevB) {
          var mappedA = currentLabelMap.getKey(label);
          if (mappedA != null) {
            var lastOccInsnA = lastOccurrenceMapA.get(mappedA);
            if (insnsA.indexOf(lastOccInsnA) < state.idxA) {
              // 初回変更時のみコピーを作成
              if (!isCopied) {
                currentLabelMap = new BiHashMap<>(currentLabelMap);
                isCopied = true;
              }
              currentLabelMap.remove(mappedA);
            }
          }
        }
      }
    }

    // 変更があった場合のみ、元のStateに反映
    if (isCopied) {
      state.labelMap = currentLabelMap;
    }
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
    }

    // FIXME: Non-standard
    return targets;
  }

  public static InsnListDiff diff(List<AbstractInsnNode> listA, List<AbstractInsnNode> listB) {
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
                      new BiHashMap<>(), new ArrayList<>(),
                      heuristicProvider
              )
      );
    }

    int j = 0;

    while (!pq.isEmpty()) {
      State current = pq.poll();

      if (current.idxA >= insnsA.length && current.idxB >= insnsB.length) {
        return new InsnListDiff(current.operations);
      }

      var currentKey = new StateKey(current.idxA, current.idxB, current.labelMap.forwardMap());
      if (visited.containsKey(currentKey) && visited.get(currentKey) < current.g()) {
        continue;
      }

      if (j++ % 100000 == 0) {
//        Logger.getInstance().log(
        System.out.println(
                String.format("State: idxA=%d, idxB=%d, g=%d, h=%d, f=%d, ops=%d",
                        current.idxA, current.idxB,
                        current.g(), current.h, current.f(),
                        current.operations.size()
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
                concat(
                        current.operations,
                        new InsnListDiff.Operation(InsnListDiff.Operation.Type.DELETE, InsnListDiff.Operation.Mode.BETWEEN, insnsA[current.idxA])
                ),
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
                concat(
                        current.operations,
                        new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT, InsnListDiff.Operation.Mode.BETWEEN, insnsB[current.idxB])

                ),
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
        boolean contentMatch = compareInsnsIgnoreLabels(insnA, insnB);

        // Having different number of target labels implies mismatch
        if (targetsA.size() != targetsB.size()) {
          contentMatch = false;
        }

        var nextRealIdxA = current.idxA + 1;
        var nextRealIdxB = current.idxB + 1;

        if (contentMatch) {
          BiHashMap<LabelNode, LabelNode> newAToB = new BiHashMap<>(current.labelMap);

          for (int i = 0; i < targetsA.size(); i++) {
            if (!newAToB.canPut(targetsA.get(i), targetsB.get(i))) {
              break match;
            }

            newAToB.put(targetsA.get(i), targetsB.get(i));
          }

          var state = State.create(
                  nextRealIdxA, nextRealIdxB,
                  newAToB,
                  current.operations,
//                  concat(
//                          current.operations,
//                          new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, null, insnA)
//                  ),
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
    var newStateKey = new StateKey(newState.idxA, newState.idxB, newState.labelMap.forwardMap());

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
