package com.koyomiji.asmweaver;

import java.util.ArrayList;
import java.util.List;

public class ListDiff<T> implements IDiff {
  public boolean isEmpty;
  public List<Operation<T>> operations;

  public ListDiff() {
    isEmpty = true;
  }

  public ListDiff(List<Operation<T>> operations) {
    isEmpty = false;
    this.operations = operations;
  }

  @Override
  public boolean isEmpty() {
    if (this.isEmpty) {
      return true;
    }

    for (Operation<T> op : operations) {
      if (op.type != Operation.Type.MATCH) {
        return false;
      }
    }

    return true;
  }

  @Override
  public int distance() {
    if (this.isEmpty) {
      return 0;
    }

    int distance = 0;

    for (Operation<T> op : operations) {
      if (op.type != Operation.Type.MATCH) {
        distance++;
      }
    }

    return distance;
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
