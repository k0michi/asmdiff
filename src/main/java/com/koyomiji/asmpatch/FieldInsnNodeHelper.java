package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.FieldInsnNode;

import java.util.Objects;

public class FieldInsnNodeHelper {
  public static boolean equals(FieldInsnNode a, FieldInsnNode b) {
    if (!AbstractInsnNodeHelper.equals(a, b)) {
      return false;
    }

    if (!Objects.equals(a.owner, b.owner)) {
      return false;
    }

    if (!Objects.equals(a.name, b.name)) {
      return false;
    }

    if (!Objects.equals(a.desc, b.desc)) {
      return false;
    }

    return true;
  }
}
