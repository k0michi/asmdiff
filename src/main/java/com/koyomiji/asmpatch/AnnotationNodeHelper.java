package com.koyomiji.asmpatch;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.List;
import java.util.Objects;

public class AnnotationNodeHelper {
  public static boolean equals(AnnotationNode a, AnnotationNode b) {
    if (a == b) {
      return true;
    }

    if (a == null || b == null) {
      return false;
    }

    if (a.getClass() != b.getClass()) {
      return false;
    }

    if (!Objects.equals(a.desc, b.desc)) {
      return false;
    }

    if (!ListHelper.equals(a.values, b.values, AnnotationNodeHelper::equalsValue)) {
      return false;
    }

    return true;
  }

  private static boolean equalsValue(Object a, Object b) {
    if (a == b) {
      return true;
    }

    if (a == null || b == null) {
      return false;
    }

    if (a instanceof AnnotationNode && b instanceof AnnotationNode) {
      if (!AnnotationNodeHelper.equals((AnnotationNode) a, (AnnotationNode) b)) {
        return false;
      }
    }

    if (a instanceof Type && b instanceof Type) {
      if (!Objects.equals(a, b)) {
        return false;
      }
    }

    if (a instanceof List<?> && b instanceof List<?>) {
      if (!ListHelper.equals((List<Object>) a, (List<Object>) b, AnnotationNodeHelper::equalsValue)) {
        return false;
      }
    }

    return true;
  }
}
