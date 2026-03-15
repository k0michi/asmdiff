package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.LdcInsnNode;

import java.util.Objects;

public class LdcInsnNodeHelper {
  public static boolean equals(LdcInsnNode a, LdcInsnNode b) {
    if (!AbstractInsnNodeHelper.equals(a, b)) {
      return false;
    }

    if (!Objects.equals(a.cst, b.cst)) {
      return false;
    }

    return true;
  }
}
