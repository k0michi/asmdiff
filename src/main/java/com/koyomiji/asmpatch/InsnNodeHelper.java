package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.InsnNode;

public class InsnNodeHelper {
  public static boolean equals(InsnNode a, InsnNode b) {
    if (!AbstractInsnNodeHelper.equals(a, b)) {
      return false;
    }

    return true;
  }
}
