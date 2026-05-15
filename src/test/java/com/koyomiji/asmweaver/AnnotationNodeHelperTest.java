package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.TypeReference;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableAnnotationNode;
import org.objectweb.asm.tree.TypeAnnotationNode;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.koyomiji.asmweaver.LabelNodes.l0;
import static com.koyomiji.asmweaver.LabelNodes.l1;

class AnnotationNodeHelperTest {
  private static final List<AnnotationNode> UNIQUE_NODES = List.of(
          new AnnotationNode("Lcom/example/Annotation;"),
          new AnnotationNode("Lcom/example/OtherAnnotation;"),
          setValues(new AnnotationNode("Lcom/example/Annotation;"), List.of("key", "value")),
          setValues(new AnnotationNode("Lcom/example/Annotation;"), List.of("key", (byte)0)),
          setValues(new AnnotationNode("Lcom/example/Annotation;"), List.of("key", (char)0)),
          setValues(new AnnotationNode("Lcom/example/Annotation;"), List.of("key", (short)0)),
          setValues(new AnnotationNode("Lcom/example/Annotation;"), List.of("key", (int)0)),
          setValues(new AnnotationNode("Lcom/example/Annotation;"), List.of("key", (long)0)),
          setValues(new AnnotationNode("Lcom/example/Annotation;"), List.of("key", (float)0)),
          setValues(new AnnotationNode("Lcom/example/Annotation;"), List.of("key", (double)0)),
          setValues(new AnnotationNode("Lcom/example/Annotation;"), List.of("key", Type.getType("Lcom/example/Type;"))),
          setValues(new AnnotationNode("Lcom/example/Annotation;"), List.of("key", new String[]{"a", "b"})),
          setValues(new AnnotationNode("Lcom/example/Annotation;"), List.of("key", List.of(1))),
          new TypeAnnotationNode(TypeReference.CLASS_TYPE_PARAMETER, TypePath.fromString("*"), "Lcom/example/TypeAnnotation;"),
          new TypeAnnotationNode(TypeReference.CLASS_TYPE_PARAMETER, TypePath.fromString("."), "Lcom/example/TypeAnnotation;"),
          new TypeAnnotationNode(TypeReference.CLASS_TYPE_PARAMETER, TypePath.fromString("*"), "Lcom/example/OtherTypeAnnotation;"),
          new TypeAnnotationNode(TypeReference.CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT, TypePath.fromString("*"), "Lcom/example/TypeAnnotation;"),
          new LocalVariableAnnotationNode(
                  TypeReference.CLASS_TYPE_PARAMETER,
                  TypePath.fromString("*"),
                  new LabelNode[]{l0},
                  new LabelNode[]{l1},
                  new int[]{0},
                  "Lcom/example/TypeAnnotation;"
          ),
          new LocalVariableAnnotationNode(
                  TypeReference.CLASS_TYPE_PARAMETER,
                  TypePath.fromString("*"),
                  new LabelNode[]{l1},
                  new LabelNode[]{l1},
                  new int[]{0},
                  "Lcom/example/TypeAnnotation;"
          ),
          new LocalVariableAnnotationNode(
                  TypeReference.CLASS_TYPE_PARAMETER,
                  TypePath.fromString("*"),
                  new LabelNode[]{l0},
                  new LabelNode[]{l0},
                  new int[]{0},
                  "Lcom/example/TypeAnnotation;"
          )
  );

  private static AnnotationNode setValues(AnnotationNode node, List<Object> values) {
    node.values = values;
    return node;
  }

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
            "Lcom/example/TypeAnnotation;"
    );
    LocalVariableAnnotationNode node2 = new LocalVariableAnnotationNode(
            TypeReference.CLASS_TYPE_PARAMETER,
            TypePath.fromString("*"),
            new LabelNode[]{l0},
            new LabelNode[]{l1},
            new int[]{0},
            "Lcom/example/TypeAnnotation;"
    );
    Assertions.assertTrue(AnnotationNodeHelper.equals(node1, node2));
  }

  static Stream<Arguments> provideAllPairs() {
    return IntStream.range(0, UNIQUE_NODES.size()).boxed().flatMap(i ->
            IntStream.range(0, UNIQUE_NODES.size()).mapToObj(j ->
                    Arguments.of(i, j, UNIQUE_NODES.get(i), UNIQUE_NODES.get(j))
            )
    );
  }

  static Stream<Arguments> provideAll() {
    return IntStream.range(0, UNIQUE_NODES.size()).boxed().map(i ->
            Arguments.of(i, UNIQUE_NODES.get(i))
    );
  }

  @ParameterizedTest(name = "i={0}, j={1}")
  @MethodSource("provideAllPairs")
  void test_equals_provideAllPairs(int i, int j, AnnotationNode nodeA, AnnotationNode nodeB) {
    if (i == j) {
      Assertions.assertTrue(AnnotationNodeHelper.equals(nodeA, nodeB));
    } else {
      Assertions.assertFalse(AnnotationNodeHelper.equals(nodeA, nodeB));
    }
  }

  @ParameterizedTest(name = "i={0}")
  @MethodSource("provideAll")
  void test_hashCode_provideAllPairs(int i, AnnotationNode node) {
    Assertions.assertEquals(AnnotationNodeHelper.hashCode(node), AnnotationNodeHelper.hashCode(node));
  }
}
