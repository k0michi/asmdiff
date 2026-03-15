package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Objects;

public class MethodInsnNodeHelper {
  public static boolean equals(MethodInsnNode a, MethodInsnNode b) {
    if (!AbstractInsnNodeHelper.equals(a, b)) {
      return false;
    }

    if (a.itf != b.itf) {
      return false;
    }

    if (Objects.equals(a.owner, b.owner)) {
      return false;
    }

    if (Objects.equals(a.name, b.name)) {
      return false;
    }

    if (Objects.equals(a.desc, b.desc)) {
      return false;
    }

    return true;
  }
}
