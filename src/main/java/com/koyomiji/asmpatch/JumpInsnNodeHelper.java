package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.JumpInsnNode;

public class JumpInsnNodeHelper {
  public static boolean equals(JumpInsnNode a, JumpInsnNode b) {
    if (!AbstractInsnNodeHelper.equals(a, b)) {
      return false;
    }

    if (!LabelNodeHelper.equals(a.label, b.label)) {
      return false;
    }

    return true;
  }
}
