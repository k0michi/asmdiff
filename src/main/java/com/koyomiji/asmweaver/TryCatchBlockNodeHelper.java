package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import java.util.Objects;
import java.util.function.BiPredicate;

public class TryCatchBlockNodeHelper {
  public static boolean equals(TryCatchBlockNode node1, TryCatchBlockNode node2, BiPredicate<LabelNode, LabelNode> labelEquals) {
    if (node1 == node2) {
      return true;
    }

    if (node1 == null || node2 == null) {
      return false;
    }

    return labelEquals.test(node1.start, node2.start)
            && labelEquals.test(node1.end, node2.end)
            && labelEquals.test(node1.handler, node2.handler)
            && Objects.equals(node1.type, node2.type)
            && ListHelper.equals(node1.visibleTypeAnnotations, node2.visibleTypeAnnotations, AnnotationNodeHelper::equals)
            && ListHelper.equals(node1.invisibleTypeAnnotations, node2.invisibleTypeAnnotations, AnnotationNodeHelper::equals);
  }

  public static boolean equals(TryCatchBlockNode node1, TryCatchBlockNode node2) {
    return equals(node1, node2, Objects::equals);
  }
}
