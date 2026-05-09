package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.TypeReference;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableAnnotationNode;

import java.util.List;

import static com.koyomiji.asmweaver.LabelNodes.l0;
import static com.koyomiji.asmweaver.LabelNodes.l1;

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

  @Test
  void test_equals_localVariable_0() {
    LocalVariableAnnotationNode node1 = new LocalVariableAnnotationNode(
            TypeReference.CLASS_TYPE_PARAMETER,
            TypePath.fromString("*"),
            new LabelNode[]{l0},
            new LabelNode[]{l1},
            new int[]{0},
            "Ljava/lang/String;"
    );
    LocalVariableAnnotationNode node2 = new LocalVariableAnnotationNode(
            TypeReference.CLASS_TYPE_PARAMETER,
            TypePath.fromString("*"),
            new LabelNode[]{l0},
            new LabelNode[]{l1},
            new int[]{0},
            "Ljava/lang/String;"
    );
    Assertions.assertTrue(AnnotationNodeHelper.equals(node1, node2));
  }
}
