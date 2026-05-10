package com.koyomiji.asmweaver;

import java.util.List;

public class KeyedListDiff<Key, Value, Diff> implements IDiff {
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
    }

    return true;
  }

  public static class Operation<Key, Value, Diff> {
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

    public Type type;
    public Mode mode;
    public Key operandKey;
    /**
     * For INSERT, operandValue is the value to insert into the original list.
     * Otherwise, operandValue is null.
     */
    public Value operandValue;
    /**
     * For MATCH, operandDiff is the diff between the values in the original and modified lists.
     * Otherwise, operandDiff is null.
     */
    public Diff operandDiff;

    public Operation(Type type, Mode mode, Key operandKey, Value operandValue, Diff operandDiff) {
      this.type = type;
      this.mode = mode;
      this.operandKey = operandKey;
      this.operandValue = operandValue;
      this.operandDiff = operandDiff;
    }
  }
}
