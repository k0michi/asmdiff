package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.LookupSwitchInsnNode;

public class LookupSwitchInsnNodeHelper {
  public static boolean equals(LookupSwitchInsnNode a, LookupSwitchInsnNode b) {
    if (!AbstractInsnNodeHelper.equals(a, b)) {
      return false;
    }

    if (!LabelNodeHelper.equals(a.dflt, b.dflt)) {
      return false;
    }

    if (!ListHelper.equals(a.keys, b.keys)) {
      return false;
    }

    if (!ListHelper.equals(a.labels, b.labels, LabelNodeHelper::equals)) {
      return false;
    }

    return true;
  }
}
