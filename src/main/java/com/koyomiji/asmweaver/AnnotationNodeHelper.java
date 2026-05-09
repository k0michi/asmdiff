package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableAnnotationNode;
import org.objectweb.asm.tree.TypeAnnotationNode;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

public class AnnotationNodeHelper {
  public static boolean equals(AnnotationNode a, AnnotationNode b) {
    return equals(a, b, Objects::equals, Objects::equals);
  }

  public static boolean equals(AnnotationNode a, AnnotationNode b, BiPredicate<LabelNode, LabelNode> labelEquals, BiPredicate<Integer, Integer> localEquals) {
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

    if (a.getClass() == AnnotationNode.class) {
      return true;
    } else if (a.getClass() == TypeAnnotationNode.class) {
      return equals((TypeAnnotationNode) a, (TypeAnnotationNode) b);
    } else if (a.getClass() == LocalVariableAnnotationNode.class) {
      return equals((LocalVariableAnnotationNode) a, (LocalVariableAnnotationNode) b, labelEquals, localEquals);
    }

    return Objects.equals(a, b);
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

  private static boolean equals(TypeAnnotationNode a, TypeAnnotationNode b) {
    return Objects.equals(a.typeRef, b.typeRef)
            && TypePathHelper.equals(a.typePath, b.typePath);
  }

  private static boolean equals(LocalVariableAnnotationNode a, LocalVariableAnnotationNode b, BiPredicate<LabelNode, LabelNode> labelEquals, BiPredicate<Integer, Integer> localEquals) {
    if (a.start.size() != b.start.size()) {
      return false;
    }

    if (a.end.size() != b.end.size()) {
      return false;
    }

    if (a.index.size() != b.index.size()) {
      return false;
    }

    int size = Math.max(a.start.size(), Math.max(a.end.size(), a.index.size()));

    for (int i = 0; i < size; i++) {
      if (!labelEquals.test(ListHelper.getOrNull(a.start, i), ListHelper.getOrNull(b.start, i))
              || !labelEquals.test(ListHelper.getOrNull(a.end, i), ListHelper.getOrNull(b.end, i))
              || !localEquals.test(ListHelper.getOrNull(a.index, i), ListHelper.getOrNull(b.index, i))) {
        return false;
      }
    }

    return true;
  }

  public static int hashCode(AnnotationNode node) {
    if (node == null) {
      return 0;
    }

    if (node.getClass() == AnnotationNode.class) {
      return Objects.hash(node.desc, ListHelper.hashCode(node.values, AnnotationNodeHelper::annotationValueHashCode));
    } else if (node.getClass() == TypeAnnotationNode.class) {
      TypeAnnotationNode typeNode = (TypeAnnotationNode) node;
      return Objects.hash(node.desc, ListHelper.hashCode(node.values, AnnotationNodeHelper::annotationValueHashCode), typeNode.typeRef, TypePathHelper.hashCode(typeNode.typePath));
    } else if (node.getClass() == LocalVariableAnnotationNode.class) {
      LocalVariableAnnotationNode localVarNode = (LocalVariableAnnotationNode) node;
      return Objects.hash(node.desc, ListHelper.hashCode(node.values, AnnotationNodeHelper::annotationValueHashCode), ListHelper.hashCode(localVarNode.start, Objects::hashCode), ListHelper.hashCode(localVarNode.end, Objects::hashCode), ListHelper.hashCode(localVarNode.index, Objects::hashCode));
    }

    return Objects.hash(node);
  }

  private static int annotationValueHashCode(Object value) {
    // String[]
    if (value instanceof String[]) {
      return Arrays.hashCode((String[]) value);
    }

    // AnnotationNode
    if (value instanceof AnnotationNode) {
      return hashCode((AnnotationNode) value);
    }

    // List
    if (value instanceof List<?>) {
      return ListHelper.hashCode((List<Object>) value, AnnotationNodeHelper::annotationValueHashCode);
    }

    // Byte, Boolean, Character, Short, Integer, Long, Float, Double, String, Type
    return Objects.hashCode(value);
  }
}