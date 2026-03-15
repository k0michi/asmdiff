package com.koyomiji.asmpatch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.List;

class AnnotationNodeHelperTest {
  @Test
  void testEquals() {
    var node1 = new AnnotationNode("Ljava/lang/Override;");
    var node2 = new AnnotationNode("Ljava/lang/Override;");
    Assertions.assertTrue(AnnotationNodeHelper.equals(node1, node2));
  }

  @Test
  void testEquals_differentDesc() {
    var node1 = new AnnotationNode("Ljava/lang/Override;");
    var node2 = new AnnotationNode("Ljava/lang/Deprecated;");
    Assertions.assertFalse(AnnotationNodeHelper.equals(node1, node2));
  }

  @Test
  void testEquals_differentValues() {
    var node1 = new AnnotationNode("Ljava/lang/Override;");
    node1.values = List.of("value", "test");
    var node2 = new AnnotationNode("Ljava/lang/Override;");
    node2.values = List.of("value", "test2");
    Assertions.assertFalse(AnnotationNodeHelper.equals(node1, node2));
  }
}
