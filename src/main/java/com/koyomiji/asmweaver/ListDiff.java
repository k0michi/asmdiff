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

    public Type type;
    public Mode mode;
    public T operand;

    public Operation(Type type, Mode mode, T operand) {
      this.type = type;
      this.mode = mode;
      this.operand = operand;
    }
  }
}
