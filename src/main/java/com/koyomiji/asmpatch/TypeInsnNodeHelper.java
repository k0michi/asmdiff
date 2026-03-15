package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.TypeInsnNode;

import java.util.Objects;

public class TypeInsnNodeHelper {
  public static boolean equals(TypeInsnNode a, TypeInsnNode b) {
    if (!AbstractInsnNodeHelper.equals(a, b)) {
      return false;
    }

    if (!Objects.equals(a.desc, b.desc)) {
      return false;
    }

    return true;
  }
}
