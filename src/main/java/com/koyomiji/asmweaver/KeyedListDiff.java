package com.koyomiji.asmweaver;

import java.util.List;

public class KeyedListDiff<Key, Value, Diff extends IDiff> implements IDiff {
  public List<Operation<Key, Value, Diff>> operations;

  public KeyedListDiff(List<Operation<Key, Value, Diff>> operations) {
    this.operations = operations;
  }

  @Override
  public boolean isEmpty() {
    for (Operation<Key, Value, Diff> op : operations) {
      if (op.type != Operation.Type.MATCH) {
        return false;
      }

      if (!op.operandDiff.isEmpty()) {
        return false;
      }
    }

    return true;
  }

  @Override
  public int distance() {
    int distance = 0;

    for (Operation<Key, Value, Diff> op : operations) {
      if (op.type != Operation.Type.MATCH) {
        distance++;
      } else {
        distance += op.operandDiff.distance();
      }
    }

    return distance;
  }

  public static class Operation<Key, Value, Diff extends IDiff> {
    public enum Type {
      MATCH,
      INSERT,
      DELETE
    }

    public enum Mode {
      BETWEEN,
      BEFORE,
      AFTER
    }

    public final Type type;
    public final Mode mode;
    public final Key operandKey;
    /**
     * For INSERT, operandValue is the value to insert into the original list.
     * Otherwise, operandValue is null.
     */
    public final Value operandValue;
    /**
     * For MATCH, operandDiff is the diff between the values in the original and modified lists.
     * Otherwise, operandDiff is null.
     */
    public final Diff operandDiff;

    public Operation(Type type, Mode mode, Key operandKey, Value operandValue, Diff operandDiff) {
      this.type = type;
      this.mode = mode;
      this.operandKey = operandKey;
      this.operandValue = operandValue;
      this.operandDiff = operandDiff;
    }
  }
}
