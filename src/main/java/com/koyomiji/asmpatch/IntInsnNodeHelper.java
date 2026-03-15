package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.IntInsnNode;

public class IntInsnNodeHelper {
  public static boolean equals(IntInsnNode a, IntInsnNode b) {
    if (!AbstractInsnNodeHelper.equals(a, b)) {
      return false;
    }

    if (a.operand != b.operand) {
      return false;
    }

    return true;
  }
}
