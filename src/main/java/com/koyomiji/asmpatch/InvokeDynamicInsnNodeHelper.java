package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.InvokeDynamicInsnNode;

import java.util.Objects;

public class InvokeDynamicInsnNodeHelper {
  public static boolean equals(InvokeDynamicInsnNode a, InvokeDynamicInsnNode b) {
    if (!AbstractInsnNodeHelper.equals(a, b)) {
      return false;
    }

    if (!Objects.equals(a.name, b.name)) {
      return false;
    }

    if (!Objects.equals(a.desc, b.desc)) {
      return false;
    }

    if (!Objects.equals(a.bsm, b.bsm)) {
      return false;
    }

    if (!ArrayHelper.equals(a.bsmArgs, b.bsmArgs, Objects::equals)) {
      return false;
    }

    return true;
  }
}
