package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.CustomDataInput;
import com.koyomiji.asmweaver.io.CustomDataOutput;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

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
}
