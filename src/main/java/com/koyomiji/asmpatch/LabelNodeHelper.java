package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.LabelNode;

public class LabelNodeHelper {
  public static boolean equals(LabelNode a, LabelNode b) {
    if (!AbstractInsnNodeHelper.equals(a, b)) {
      return false;
    }

    if (a.getLabel() != b.getLabel()) {
      return false;
    }

    return true;
  }
}
