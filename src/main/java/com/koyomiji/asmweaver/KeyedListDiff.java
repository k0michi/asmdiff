package com.koyomiji.asmweaver;

import java.util.List;

public class KeyedListDiff<Key, Value, Diff> {
  public List<Operation<Key, Value, Diff>> operations;

  public KeyedListDiff(List<Operation<Key, Value, Diff>> operations) {
    this.operations = operations;
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
      if (type == null) {
        throw new IllegalArgumentException("type cannot be null");
      }

      if (mode == null) {
        throw new IllegalArgumentException("mode cannot be null");
      }

      this.type = type;
      this.mode = mode;
      this.operandKey = operandKey;
      this.operandValue = operandValue;
      this.operandDiff = operandDiff;
    }
  }
}
