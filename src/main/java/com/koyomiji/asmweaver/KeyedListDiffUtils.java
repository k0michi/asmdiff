package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.CustomDataInput;
import com.koyomiji.asmweaver.io.CustomDataOutput;
import com.koyomiji.asmweaver.util.PeekableIterator;
import com.koyomiji.asmweaver.util.tuple.Pair;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class KeyedListDiffUtils {
  public static <Key, Value, Diff extends IDiff> KeyedListDiff<Key, Value, Diff> invert(KeyedListDiff<Key, Value, Diff> diff, Function<Diff, Diff> invert) {
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
  public static <Key, Value, Diff extends IDiff> KeyedListDiff<Key, Value, Diff> diff(List<Value> list1, List<Value> list2, Function<Value, Key> keyExtractor, BiFunction<Value, Value, Diff> diffFunction) {
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
        operations.add(new KeyedListDiff.Operation<>(KeyedListDiff.Operation.Type.MATCH, KeyedListDiff.Operation.Mode.BETWEEN, keyExtractor.apply(list1.get(i - 1)), null, diffFunction.apply(list1.get(i - 1), list2.get(j - 1))));
        i--;
        j--;
      } else if (dp[i][j] == dp[i][j - 1] + 1) {
        operations.add(new KeyedListDiff.Operation<>(KeyedListDiff.Operation.Type.INSERT, KeyedListDiff.Operation.Mode.BETWEEN, keyExtractor.apply(list2.get(j - 1)), list2.get(j - 1), null));
        j--;
      } else {
        operations.add(new KeyedListDiff.Operation<>(KeyedListDiff.Operation.Type.DELETE, KeyedListDiff.Operation.Mode.BETWEEN, keyExtractor.apply(list1.get(i - 1)), list1.get(i - 1), null));
        i--;
      }
    }

    while (i > 0) {
      operations.add(new KeyedListDiff.Operation<>(KeyedListDiff.Operation.Type.DELETE, KeyedListDiff.Operation.Mode.BETWEEN, keyExtractor.apply(list1.get(i - 1)), list1.get(i - 1), null));
      i--;
    }

    while (j > 0) {
      operations.add(new KeyedListDiff.Operation<>(KeyedListDiff.Operation.Type.INSERT, KeyedListDiff.Operation.Mode.BETWEEN, keyExtractor.apply(list2.get(j - 1)), list2.get(j - 1), null));
      j--;
    }

    List<KeyedListDiff.Operation<Key, Value, Diff>> reversedOperations = new ArrayList<>();

    for (int k = operations.size() - 1; k >= 0; k--) {
      reversedOperations.add(operations.get(k));
    }

    return new KeyedListDiff<>(reversedOperations);
  }

  // FIXME: breaks if duplicate values
  public static <Value, Diff extends IDiff> KeyedListDiff<Integer, Value, Diff> diffIndexed(List<Value> list1, List<Value> list2, BiFunction<Value, Value, Diff> diffFunction) {
    Map<Value, Integer> indexMap = new HashMap<>();

    for (int i = 0; i < list1.size(); i++) {
      indexMap.put(list1.get(i), i);
    }

    for  (int i = 0; i < list2.size(); i++) {
      indexMap.put(list2.get(i), i);
    }

    return diff(
            list1,
            list2,
            indexMap::get,
            diffFunction
    );
  }

  public static <Key, Value, Diff extends IDiff> List<Value> patch(List<Value> original, KeyedListDiff<Key, Value, Diff> diff, BiFunction<Value, Diff, Value> elementPatch) {
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

  public static <Key, Value, Diff extends IDiff> void write(
          KeyedListDiff<Key, Value, Diff> diff,
          CustomDataOutput out,
          ListHelper.ElementWriter<Key> keyWriter,
          ListHelper.ElementWriter<Value> valueWriter,
          ListHelper.ElementWriter<Diff> diffWriter
  ) throws IOException {
    out.writeInt(diff.operations.size());

    for (KeyedListDiff.Operation<Key, Value, Diff> op : diff.operations) {
      out.writeInt(op.type.ordinal());
      out.writeInt(op.mode.ordinal());
      keyWriter.write(op.operandKey, out);
      NullableHelper.write(
              op.operandValue,
              out,
              valueWriter
      );
      NullableHelper.write(
              op.operandDiff,
              out,
              diffWriter
      );
    }
  }

  public static <Key, Value, Diff extends IDiff> KeyedListDiff<Key, Value, Diff> read(
          CustomDataInput in,
          ListHelper.ElementReader<Key> keyReader,
          ListHelper.ElementReader<Value> valueReader,
          ListHelper.ElementReader<Diff> diffReader
  ) throws IOException {
    int size = in.readInt();
    List<KeyedListDiff.Operation<Key, Value, Diff>> operations = new ArrayList<>();

    for (int i = 0; i < size; i++) {
      int typeOrdinal = in.readInt();
      int modeOrdinal = in.readInt();
      Key key = keyReader.read(in);
      Value value = NullableHelper.read(in, valueReader);
      Diff diff = NullableHelper.read(in, diffReader);
      operations.add(
              new KeyedListDiff.Operation<>(
                      KeyedListDiff.Operation.Type.values()[typeOrdinal],
                      KeyedListDiff.Operation.Mode.values()[modeOrdinal],
                      key,
                      value,
                      diff
              )
      );
    }

    return new KeyedListDiff<>(operations);
  }

  public static <Key, Value, Diff extends IDiff> Pair<KeyedListDiff<Key, Value, Diff>, KeyedListDiff<Key, Value, Diff>> commute(
          KeyedListDiff<Key, Value, Diff> p,
          KeyedListDiff<Key, Value, Diff> q,
          CommuteFunction<Diff> commuteDiff,
          BiFunction<Value, Value, Diff> diffFunction
  ) throws ConflictException {
    List<KeyedListDiff.Operation<Key, Value, Diff>> qPrimeOps = new ArrayList<>();
    List<KeyedListDiff.Operation<Key, Value, Diff>> pPrimeOps = new ArrayList<>();

    Iterator<KeyedListDiff.Operation<Key, Value, Diff>> itP = p.operations.iterator();
    PeekableIterator<KeyedListDiff.Operation<Key, Value, Diff>> itQ = new PeekableIterator<>(q.operations.iterator());

    while (itP.hasNext()) {
      KeyedListDiff.Operation<Key, Value, Diff> opP = itP.next();

      if (opP.type == KeyedListDiff.Operation.Type.DELETE) {
        // p が DELETE の場合：
        // 後行 q 側にはこの要素への操作はないため、q' 側では空の MATCH を生成し、p' 側で DELETE を引き継ぐ
        Diff emptyDiff = diffFunction.apply(opP.operandValue, opP.operandValue);
        qPrimeOps.add(new KeyedListDiff.Operation<>(KeyedListDiff.Operation.Type.MATCH, opP.mode, opP.operandKey, null, emptyDiff));
        pPrimeOps.add(new KeyedListDiff.Operation<>(KeyedListDiff.Operation.Type.DELETE, opP.mode, opP.operandKey, opP.operandValue, null));
      } else {
        // opP が MATCH または INSERT の場合

        // q 側で新規に挿入される要素(INSERT)を優先して回収
        while (itQ.hasNext() && itQ.peek().type == KeyedListDiff.Operation.Type.INSERT) {
          KeyedListDiff.Operation<Key, Value, Diff> opQIns = itQ.next();
          qPrimeOps.add(opQIns);

          Diff emptyDiff = diffFunction.apply(opQIns.operandValue, opQIns.operandValue);
          pPrimeOps.add(new KeyedListDiff.Operation<>(KeyedListDiff.Operation.Type.MATCH, opQIns.mode, opQIns.operandKey, null, emptyDiff));
        }

        if (!itQ.hasNext()) {
          throw new IllegalDiffException("p has remaining operations after q is exhausted");
        }
        KeyedListDiff.Operation<Key, Value, Diff> opQBase = itQ.next();

        // キーによるノードの同一性検証
        if (!opP.operandKey.equals(opQBase.operandKey)) {
          throw new IllegalDiffException("p and q disagree on node identity");
        }

        if (opP.type == KeyedListDiff.Operation.Type.MATCH) {
          if (opQBase.type == KeyedListDiff.Operation.Type.MATCH) {
            // MATCH -> MATCH のパターン：双方の内部変更を交換
            Pair<Diff, Diff> commuted = commuteDiff.commute(opP.operandDiff, opQBase.operandDiff);
            qPrimeOps.add(new KeyedListDiff.Operation<>(KeyedListDiff.Operation.Type.MATCH, opQBase.mode, opQBase.operandKey, null, commuted.first));
            pPrimeOps.add(new KeyedListDiff.Operation<>(KeyedListDiff.Operation.Type.MATCH, opP.mode, opP.operandKey, null, commuted.second));
          } else if (opQBase.type == KeyedListDiff.Operation.Type.DELETE) {
            // change -> delete のパターン：交換不可（Conflict）
            throw new ConflictException("Conflict: p modifies a node that q subsequently deletes (Key: " + opP.operandKey + ")");
          }
        } else {
          // opP.type == INSERT のパターン
          if (opQBase.type == KeyedListDiff.Operation.Type.DELETE) {
            // insert -> delete のパターン：交換不可（Conflict）
            throw new ConflictException("Conflict: p inserts a node that q subsequently deletes (Key: " + opP.operandKey + ")");
          }

          // 【方針適用】：insert -> match/change のパターン切り分け
          // q の持っている変更（operandDiff）が空でない（実質的なchangeである）場合は、依存関係があるため交換不可
          if (!opQBase.operandDiff.isEmpty()) {
            throw new ConflictException("Conflict: p inserts a node that q subsequently modifies (Key: " + opP.operandKey + ")");
          }

          // insert -> match（空の差分）のパターン：
          // 依存関係がないため安全に交換可能。q' 側には何も追加せず、p' 側で元の値をそのまま INSERT する。
          pPrimeOps.add(new KeyedListDiff.Operation<>(KeyedListDiff.Operation.Type.INSERT, opP.mode, opP.operandKey, opP.operandValue, null));
        }
      }
    }

    // q 側に残った末尾の INSERT 操作をすべて回収
    while (itQ.hasNext()) {
      KeyedListDiff.Operation<Key, Value, Diff> opQ = itQ.next();
      if (opQ.type == KeyedListDiff.Operation.Type.INSERT) {
        qPrimeOps.add(opQ);

        Diff emptyDiff = diffFunction.apply(opQ.operandValue, opQ.operandValue);
        pPrimeOps.add(new KeyedListDiff.Operation<>(KeyedListDiff.Operation.Type.MATCH, opQ.mode, opQ.operandKey, null, emptyDiff));
      } else {
        throw new IllegalDiffException("q has remaining operations after p is exhausted");
      }
    }

    return new Pair<>(new KeyedListDiff<>(qPrimeOps), new KeyedListDiff<>(pPrimeOps));
  }
}
