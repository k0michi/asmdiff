package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.IincInsnNode;

public class IincInsnNodeHelper {
  public static boolean equals(IincInsnNode a, IincInsnNode b) {
    if (!AbstractInsnNodeHelper.equals(a, b)) {
      return false;
    }

    if (a.var != b.var) {
      return false;
    }

    if (a.incr != b.incr) {
      return false;
    }

    return true;
  }
}
