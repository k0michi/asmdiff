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
      // TODO: insn annotation
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
      /**
       * Anchored by the previous instruction and the next instruction.
       */
      BETWEEN,
      BEFORE,
      AFTER
    }

    public final Type type;
    public final Mode mode;

    public final AbstractInsnNode operand1;
    public final AbstractInsnNode operand2;

    public Operation(Type type, Mode mode, AbstractInsnNode operand1, AbstractInsnNode operand2) {
      this.type = type;
      this.mode = mode;
      this.operand1 = operand1;
      this.operand2 = operand2;
    }
  }
}
