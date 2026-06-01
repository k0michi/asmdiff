package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.CustomDataInput;
import com.koyomiji.asmweaver.io.CustomDataOutput;
import com.koyomiji.asmweaver.util.PeekableIterator;
import com.koyomiji.asmweaver.util.tuple.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class KeyedListDiffUtils {
  public static <Key, Value, Diff> KeyedListDiff<Key, Value, Diff> unchangedToNull(KeyedListDiff<Key, Value, Diff> diff) {
    boolean isEmpty = true;

    for (KeyedListDiff.Operation<Key, Value, Diff> op : diff.operations) {
      if (op.type != KeyedListDiff.Operation.Type.MATCH || op.operandDiff != null) {
        isEmpty = false;
        break;
      }
    }

    if (isEmpty) {
      return null;
    }

    return diff;
  }

  public static <Key, Value, Diff> KeyedListDiff<Key, Value, Diff> invert(KeyedListDiff<Key, Value, Diff> diff, Function<Diff, Diff> invert) {
    if (diff == null) {
      return null;
    }

    List<KeyedListDiff.Operation<Key, Value, Diff>> invertedOperations = new ArrayList<>();

    for (KeyedListDiff.Operation<Key, Value, Diff> op : diff.operations) {
      switch (op.type) {
        case MATCH:
          invertedOperations.add(new KeyedListDiff.Operation<>(KeyedListDiff.Operation.Type.MATCH, op.mode, op.operandKey, op.operandValue, invert.apply(op.operandDiff)));
          break;
        case INSERT:
          invertedOperations.add(new KeyedListDiff.Operation<>(KeyedListDiff.Operation.Type.DELETE, op.mode, op.operandKey, op.operandValue, null));
          break;
        case DELETE:
          invertedOperations.add(new KeyedListDiff.Operation<>(KeyedListDiff.Operation.Type.INSERT, op.mode, op.operandKey, op.operandValue, null));
          break;
      }
    }

    return new KeyedListDiff<>(invertedOperations);
  }

  // FIXME: Myers
  public static <Key, Value, Diff> KeyedListDiff<Key, Value, Diff> diff(List<Value> list1, List<Value> list2, Function<Value, Key> keyExtractor, BiFunction<Value, Value, Diff> diffFunction) {
    int[][] dp = new int[list1.size() + 1][list2.size() + 1];

    for (int i = 0; i <= list1.size(); i++) {
      dp[i][0] = i;
    }

    for (int j = 0; j <= list2.size(); j++) {
      dp[0][j] = j;
    }

    for (int i = 1; i <= list1.size(); i++) {
      for (int j = 1; j <= list2.size(); j++) {
        if (keyExtractor.apply(list1.get(i - 1)).equals(keyExtractor.apply(list2.get(j - 1)))) {
          dp[i][j] = dp[i - 1][j - 1];
        } else {
          dp[i][j] = Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1);
        }
      }
    }

    List<KeyedListDiff.Operation<Key, Value, Diff>> operations = new ArrayList<>();

    int i = list1.size();
    int j = list2.size();

    while (i > 0 && j > 0) {
      if (keyExtractor.apply(list1.get(i - 1)).equals(keyExtractor.apply(list2.get(j - 1)))) {
        operations.add(new KeyedListDiff.Operation<>(KeyedListDiff.Operation.Type.MATCH, KeyedListDiff.Operation.Mode.BETWEEN, null, null, diffFunction.apply(list1.get(i - 1), list2.get(j - 1))));
        i--;
        j--;
      } else if (dp[i][j] == dp[i][j - 1] + 1) {
        operations.add(new KeyedListDiff.Operation<>(KeyedListDiff.Operation.Type.INSERT, KeyedListDiff.Operation.Mode.BETWEEN, null, list2.get(j - 1), null));
        j--;
      } else {
        operations.add(new KeyedListDiff.Operation<>(KeyedListDiff.Operation.Type.DELETE, KeyedListDiff.Operation.Mode.BETWEEN, null, null, null));
        i--;
      }
    }

    while (i > 0) {
      operations.add(new KeyedListDiff.Operation<>(KeyedListDiff.Operation.Type.DELETE, KeyedListDiff.Operation.Mode.BETWEEN, null, null, null));
      i--;
    }

    while (j > 0) {
      operations.add(new KeyedListDiff.Operation<>(KeyedListDiff.Operation.Type.INSERT, KeyedListDiff.Operation.Mode.BETWEEN, null, list2.get(j - 1), null));
      j--;
    }

    List<KeyedListDiff.Operation<Key, Value, Diff>> reversedOperations = new ArrayList<>();

    for (int k = operations.size() - 1; k >= 0; k--) {
      reversedOperations.add(operations.get(k));
    }

    return unchangedToNull(new KeyedListDiff<>(reversedOperations));
  }

  public static <Value, Diff> KeyedListDiff<Integer, Value, Diff> diffIndexed(
          List<Value> list1,
          List<Value> list2,
          BiFunction<Value, Value, Diff> diffFunction
  ) {
    List<Pair<Integer, Value>> wrapped1 = new ArrayList<>();

    for (int i = 0; i < list1.size(); i++) {
      wrapped1.add(Pair.of(i, list1.get(i)));
    }

    List<Pair<Integer, Value>> wrapped2 = new ArrayList<>();

    for (int i = 0; i < list2.size(); i++) {
      wrapped2.add(Pair.of(i, list2.get(i)));
    }

    KeyedListDiff<Integer, Pair<Integer, Value>, Diff> pairedDiff = diff(
            wrapped1,
            wrapped2,
            p -> p.first,
            (p1, p2) -> diffFunction.apply(p1.second, p2.second)
    );

    if (pairedDiff == null) {
      return null;
    }

    List<KeyedListDiff.Operation<Integer, Value, Diff>> unwrappedOps = new ArrayList<>();

    for (KeyedListDiff.Operation<Integer, Pair<Integer, Value>, Diff> op : pairedDiff.operations) {
      unwrappedOps.add(new KeyedListDiff.Operation<>(
              op.type,
              op.mode,
              op.operandKey,
              op.operandValue != null ? op.operandValue.second : null,
              op.operandDiff
      ));
    }

    return new KeyedListDiff<>(unwrappedOps);
  }

  public static <Value, Diff> KeyedListDiff<Integer, Value, Diff> diffNullableValue(
          Value value1,
          Value value2,
          BiFunction<Value, Value, Diff> diffFunction
  ) {
    return diffIndexed(
            ListHelper.ofNullable(value1),
            ListHelper.ofNullable(value2),
            diffFunction
    );
  }

  public static <Key, Value, Diff> List<Value> patch(List<Value> original, KeyedListDiff<Key, Value, Diff> diff, BiFunction<Value, Diff, Value> elementPatch) {
    if (diff == null) {
      return original;
    }

    List<Value> result = new ArrayList<>();
    int i = 0;

    for (KeyedListDiff.Operation<Key, Value, Diff> op : diff.operations) {
      switch (op.type) {
        case MATCH:
          result.add(elementPatch.apply(original.get(i), op.operandDiff));
          i++;
          break;
        case INSERT:
          result.add(op.operandValue);
          break;
        case DELETE:
          i++;
          break;
      }
    }

    return result;
  }

  public static <Value, Diff> Value patchNullableValue(Value original, KeyedListDiff<Integer, Value, Diff> diff, BiFunction<Value, Diff, Value> elementPatch) {
    return ListHelper.getOrNull(
            patch(
                    ListHelper.ofNullable(original),
                    diff,
                    elementPatch
            ), 0);
  }

  public static <Key, Value, Diff> void write(
          KeyedListDiff<Key, Value, Diff> diff,
          CustomDataOutput out,
          ListHelper.ElementWriter<Key> keyWriter,
          ListHelper.ElementWriter<Value> valueWriter,
          ListHelper.ElementWriter<Diff> diffWriter
  ) throws IOException {
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
//              keyWriter.write(element.operandKey, stream);
              NullableHelper.write(element.operandValue, stream, valueWriter);
              NullableHelper.write(element.operandDiff, stream, diffWriter);
            }
    );
  }

  public static <Key, Value, Diff> KeyedListDiff<Key, Value, Diff> read(
          CustomDataInput in,
          ListHelper.ElementReader<Key> keyReader,
          ListHelper.ElementReader<Value> valueReader,
          ListHelper.ElementReader<Diff> diffReader
  ) throws IOException {
    if (in.readBoolean()) {
      return null;
    }

    List<KeyedListDiff.Operation<Key, Value, Diff>> ops = ListHelper.read(
            in,
            stream -> {
              return new KeyedListDiff.Operation<>(
                      KeyedListDiff.Operation.Type.values()[stream.readByte()],
//                      KeyedListDiff.Operation.Mode.values()[stream.readByte()],
                      KeyedListDiff.Operation.Mode.BETWEEN,
//                      keyReader.read(stream),
                      null,
                      NullableHelper.read(stream, valueReader),
                      NullableHelper.read(stream, diffReader)
              );
            }
    );

    return new KeyedListDiff<>(ops);
  }

  public static <Key, Value, Diff> Pair<KeyedListDiff<Key, Value, Diff>, KeyedListDiff<Key, Value, Diff>> commute(
          KeyedListDiff<Key, Value, Diff> p,
          KeyedListDiff<Key, Value, Diff> q,
          CommuteFunction<Diff> commuteDiff,
          BiFunction<Value, Value, Diff> diffFunction
  ) throws ConflictException {
    if (p == null || q == null) {
      return Pair.of(q, p);
    }

    List<KeyedListDiff.Operation<Key, Value, Diff>> qPrimeOps = new ArrayList<>();
    List<KeyedListDiff.Operation<Key, Value, Diff>> pPrimeOps = new ArrayList<>();

    KeyedListDiffPairIterator<Key, Value, Diff> it = new KeyedListDiffPairIterator<>(p, q);

    while (it.hasNext()) {
      Pair<KeyedListDiff.Operation<Key, Value, Diff>, KeyedListDiff.Operation<Key, Value, Diff>> pair = it.next();
      KeyedListDiff.Operation<Key, Value, Diff> opP = pair.first;
      KeyedListDiff.Operation<Key, Value, Diff> opQ = pair.second;

      if (opP != null && opQ == null) {
        // p が単独で DELETE
        Diff emptyDiff = diffFunction.apply(opP.operandValue, opP.operandValue);
        qPrimeOps.add(new KeyedListDiff.Operation<>(KeyedListDiff.Operation.Type.MATCH, opP.mode, opP.operandKey, null, emptyDiff));
        pPrimeOps.add(new KeyedListDiff.Operation<>(KeyedListDiff.Operation.Type.DELETE, opP.mode, opP.operandKey, opP.operandValue, null));
      } else if (opP == null && opQ != null) {
        // q が単独で INSERT
        qPrimeOps.add(opQ);
        Diff emptyDiff = diffFunction.apply(opQ.operandValue, opQ.operandValue);
        pPrimeOps.add(new KeyedListDiff.Operation<>(KeyedListDiff.Operation.Type.MATCH, opQ.mode, opQ.operandKey, null, emptyDiff));
      } else {
        // 両方のタイムラインが揃った
//      if (!opP.operandKey.equals(opQ.operandKey)) {
//        throw new IllegalDiffException("p and q disagree on node identity");
//      }

        if (opP.type == KeyedListDiff.Operation.Type.MATCH) {
          if (opQ.type == KeyedListDiff.Operation.Type.MATCH) {
            // MATCH -> MATCH のパターン：双方の内部変更を交換
            Pair<Diff, Diff> commuted = commuteDiff.commute(opP.operandDiff, opQ.operandDiff);
            qPrimeOps.add(new KeyedListDiff.Operation<>(KeyedListDiff.Operation.Type.MATCH, opQ.mode, opQ.operandKey, null, commuted.first));
            pPrimeOps.add(new KeyedListDiff.Operation<>(KeyedListDiff.Operation.Type.MATCH, opP.mode, opP.operandKey, null, commuted.second));
          } else if (opQ.type == KeyedListDiff.Operation.Type.DELETE) {
            // change -> delete のパターン：交換不可（Conflict）
            throw new ConflictException("Conflict: p modifies a node that q subsequently deletes (Key: " + opP.operandKey + ")");
          }
        } else {
          // opP.type == INSERT
          if (opQ.type == KeyedListDiff.Operation.Type.DELETE) {
            // insert -> delete のパターン：交換不可（Conflict）
            throw new ConflictException("Conflict: p inserts a node that q subsequently deletes (Key: " + opP.operandKey + ")");
          }

          if (opQ.operandDiff != null) {
            // insert -> change のパターン：依存関係があるため交換不可（Conflict）
            throw new ConflictException("Conflict: p inserts a node that q subsequently modifies (Key: " + opP.operandKey + ")");
          }

          // insert -> match（空の差分）のパターン：安全に交換可能
          pPrimeOps.add(new KeyedListDiff.Operation<>(KeyedListDiff.Operation.Type.INSERT, opP.mode, opP.operandKey, opP.operandValue, null));
        }
      }
    }

    return Pair.of(new KeyedListDiff<>(qPrimeOps), new KeyedListDiff<>(pPrimeOps));
  }

  private static <Key, Value, Diff> List<KeyedListDiff.Operation<Key, Value, Diff>> collectInsertions(PeekableIterator<KeyedListDiff.Operation<Key, Value, Diff>> it) {
    List<KeyedListDiff.Operation<Key, Value, Diff>> insertions = new ArrayList<>();

    while (it.hasNext() && it.peek().type == KeyedListDiff.Operation.Type.INSERT) {
      insertions.add(it.next());
    }

    return insertions;
  }

  private static <Key, Value, Diff> List<KeyedListDiff.Operation<Key, Value, Diff>> mergeInsertionSlot(
          List<KeyedListDiff.Operation<Key, Value, Diff>> ins1,
          List<KeyedListDiff.Operation<Key, Value, Diff>> ins2
  ) {
    List<KeyedListDiff.Operation<Key, Value, Diff>> merged = new ArrayList<>(ins1);
    merged.addAll(ins2);
    return merged;
  }

  private static <Key, Value, Diff> void validateKey(
          KeyedListDiff.Operation<Key, Value, Diff> opP,
          KeyedListDiff.Operation<Key, Value, Diff> opQ
  ) {
    if (!opP.operandKey.equals(opQ.operandKey)) {
      throw new IllegalDiffException("Composition Error: Operand key mismatch at intermediate B.");
    }
  }

  public static <Key, Value, Diff> KeyedListDiff<Key, Value, Diff> compose(
          KeyedListDiff<Key, Value, Diff> p,
          KeyedListDiff<Key, Value, Diff> q,
          BiFunction<Diff, Diff, Diff> composeDiff,
          BiFunction<Value, Diff, Value> applyDiff,
          Function<Diff, Diff> invertDiff
  ) {
    if (p == null) {
      return q;
    }

    if (q == null) {
      return p;
    }

    List<KeyedListDiff.Operation<Key, Value, Diff>> result = new ArrayList<>();

    KeyedListDiffPairIterator<Key, Value, Diff> it = new KeyedListDiffPairIterator<>(p, q);

    while (it.hasNext()) {
      Pair<KeyedListDiff.Operation<Key, Value, Diff>, KeyedListDiff.Operation<Key, Value, Diff>> pair = it.next();
      KeyedListDiff.Operation<Key, Value, Diff> opP = pair.first;
      KeyedListDiff.Operation<Key, Value, Diff> opQ = pair.second;

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
//      validateKey(opP, opQ);

        if (opP.type == KeyedListDiff.Operation.Type.INSERT) {
          if (opQ.type == KeyedListDiff.Operation.Type.MATCH) {
            // insert -> match のパターン
            Value newValue = applyDiff.apply(opP.operandValue, opQ.operandDiff);
            result.add(new KeyedListDiff.Operation<>(
                    KeyedListDiff.Operation.Type.INSERT,
                    opP.mode,
                    opP.operandKey,
                    newValue,
                    null
            ));
          }
          // opQ.type == DELETE の場合は消滅
        } else { // opP.type == MATCH
          if (opQ.type == KeyedListDiff.Operation.Type.MATCH) {
            // match -> match のパターン
            Diff combinedDiff = composeDiff.apply(opP.operandDiff, opQ.operandDiff);
            result.add(new KeyedListDiff.Operation<>(
                    KeyedListDiff.Operation.Type.MATCH,
                    opQ.mode,
                    opP.operandKey,
                    null,
                    combinedDiff
            ));
          } else if (opQ.type == KeyedListDiff.Operation.Type.DELETE) {
            // match -> delete のパターン
            Value valueA = applyDiff.apply(opQ.operandValue, invertDiff.apply(opP.operandDiff));
            result.add(new KeyedListDiff.Operation<>(
                    KeyedListDiff.Operation.Type.DELETE,
                    opQ.mode,
                    opP.operandKey,
                    valueA,
                    null
            ));
          }
        }
      }
    }

    return unchangedToNull(new KeyedListDiff<>(result));
  }

  public static <Key, Value, Diff> int distance(KeyedListDiff<Key, Value, Diff> diff, Function<Diff, Integer> diffDistance) {
    if (diff == null) {
      return 0;
    }

    int distance = 0;

    for (KeyedListDiff.Operation<Key, Value, Diff> op : diff.operations) {
      if (op.type != KeyedListDiff.Operation.Type.MATCH) {
        distance++;
      } else {
        distance += diffDistance.apply(op.operandDiff);
      }
    }

    return distance;
  }
}
