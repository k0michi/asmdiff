package com.koyomiji.asmweaver;

import java.util.List;

public class KeyedListDiff<Key, Value, Patch> {
  public List<Operation<Key, Value, Patch>> operations;

  public KeyedListDiff(List<Operation<Key, Value, Patch>> operations) {
    this.operations = operations;
  }

  public static class Operation<Key, Value, Patch> {
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
     * For MATCH, operandPatch is the patch to apply to the value in the original list.
     * Otherwise, operandPatch is null.
     */
    public Patch operandPatch;

    public Operation(Type type, Mode mode, Key operandKey, Value operandValue, Patch operandPatch) {
      this.type = type;
      this.mode = mode;
      this.operandKey = operandKey;
      this.operandValue = operandValue;
      this.operandPatch = operandPatch;
    }
  }
}
