package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.AbstractInsnNode;

public class AbstractInsnNodeHelper {
  public static boolean equals(AbstractInsnNode a, AbstractInsnNode b) {
    if (a == b) {
      return true;
    }

    if (a == null || b == null) {
      return false;
    }

    if (a.getClass() != b.getClass()) {
      return false;
    }

    if (a.getOpcode() != b.getOpcode()) {
      return false;
    }

    if (a.getType() != b.getType()) {
      return false;
    }

    if (!ListHelper.equals(a.visibleTypeAnnotations, b.visibleTypeAnnotations, TypeAnnotationNodeHelper::equals)) {
      return false;
    }

    if (!ListHelper.equals(a.invisibleTypeAnnotations, b.invisibleTypeAnnotations, TypeAnnotationNodeHelper::equals)) {
      return false;
    }

    return true;
  }
}
