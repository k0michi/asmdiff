package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.VarInsnNode;

public class VarInsnNodeHelper {
  public static boolean equals(VarInsnNode a, VarInsnNode b) {
    if (!AbstractInsnNodeHelper.equals(a, b)) {
      return false;
    }

    if (a.var != b.var) {
      return false;
    }

    return true;
  }
}
