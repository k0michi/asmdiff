 package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.List;

public class InsnListDiff implements IDiff {
  public List<Operation> operations;

  public InsnListDiff(List<Operation> operations) {
    this.operations = operations;
  }

  @Override
  public boolean isEmpty() {
    for (Operation op : operations) {
      if (op.type != Operation.Type.MATCH) {
        return false;
      }
    }

    return true;
  }

  @Override
  public int distance() {
    int distance = 0;

    for (Operation op : operations) {
      if (op.type != Operation.Type.MATCH) {
        distance++;
      }
    }

    return distance;
  }

  public static class Operation {
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

    public final AbstractInsnNode operand;

    public Operation(Type type, Mode mode, AbstractInsnNode operand) {
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
