package com.koyomiji.asmweaver;

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
    if (a instanceof AnnotationNode && b instanceof AnnotationNode) {
      return AnnotationNodeHelper.equals((AnnotationNode) a, (AnnotationNode) b);
    }

    if (a instanceof List<?> && b instanceof List<?>) {
      return ListHelper.equals((List<Object>) a, (List<Object>) b, AnnotationNodeHelper::equalsValue);
    }

    return Objects.equals(a, b);
  }
}