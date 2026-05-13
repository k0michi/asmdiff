package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.PeekableIterator;
import com.koyomiji.asmweaver.util.tuple.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;

public class ListDiffUtils {
  public static <T> ListDiff<T> invert(ListDiff<T> diff) {
    List<ListDiff.Operation<T>> invertedOperations = new ArrayList<>();

    for (ListDiff.Operation<T> op : diff.operations) {
      ListDiff.Operation<T> invertedOp;

      switch (op.type) {
        case MATCH:
          invertedOp = new ListDiff.Operation<>(ListDiff.Operation.Type.MATCH, op.mode, op.operand1, op.operand2);
          break;
        case INSERT:
          invertedOp = new ListDiff.Operation<>(ListDiff.Operation.Type.DELETE, op.mode, op.operand1, op.operand2);
          break;
        case DELETE:
          invertedOp = new ListDiff.Operation<>(ListDiff.Operation.Type.INSERT, op.mode, op.operand1, op.operand2);
          break;
        default:
          throw new IllegalStateException("Unexpected operation type: " + op.type);
      }

      invertedOperations.add(invertedOp);
    }

    return new ListDiff<>(invertedOperations);
  }

  public static <T> Pair<ListDiff<T>, ListDiff<T>> commute(ListDiff<T> p, ListDiff<T> q, BiPredicate<T, T> compare) throws ConflictException {
    List<ListDiff.Operation<T>> qPrimeOps = new ArrayList<>();
    List<ListDiff.Operation<T>> pPrimeOps = new ArrayList<>();

    Iterator<ListDiff.Operation<T>> itP = p.operations.iterator();
    PeekableIterator<ListDiff.Operation<T>> itQ = new PeekableIterator<>(q.operations.iterator());

    while (itP.hasNext()) {
      ListDiff.Operation<T> opP = itP.next();

      if (opP.type == ListDiff.Operation.Type.DELETE) {
        // DELETEの場合、operand1が対象。q'ではその要素をMATCH（維持）させる
        qPrimeOps.add(new ListDiff.Operation<>(ListDiff.Operation.Type.MATCH, opP.mode, opP.operand1, opP.operand1));
        pPrimeOps.add(new ListDiff.Operation<>(ListDiff.Operation.Type.DELETE, opP.mode, opP.operand1, null));
      } else {
        // opP が MATCH または INSERT の場合
        T valP = (opP.type == ListDiff.Operation.Type.INSERT) ? opP.operand2 : opP.operand1;

        while (itQ.hasNext() && itQ.peek().type == ListDiff.Operation.Type.INSERT) {
          ListDiff.Operation<T> opQIns = itQ.next();
          qPrimeOps.add(opQIns);
          // qが挿入した要素を、p'側ではMATCH（維持）として扱う
          pPrimeOps.add(new ListDiff.Operation<>(ListDiff.Operation.Type.MATCH, opQIns.mode, opQIns.operand2, opQIns.operand2));
        }

        if (!itQ.hasNext()) {
          throw new IllegalDiffException("p has remaining operations after q is exhausted");
        }
        ListDiff.Operation<T> opQBase = itQ.next();
        T valQBase = opQBase.operand1; // MATCHかDELETEなのでoperand1を使用

        if (!compare.test(valP, valQBase)) {
          throw new IllegalDiffException("p and q disagree on node identity");
        }

        if (opP.type == ListDiff.Operation.Type.MATCH) {
          qPrimeOps.add(opQBase);
          if (opQBase.type == ListDiff.Operation.Type.MATCH) {
            pPrimeOps.add(new ListDiff.Operation<>(ListDiff.Operation.Type.MATCH, opP.mode, opP.operand1, opP.operand1));
          }
        } else {
          // opP.type == INSERT
          if (opQBase.type == ListDiff.Operation.Type.DELETE) {
            throw new ConflictException("p inserts a node that q deletes");
          }
          // pが挿入しようとしている要素をp'でもそのまま挿入
          pPrimeOps.add(new ListDiff.Operation<>(ListDiff.Operation.Type.INSERT, opP.mode, null, opP.operand2));
        }
      }
    }

    while (itQ.hasNext()) {
      ListDiff.Operation<T> opQ = itQ.next();
      if (opQ.type == ListDiff.Operation.Type.INSERT) {
        qPrimeOps.add(opQ);
        pPrimeOps.add(new ListDiff.Operation<>(ListDiff.Operation.Type.MATCH, opQ.mode, opQ.operand2, opQ.operand2));
      } else {
        throw new IllegalDiffException("q has remaining operations after p is exhausted");
      }
    }

    return new Pair<>(new ListDiff<>(qPrimeOps), new ListDiff<>(pPrimeOps));
  }

  public static <T> ListDiff<T> diff(List<T> list1, List<T> list2, BiPredicate<T, T> compare) {
    int[][] dp = new int[list1.size() + 1][list2.size() + 1];

    for (int i = 0; i <= list1.size(); i++) {
      dp[i][0] = i;
    }

    for (int j = 0; j <= list2.size(); j++) {
      dp[0][j] = j;
    }

    for (int i = 1; i <= list1.size(); i++) {
      for (int j = 1; j <= list2.size(); j++) {
        if (compare.test(list1.get(i - 1), list2.get(j - 1))) {
          dp[i][j] = dp[i - 1][j - 1];
        } else {
          dp[i][j] = Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1);
        }
      }
    }

    List<ListDiff.Operation<T>> operations = new ArrayList<>();

    int i = list1.size();
    int j = list2.size();

    while (i > 0 && j > 0) {
      if (compare.test(list1.get(i - 1), list2.get(j - 1))) {
        operations.add(new ListDiff.Operation<>(ListDiff.Operation.Type.MATCH, ListDiff.Operation.Mode.BETWEEN, list1.get(i - 1), list2.get(j - 1)));
        i--;
        j--;
//      } else if (dp[i][j] == dp[i - 1][j] + 1) {
//        operations.add(new ListDiff.Operation<>(ListDiff.Operation.Type.DELETE, null, list1.get(i - 1)));
//        i--;
//      } else {
//        operations.add(new ListDiff.Operation<>(ListDiff.Operation.Type.INSERT, null, list2.get(j - 1)));
//        j--;
//      }
      } else if (dp[i][j] == dp[i][j - 1] + 1) {
//        operations.add(new ListDiff.Operation<>(ListDiff.Operation.Type.INSERT, null, list2.get(j - 1)));
        operations.add(new ListDiff.Operation<>(ListDiff.Operation.Type.INSERT, ListDiff.Operation.Mode.BETWEEN, null, list2.get(j - 1)));
        j--;
      } else {
//        operations.add(new ListDiff.Operation<>(ListDiff.Operation.Type.DELETE, null, list1.get(i - 1)));
        operations.add(new ListDiff.Operation<>(ListDiff.Operation.Type.DELETE, ListDiff.Operation.Mode.BETWEEN, list1.get(i - 1), null));
        i--;
      }
    }

    while (i > 0) {
//      operations.add(new ListDiff.Operation<>(ListDiff.Operation.Type.DELETE, null, list1.get(i - 1)));
      operations.add(new ListDiff.Operation<>(ListDiff.Operation.Type.DELETE, ListDiff.Operation.Mode.BETWEEN, list1.get(i - 1), null));
      i--;
    }

    while (j > 0) {
//      operations.add(new ListDiff.Operation<>(ListDiff.Operation.Type.INSERT, null, list2.get(j - 1)));
      operations.add(new ListDiff.Operation<>(ListDiff.Operation.Type.INSERT, ListDiff.Operation.Mode.BETWEEN, null, list2.get(j - 1)));
      j--;
    }

    List<ListDiff.Operation<T>> reversedOperations = new ArrayList<>();

    for (int k = operations.size() - 1; k >= 0; k--) {
      reversedOperations.add(operations.get(k));
    }

    return new ListDiff<>(reversedOperations);
  }

  public static <T> ListDiff<T> diffNullableValue(T element1, T element2, BiPredicate<T, T> compare) {
    return diff(ListHelper.ofNullable(element1), ListHelper.ofNullable(element2), compare);
  }

  public static <T> ListDiff<T> diffNonNullableValue(T element1, T element2, BiPredicate<T, T> compare) {
    return diff(ListHelper.ofNonNullable(element1), ListHelper.ofNonNullable(element2), compare);
  }

  public static <T> List<T> patch(List<T> list, ListDiff<T> diff) {
    List<T> result = new ArrayList<>();
    int i = 0;

    for (ListDiff.Operation<T> op : diff.operations) {
      switch (op.type) {
        case MATCH:
          result.add(list.get(i));
          i++;
          break;
        case INSERT:
          result.add(op.operand2);
          break;
        case DELETE:
          i++;
          break;
        default:
          throw new IllegalStateException("Unexpected operation type: " + op.type);
      }
    }

    return result;
  }

  public static <T> T patchNullableValue(T element, ListDiff<T> diff) throws IllegalDiffException {
    List<T> patched = patch(ListHelper.ofNullable(element), diff);

    if (patched.size() > 1) {
      throw new IllegalDiffException("Diff results in multiple elements, expected at most one");
    }

    return patched.isEmpty() ? null : patched.get(0);
  }

  public static <T> T patchNonNullableValue(T element, ListDiff<T> diff) throws IllegalDiffException {
    List<T> patched = patch(ListHelper.ofNonNullable(element), diff);

    if (patched.size() != 1) {
      throw new IllegalDiffException("Diff results in zero or multiple elements, expected exactly one");
    }

    return patched.get(0);
  }

  private static <T> boolean isInsert(ListDiff.Operation<T> op) {
    return op.type == ListDiff.Operation.Type.INSERT;
  }

  private static <T> List<ListDiff.Operation<T>> mergeInsertionSlot(List<ListDiff.Operation<T>> ins1, List<ListDiff.Operation<T>> ins2) throws ConflictException {
    List<ListDiff.Operation<T>> result = new ArrayList<>();

//    boolean hasBetween1 = ins1.stream().anyMatch(o -> o.type == ListDiff.Operation.Type.INSERT && o.mode == ListDiff.Operation.Mode.BETWEEN);
//    boolean hasBetween2 = ins2.stream().anyMatch(o -> o.type == ListDiff.Operation.Type.INSERT && o.mode == ListDiff.Operation.Mode.BETWEEN);
//
//    if (hasBetween1 && hasBetween2) {
//      throw new ConflictException("Both diffs have BETWEEN insertions at the same position");
//    }


    for (ListDiff.Operation<T> o : ins2) {
//      if (o.type == InsnListDiff.Operation.Type.INSERT && o.mode == InsnListDiff.Operation.Mode.AFTER) result.add(o);
      if (o.type == ListDiff.Operation.Type.INSERT && o.mode == ListDiff.Operation.Mode.AFTER) result.add(o);
    }
//    for (InsnListDiff.Operation o : ins1) {
//      if (o.type == InsnListDiff.Operation.Type.INSERT && o.mode == InsnListDiff.Operation.Mode.AFTER) result.add(o);
//    }
    for (ListDiff.Operation<T> o : ins1) {
      if (o.type == ListDiff.Operation.Type.INSERT && o.mode == ListDiff.Operation.Mode.AFTER) result.add(o);
    }

//    for (InsnListDiff.Operation o : ins1) {
//      if (o.type == InsnListDiff.Operation.Type.INSERT && o.mode == InsnListDiff.Operation.Mode.BETWEEN) result.add(o);
//    }
//    for (InsnListDiff.Operation o : ins2) {
//      if (o.type == InsnListDiff.Operation.Type.INSERT && o.mode == InsnListDiff.Operation.Mode.BETWEEN) result.add(o);
//    }
    for (ListDiff.Operation<T> o : ins1) {
      if (o.type == ListDiff.Operation.Type.INSERT && o.mode == ListDiff.Operation.Mode.BETWEEN) result.add(o);
    }
    for (ListDiff.Operation<T> o : ins2) {
      if (o.type == ListDiff.Operation.Type.INSERT && o.mode == ListDiff.Operation.Mode.BETWEEN) result.add(o);
    }

//    for (InsnListDiff.Operation o : ins1) {
//      if (o.type == InsnListDiff.Operation.Type.INSERT && o.mode == InsnListDiff.Operation.Mode.BEFORE) result.add(o);
//    }
//    for (InsnListDiff.Operation o : ins2) {
//      if (o.type == InsnListDiff.Operation.Type.INSERT && o.mode == InsnListDiff.Operation.Mode.BEFORE) result.add(o);
//    }
    for (ListDiff.Operation<T> o : ins1) {
      if (o.type == ListDiff.Operation.Type.INSERT && o.mode == ListDiff.Operation.Mode.BEFORE) result.add(o);
    }
    for (ListDiff.Operation<T> o : ins2) {
      if (o.type == ListDiff.Operation.Type.INSERT && o.mode == ListDiff.Operation.Mode.BEFORE) result.add(o);
    }

    return result;
  }

  private static <T> List<ListDiff.Operation<T>> collectInsertions(PeekableIterator<ListDiff.Operation<T>> it) {
    List<ListDiff.Operation<T>> insertions = new ArrayList<>();

    while (it.hasNext() && it.peek().type == ListDiff.Operation.Type.INSERT) {
      insertions.add(it.next());
    }

    return insertions;
  }

  public static <T> ListDiff<T> compose(ListDiff<T> p, ListDiff<T> q, BiPredicate<T, T> compare) throws ConflictException {
    return compose(p, q, compare, compare);
  }

  /**
   *
   * @param p Diff from list 1 to list 2.
   * @param q Diff from list 2 to list 3.
   * @param compare13 Compare element in list 1 and element in list 3.
   * @param compare2 Compare element in list 2 and element in list 2.
   * @return
   * @param <T>
   * @throws ConflictException
   */
  public static <T> ListDiff<T> compose(ListDiff<T> p, ListDiff<T> q, BiPredicate<T, T> compare13, BiPredicate<T, T> compare2) throws ConflictException {
    List<ListDiff.Operation<T>> result = new ArrayList<>();

    PeekableIterator<ListDiff.Operation<T>> itP = new PeekableIterator<>(p.operations.iterator());
    PeekableIterator<ListDiff.Operation<T>> itQ = new PeekableIterator<>(q.operations.iterator());

    List<ListDiff.Operation<T>> ins1 = new ArrayList<>();
    List<ListDiff.Operation<T>> ins2 = new ArrayList<>();

    while (itP.hasNext()) {
//      InsnListDiff.Operation opP = itP.next();
      ListDiff.Operation<T> opP = itP.next();

//      if (opP.type == InsnListDiff.Operation.Type.INSERT) {
      if (opP.type == ListDiff.Operation.Type.INSERT) {
        ins2.addAll(collectInsertions(itQ));

//        InsnListDiff.Operation opQ = IteratorHelper.nextOrThrow(itQ, () -> new IllegalDiffException("Composition Error: q is shorter than intermediate B."));
        ListDiff.Operation<T> opQ = IteratorHelper.nextOrThrow(itQ, () -> new IllegalDiffException("Composition Error: q is shorter than intermediate B."));

//        if (!compare.test(opP.operand2, opQ.operand1)) {
        if (!compare2.test(opP.operand2, opQ.operand1)) {
          throw new IllegalDiffException("Composition Error: Operand mismatch at B.");
        }

//        if (opQ.type == InsnListDiff.Operation.Type.MATCH) {
        if (opQ.type == ListDiff.Operation.Type.MATCH) {
          ins1.add(opP);
        }
//      } else if (opP.type == InsnListDiff.Operation.Type.DELETE) {
      } else if (opP.type == ListDiff.Operation.Type.DELETE) {
        result.addAll(mergeInsertionSlot(ins1, ins2));
        ins1.clear();
        ins2.clear();

        result.add(opP);
      } else { // MATCH
        ins2.addAll(collectInsertions(itQ));

//        InsnListDiff.Operation opQ = IteratorHelper.nextOrThrow(itQ, () -> new IllegalDiffException("Composition Error: q is shorter than intermediate B."));
        ListDiff.Operation<T> opQ = IteratorHelper.nextOrThrow(itQ, () -> new IllegalDiffException("Composition Error: q is shorter than intermediate B."));

        // FIXME: should not ignore
//        if (!compareInsnsIgnoreLabelsIgnoreLocals(opP.operand, opQ.operand)) {
//          throw new IllegalDiffException("Composition Error: Operand mismatch at C.");
//        }
        if (!compare2.test(opP.operand2, opQ.operand1)) {
          throw new IllegalDiffException("Composition Error: Operand mismatch at C.");
        }

        result.addAll(mergeInsertionSlot(ins1, ins2));
        ins1.clear();
        ins2.clear();

//        result.add(new InsnListDiff.Operation(opQ.type, opQ.mode, opP.operand));
        result.add(new ListDiff.Operation<>(opQ.type, opQ.mode, opP.operand1, opQ.operand2));
      }
    }

    ins2.addAll(collectInsertions(itQ));
    result.addAll(mergeInsertionSlot(ins1, ins2));

    IteratorHelper.throwIfNext(itQ, () -> new IllegalDiffException("Composition Error: q has remaining operations after p is exhausted."));

    return new ListDiff<>(result);
  }
}
