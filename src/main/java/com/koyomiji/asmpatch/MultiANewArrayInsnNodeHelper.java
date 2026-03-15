package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.MultiANewArrayInsnNode;

import java.util.Objects;

public class MultiANewArrayInsnNodeHelper {
  public static boolean equals(MultiANewArrayInsnNode a, MultiANewArrayInsnNode b) {
    if (!AbstractInsnNodeHelper.equals(a, b)) {
      return false;
    }

    if (!Objects.equals(a.desc, b.desc)) {
      return false;
    }

    if (a.dims != b.dims) {
      return false;
    }

    return true;
  }
}
