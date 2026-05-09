package com.koyomiji.asmweaver.analysis;

import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.Objects;

public class DefUse {
  public final int index;
  public final AbstractInsnNode insn;
  public final int insnIndex;

  public DefUse(int insnIndex, AbstractInsnNode insn, int index) {
    this.index = index;
    this.insnIndex = insnIndex;
    this.insn = insn;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DefUse defUse = (DefUse) o;
    return index == defUse.index && insnIndex == defUse.insnIndex ;
  }

  @Override
  public int hashCode() {
    return Objects.hash(index, insnIndex);
  }
}
