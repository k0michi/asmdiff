package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.List;

class AnnotationNodeHelperTest {
  @Test
  void test_equals_0() {
    AnnotationNode node1 = new AnnotationNode("Lcom/example/Annotation;");
    AnnotationNode node2 = new AnnotationNode("Lcom/example/Annotation;");
    Assertions.assertTrue(AnnotationNodeHelper.equals(node1, node2));
  }

  @Test
  void test_equals_1() {
    AnnotationNode node1 = new AnnotationNode("Lcom/example/Annotation;");
    AnnotationNode node2 = new AnnotationNode("Lcom/example/OtherAnnotation;");
    Assertions.assertFalse(AnnotationNodeHelper.equals(node1, node2));
  }

  @Test
  void test_equals_2() {
    AnnotationNode node1 = new AnnotationNode("Lcom/example/Annotation;");
    node1.values = List.of("key", "value");
    AnnotationNode node2 = new AnnotationNode("Lcom/example/Annotation;");
    node2.values = List.of("key", "value");
    Assertions.assertTrue(AnnotationNodeHelper.equals(node1, node2));
  }

  @Test
  void test_equals_nested_0() {
    AnnotationNode nodeNested1 = new AnnotationNode("Lcom/example/NestedAnnotation;");
    AnnotationNode node1 = new AnnotationNode("Lcom/example/Annotation;");
    node1.values = List.of("nested", nodeNested1);

    AnnotationNode nodeNested2 = new AnnotationNode("Lcom/example/NestedAnnotation;");
    AnnotationNode node2 = new AnnotationNode("Lcom/example/Annotation;");
    node2.values = List.of("nested", nodeNested2);

    Assertions.assertTrue(AnnotationNodeHelper.equals(node1, node2));
  }
}
