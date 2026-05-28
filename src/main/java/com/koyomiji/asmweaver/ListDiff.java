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
     * For MATCH, operand1 is the element that matches in both lists.
     * For DELETE, operand1 is the element to delete from the original list.
     * Otherwise, operand1 is null.
     */
    public final T operand1;
    /**
     * For MATCH, operand2 is the element that matches in both lists.
     * For INSERT, operand2 is the element to insert into the original list.
     * Otherwise, operand2 is null.
     */
    public final T operand2;

    public Operation(Type type, Mode mode, T operand1, T operand2) {
      if (type == null) {
        throw new IllegalArgumentException("type cannot be null");
      }

      if (mode == null) {
        throw new IllegalArgumentException("mode cannot be null");
      }

      this.type = type;
      this.mode = mode;
      this.operand1 = operand1;
      this.operand2 = operand2;
    }
  }
}
