package com.koyomiji.asmweaver;

import java.util.List;

public class ListDiff<T> {
  public List<Operation<T>> operations;

  public ListDiff(List<Operation<T>> operations) {
    this.operations = operations;
  }

  public static class Operation<T> {
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
    /**
     * MATCH: operand is the matched element
     * INSERT: operand is the inserted element
     * DELETE: operand is the deleted element
     */
    public final T operand;

    public Operation(Type type, Mode mode, T operand) {
      if (type == null) {
        throw new IllegalArgumentException("type cannot be null");
      }

      if (mode == null) {
        throw new IllegalArgumentException("mode cannot be null");
      }

      this.type = type;
      this.mode = mode;
      this.operand = operand;
    }
  }
}
