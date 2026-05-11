package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.HashCodeBuilder;
import org.objectweb.asm.tree.RecordComponentNode;

import java.util.Objects;

public class RecordComponentHelper {
  public static boolean equals(RecordComponentNode node1, RecordComponentNode node2) {
    if (node1 == node2) {
      return true;
    }

    if (node1 == null || node2 == null) {
      return false;
    }

    if (node1.getClass() != node2.getClass()) {
      return false;
    }

    if (node1.getClass() == org.objectweb.asm.tree.RecordComponentNode.class) {
      return Objects.equals(node1.name, node2.name)
              && Objects.equals(node1.descriptor, node2.descriptor)
              && Objects.equals(node1.signature, node2.signature)
              && ListHelper.equals(node1.visibleAnnotations, node2.visibleAnnotations, AnnotationNodeHelper::equals)
              && ListHelper.equals(node1.invisibleAnnotations, node2.invisibleAnnotations, AnnotationNodeHelper::equals)
              && ListHelper.equals(node1.visibleTypeAnnotations, node2.visibleTypeAnnotations, AnnotationNodeHelper::equals)
              && ListHelper.equals(node1.invisibleTypeAnnotations, node2.invisibleTypeAnnotations, AnnotationNodeHelper::equals);
    }

    // Non-standard
    return Objects.equals(node1, node2);
  }

  public static int hashCode(RecordComponentNode node) {
    if (node == null) {
      return 0;
    }

    return new HashCodeBuilder()
            .append(node.name)
            .append(node.descriptor)
            .append(node.signature)
            .append(node.visibleAnnotations,
                    (l) -> ListHelper.hashCode(l, AnnotationNodeHelper::hashCode)
            )
            .append(node.invisibleAnnotations,
                    (l) -> ListHelper.hashCode(l, AnnotationNodeHelper::hashCode)
            )
            .append(node.visibleTypeAnnotations,
                    (l) -> ListHelper.hashCode(l, AnnotationNodeHelper::hashCode)
            )
            .append(node.invisibleTypeAnnotations,
                    (l) -> ListHelper.hashCode(l, AnnotationNodeHelper::hashCode)
            ).build();
  }
}
