package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.CustomDataInput;
import com.koyomiji.asmweaver.io.CustomDataOutput;
import com.koyomiji.asmweaver.util.PeekableIterator;
import com.koyomiji.asmweaver.util.tuple.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class ListDiffUtils {
  public static <T> ListDiff<T> unchangedToNull(ListDiff<T> diff) {
    boolean isEmpty = true;

    for (ListDiff.Operation<T> op : diff.operations) {
      if (op.type != ListDiff.Operation.Type.MATCH
      ) {
        isEmpty = false;
        break;
      }
    }

    if (isEmpty) {
      return null;
    }

    return diff;
  }

  public static <T> ListDiff<T> invert(ListDiff<T> diff) {
    if (diff == null) {
      return null;
    }

    List<ListDiff.Operation<T>> invertedOperations = new ArrayList<>();

    for (ListDiff.Operation<T> op : diff.operations) {
      ListDiff.Operation<T> invertedOp;

      switch (op.type) {
        case MATCH:
          invertedOp = new ListDiff.Operation<>(ListDiff.Operation.Type.MATCH, op.mode, op.operand);
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
    if (p == null || q == null) {
      return Pair.of(q, p);
    }

    List<ListDiff.Operation<T>> qPrimeOps = new ArrayList<>();
    List<ListDiff.Operation<T>> pPrimeOps = new ArrayList<>();

    ListDiffPairIterator<T> it = new ListDiffPairIterator<>(p, q);

    while (it.hasNext()) {
      Pair<ListDiff.Operation<T>, ListDiff.Operation<T>> pair = it.next();
      ListDiff.Operation<T> opP = pair.first;
      ListDiff.Operation<T> opQ = pair.second;

      if (opP != null && opP.type == ListDiff.Operation.Type.DELETE) {
        // DELETEの場合、operand1が対象。q'ではその要素をMATCH（維持）させる
        qPrimeOps.add(new ListDiff.Operation<>(ListDiff.Operation.Type.MATCH, opP.mode, opP.operand));
        pPrimeOps.add(new ListDiff.Operation<>(ListDiff.Operation.Type.DELETE, opP.mode, opP.operand));
      } else if (opQ != null && opQ.type == ListDiff.Operation.Type.INSERT) {
        qPrimeOps.add(opQ);
        // qが挿入した要素を、p'側ではMATCH（維持）として扱う
        pPrimeOps.add(new ListDiff.Operation<>(ListDiff.Operation.Type.MATCH, opQ.mode, opQ.operand));
      } else {
        // opP が MATCH または INSERT、opQ が MATCH または DELETE の1対1対応
        if (opP.type == ListDiff.Operation.Type.MATCH) {
          qPrimeOps.add(opQ);
          if (opQ.type == ListDiff.Operation.Type.MATCH) {
            pPrimeOps.add(new ListDiff.Operation<>(ListDiff.Operation.Type.MATCH, opP.mode, opP.operand));
          }
        } else {
          // opP.type == INSERT
          if (opQ.type == ListDiff.Operation.Type.DELETE) {
            throw new ConflictException("p inserts a node that q deletes");
          }
          // pが挿入しようとしている要素をp'でもそのまま挿入
          pPrimeOps.add(new ListDiff.Operation<>(ListDiff.Operation.Type.INSERT, opP.mode, opP.operand));
        }
      }
    }

    return Pair.of(new ListDiff<>(qPrimeOps), new ListDiff<>(pPrimeOps));
  }

  // FIXME: Myers
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
        operations.add(new ListDiff.Operation<>(ListDiff.Operation.Type.MATCH, ListDiff.Operation.Mode.BETWEEN, null));
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
        operations.add(new ListDiff.Operation<>(ListDiff.Operation.Type.INSERT, ListDiff.Operation.Mode.BETWEEN, list2.get(j - 1)));
        j--;
      } else {
//        operations.add(new ListDiff.Operation<>(ListDiff.Operation.Type.DELETE, null, list1.get(i - 1)));
        operations.add(new ListDiff.Operation<>(ListDiff.Operation.Type.DELETE, ListDiff.Operation.Mode.BETWEEN, null));
        i--;
      }
    }

    while (i > 0) {
//      operations.add(new ListDiff.Operation<>(ListDiff.Operation.Type.DELETE, null, list1.get(i - 1)));
      operations.add(new ListDiff.Operation<>(ListDiff.Operation.Type.DELETE, ListDiff.Operation.Mode.BETWEEN, null));
      i--;
    }

    while (j > 0) {
//      operations.add(new ListDiff.Operation<>(ListDiff.Operation.Type.INSERT, null, list2.get(j - 1)));
      operations.add(new ListDiff.Operation<>(ListDiff.Operation.Type.INSERT, ListDiff.Operation.Mode.BETWEEN, list2.get(j - 1)));
      j--;
    }

    List<ListDiff.Operation<T>> reversedOperations = new ArrayList<>();

    for (int k = operations.size() - 1; k >= 0; k--) {
      reversedOperations.add(operations.get(k));
    }

    return unchangedToNull(new ListDiff<>(reversedOperations));
  }

  public static <T> ListDiff<T> diffNullableValue(T element1, T element2, BiPredicate<T, T> compare) {
    return diff(ListHelper.ofNullable(element1), ListHelper.ofNullable(element2), compare);
  }

  public static <T> ListDiff<T> diffNonNullableValue(T element1, T element2, BiPredicate<T, T> compare) {
    return diff(ListHelper.ofNonNullable(element1), ListHelper.ofNonNullable(element2), compare);
  }

  public static <T> List<T> patch(List<T> list, ListDiff<T> diff) {
    if (diff == null) {
      return list;
    }

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

  private static <T> List<ListDiff.Operation<T>> mergeInsertionSlot(List<ListDiff.Operation<T>> ins1, List<ListDiff.Operation<T>> ins2) {
    List<ListDiff.Operation<T>> result = new ArrayList<>();
    result.addAll(ins1);
    result.addAll(ins2);
    return result;
  }

  private static <T> List<ListDiff.Operation<T>> collectInsertions(PeekableIterator<ListDiff.Operation<T>> it) {
    List<ListDiff.Operation<T>> insertions = new ArrayList<>();

    while (it.hasNext() && it.peek().type == ListDiff.Operation.Type.INSERT) {
      insertions.add(it.next());
    }

    return insertions;
  }

  public static <T> ListDiff<T> compose(ListDiff<T> p, ListDiff<T> q, BiPredicate<T, T> compare2) {
    if (p == null) {
      return q;
    }

    if (q == null) {
      return p;
    }

    List<ListDiff.Operation<T>> result = new ArrayList<>();

    ListDiffPairIterator<T> it = new ListDiffPairIterator<>(p, q);

    while (it.hasNext()) {
      Pair<ListDiff.Operation<T>, ListDiff.Operation<T>> pair = it.next();
      ListDiff.Operation<T> opP = pair.first;
      ListDiff.Operation<T> opQ = pair.second;

      // パターン1: P が単独で DELETE
      if (opP != null && opQ == null) {
        result.add(opP);
      }
      // パターン2: Q が単独で INSERT
      else if (opP == null && opQ != null) {
        result.add(opQ);
      }
      // パターン3: 両方のタイムラインが揃った
      else if (opP != null && opQ != null) {
//        if (!compare2.test(opP.operand, opQ.operand)) {
//          throw new IllegalDiffException("Composition Error: Operand mismatch at intermediate state B.");
//        }

        if (opP.type == ListDiff.Operation.Type.INSERT) {
          // PのINSERTをQがMATCHで通したなら、ここでPのINSERT（X）がresultに積まれる
          if (opQ.type == ListDiff.Operation.Type.MATCH) {
            result.add(opP);
          }
          // QがDELETEなら相殺されてresultには何も入らない
        } else { // opP.type == MATCH
          // ベース操作同士の合成
          result.add(new ListDiff.Operation<>(opQ.type, opQ.mode, opP.operand));
        }
      }
    }

    return unchangedToNull(new ListDiff<>(result));
  }

  public static <T> ListDiff<T> mapOperands(ListDiff<T> diff, Function<T, T> mapper) {
    if (diff == null) {
      return null;
    }

    List<ListDiff.Operation<T>> mappedOps = new ArrayList<>();

    for (ListDiff.Operation<T> op : diff.operations) {
      T mappedOperand = (op.operand != null) ? mapper.apply(op.operand) : null;
      mappedOps.add(new ListDiff.Operation<>(op.type, op.mode, mappedOperand));
    }

    return new ListDiff<>(mappedOps);
  }

  public static <T> void write(ListDiff<T> diff, CustomDataOutput out, ListHelper.ElementWriter<T> elementWriter) throws IOException {
    out.writeBoolean(diff == null);

    if (diff == null) {
      return;
    }

    ListHelper.write(
            diff.operations,
            out,
            (element, stream) -> {
              stream.writeByte(element.type.ordinal());
//              stream.writeByte(element.mode.ordinal());
              NullableHelper.write(element.operand, stream, elementWriter);
            }
    );
  }

  public static <T> ListDiff<T> read(CustomDataInput in, ListHelper.ElementReader<T> elementReader) throws IOException {
    if (in.readBoolean()) {
      return null;
    }

    List<ListDiff.Operation<T>> ops = ListHelper.read(
            in,
            stream -> new ListDiff.Operation<>(
                    ListDiff.Operation.Type.values()[stream.readByte()],
//                    ListDiff.Operation.Mode.values()[stream.readByte()],
                    ListDiff.Operation.Mode.BETWEEN,
                    NullableHelper.read(stream, elementReader)
            )
    );

    return new ListDiff<>(ops);
  }

  /**
   * Merge diff1 and diff2.
   * That is, this computes $AB'$ where
   * $$
   * AA^{-1}B\leftrightarrow AB'{A^{-1}}'
   * $$
   *
   * @param diff1
   * @param diff2
   * @param compare
   * @param <T>
   * @return
   * @throws ConflictException
   */
  public static <T> ListDiff<T> merge(ListDiff<T> diff1, ListDiff<T> diff2, BiPredicate<T, T> compare) throws ConflictException {
    ListDiff<T> diff1Inv = ListDiffUtils.invert(diff1);
    ListDiff<T> diff2Prime = ListDiffUtils.commute(diff1Inv, diff2, compare).first;
    return ListDiffUtils.compose(diff1, diff2Prime, compare);
  }

  public static <T> int distance(ListDiff<T> diff) {
    if (diff == null) {
      return 0;
    }

    int distance = 0;

    for (ListDiff.Operation<T> op : diff.operations) {
      if (op.type != ListDiff.Operation.Type.MATCH) {
        distance++;
      }
    }

    return distance;
  }
}
