package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.List;

public class InsnListDiff {
  public List<Operation> operations;

  public InsnListDiff(List<Operation> operations) {
    this.operations = operations;
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

    public Type type;
    public Mode mode;
    /**
     * For MATCH, operand is the instruction that matches in both lists.
     * For INSERT, operand is the instruction to insert into the original list.
     * For DELETE, operand is the instruction to delete from the original list.
     */
    public AbstractInsnNode operand;

    public Operation(Type type, Mode mode, AbstractInsnNode operand) {
      this.type = type;
      this.mode = mode;
      this.operand = operand;
    }
  }
}
