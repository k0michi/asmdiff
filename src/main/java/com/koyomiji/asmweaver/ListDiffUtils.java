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
          invertedOp = new ListDiff.Operation<>(ListDiff.Operation.Type.MATCH, null, op.operand);
          break;
        case INSERT:
          invertedOp = new ListDiff.Operation<>(ListDiff.Operation.Type.DELETE, op.mode, op.operand);
          break;
        case DELETE:
          invertedOp = new ListDiff.Operation<>(ListDiff.Operation.Type.INSERT, op.mode, op.operand);
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
        qPrimeOps.add(new ListDiff.Operation<>(ListDiff.Operation.Type.MATCH, opP.mode, opP.operand));
        pPrimeOps.add(new ListDiff.Operation<>(ListDiff.Operation.Type.DELETE, opP.mode, opP.operand));
      } else if (opP.type == ListDiff.Operation.Type.MATCH || opP.type == ListDiff.Operation.Type.INSERT) {

        while (itQ.hasNext() && itQ.peek().type == ListDiff.Operation.Type.INSERT) {
          ListDiff.Operation<T> opQIns = itQ.next();

          qPrimeOps.add(opQIns);
          pPrimeOps.add(new ListDiff.Operation<>(ListDiff.Operation.Type.MATCH, opQIns.mode, opQIns.operand));
        }

        if (!itQ.hasNext()) {
          throw new IllegalDiffException("p has remaining operations after q is exhausted");
        }
        ListDiff.Operation<T> opQBase = itQ.next();

        if (!compare.test(opP.operand, opQBase.operand)) {
          throw new IllegalDiffException("p and q disagree on node identity");
        }

        if (opP.type == ListDiff.Operation.Type.MATCH) {
          qPrimeOps.add(opQBase);
          if (opQBase.type == ListDiff.Operation.Type.MATCH) {
            pPrimeOps.add(new ListDiff.Operation<>(ListDiff.Operation.Type.MATCH, opP.mode, opP.operand));
          }
        } else {
          if (opQBase.type == ListDiff.Operation.Type.DELETE) {
            throw new ConflictException("p inserts a node that q deletes");
          }

          pPrimeOps.add(new ListDiff.Operation<>(opP.type, opP.mode, opP.operand));
        }
      }
    }

    while (itQ.hasNext()) {
      ListDiff.Operation<T> opQ = itQ.next();

      if (opQ.type == ListDiff.Operation.Type.INSERT) {
        qPrimeOps.add(opQ);
        pPrimeOps.add(new ListDiff.Operation<>(ListDiff.Operation.Type.MATCH, opQ.mode, opQ.operand));
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
        operations.add(new ListDiff.Operation<>(ListDiff.Operation.Type.MATCH, null, list1.get(i - 1)));
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
        operations.add(new ListDiff.Operation<>(ListDiff.Operation.Type.INSERT, null, list2.get(j - 1)));
        j--;
      } else {
        operations.add(new ListDiff.Operation<>(ListDiff.Operation.Type.DELETE, null, list1.get(i - 1)));
        i--;
      }
    }

    while (i > 0) {
      operations.add(new ListDiff.Operation<>(ListDiff.Operation.Type.DELETE, null, list1.get(i - 1)));
      i--;
    }

    while (j > 0) {
      operations.add(new ListDiff.Operation<>(ListDiff.Operation.Type.INSERT, null, list2.get(j - 1)));
      j--;
    }

    List<ListDiff.Operation<T>> reversedOperations = new ArrayList<>();

    for (int k = operations.size() - 1; k >= 0; k--) {
      reversedOperations.add(operations.get(k));
    }

    return new ListDiff<>(reversedOperations);
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
          result.add(op.operand);
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

  public static <T> T patchNullable(T element, ListDiff<T> diff) throws IllegalDiffException {
    List<T> patched = patch(ListHelper.ofNullable(element), diff);

    if (patched.size() > 1) {
      throw new IllegalDiffException("Diff results in multiple elements, expected at most one");
    }

    return patched.isEmpty() ? null : patched.get(0);
  }

  public static <T> T patchNonNullable(T element, ListDiff<T> diff) throws IllegalDiffException {
    List<T> patched = patch(ListHelper.ofNonNullable(element), diff);

    if (patched.size() != 1) {
      throw new IllegalDiffException("Diff results in zero or multiple elements, expected exactly one");
    }

    return patched.get(0);
  }
}
