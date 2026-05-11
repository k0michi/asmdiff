package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.HashCodeBuilder;
import org.objectweb.asm.tree.FieldNode;

import java.util.Objects;

public class FieldNodeHelper {
  public static boolean equals(FieldNode node1, FieldNode node2) {
    if (node1 == node2) {
      return true;
    }

    if (node1 == null || node2 == null) {
      return false;
    }

    if (node1.getClass() != node2.getClass()) {
      return false;
    }

    return node1.access == node2.access
            && Objects.equals(node1.name, node2.name)
            && Objects.equals(node1.desc, node2.desc)
            && Objects.equals(node1.signature, node2.signature)
            && Objects.equals(node1.value, node2.value)
            && ListHelper.equals(node1.visibleAnnotations, node2.visibleAnnotations, AnnotationNodeHelper::equals)
            && ListHelper.equals(node1.invisibleAnnotations, node2.invisibleAnnotations, AnnotationNodeHelper::equals)
            && ListHelper.equals(node1.visibleTypeAnnotations, node2.visibleTypeAnnotations, AnnotationNodeHelper::equals)
            && ListHelper.equals(node1.invisibleTypeAnnotations, node2.invisibleTypeAnnotations, AnnotationNodeHelper::equals)
            && ListHelper.equals(node1.attrs, node2.attrs, Objects::equals);
  }

  public static int hashCode(FieldNode node) {
    if (node == null) {
      return 0;
    }

    HashCodeBuilder builder = new HashCodeBuilder();
    builder.append(node.access);
    builder.append(node.name);
    builder.append(node.desc);
    builder.append(node.signature);
    builder.append(node.value);
    builder.append(ListHelper.hashCode(node.visibleAnnotations, AnnotationNodeHelper::hashCode));
    builder.append(ListHelper.hashCode(node.invisibleAnnotations, AnnotationNodeHelper::hashCode));
    builder.append(ListHelper.hashCode(node.visibleTypeAnnotations, AnnotationNodeHelper::hashCode));
    builder.append(ListHelper.hashCode(node.invisibleTypeAnnotations, AnnotationNodeHelper::hashCode));
    builder.append(ListHelper.hashCode(node.attrs, Objects::hashCode));
    return builder.build();
  }
}
