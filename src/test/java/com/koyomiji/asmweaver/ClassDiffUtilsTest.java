package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

class ClassDiffUtilsTest {
  static ClassNode base;

  static {
    base = new ClassNode();
    base.visit(Opcodes.V1_8, 0, "TestClass", null, "java/lang/Object", null);
    base.visitEnd();
  }

  @Test
  void test_diff_0() {
    ClassNode node1 = base;
    ClassNode node2 = new ClassNode();
    node2.visit(Opcodes.V1_8, 0, "TestClass", null, "java/lang/Object", null);
    node2.visitEnd();
    ClassDiff diff = ClassDiffUtils.diff(node1, node2);
    Assertions.assertTrue(diff.isEmpty());
  }

  @Test
  void test_distance_0() {
    ClassNode node1 = base;
    ClassNode node2 = new ClassNode();
    node2.visit(Opcodes.V1_8, 0, "TestClass", null, "java/lang/Object", null);
    node2.visitEnd();
    ClassDiff diff = ClassDiffUtils.diff(node1, node2);
    Assertions.assertEquals(0, diff.distance());
  }

  @Test
  void test_diff_1() {
    ClassNode node1 = base;
    ClassNode node2 = new ClassNode();
    node2.visit(Opcodes.V9, 0, "TestClass", null, "java/lang/Object", null);
    node2.visitEnd();
    ClassDiff diff = ClassDiffUtils.diff(node1, node2);
    Assertions.assertFalse(diff.isEmpty());
    Assertions.assertFalse(diff.version.isEmpty());
  }

  @Test
  void test_diff_2() {
    ClassNode node1 = base;
    ClassNode node2 = new ClassNode();
    node2.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "TestClass", null, "java/lang/Object", null);
    node2.visitEnd();
    ClassDiff diff = ClassDiffUtils.diff(node1, node2);
    Assertions.assertFalse(diff.isEmpty());
    Assertions.assertFalse(diff.access.isEmpty());
  }

  @Test
  void test_diff_3() {
    ClassNode node1 = base;
    ClassNode node2 = new ClassNode();
    node2.visit(Opcodes.V1_8, 0, "TestClass2", null, "java/lang/Object", null);
    node2.visitEnd();
    ClassDiff diff = ClassDiffUtils.diff(node1, node2);
    Assertions.assertFalse(diff.isEmpty());
    Assertions.assertFalse(diff.name.isEmpty());
  }

  @Test
  void test_diff_4() {
    ClassNode node1 = base;
    ClassNode node2 = new ClassNode();
    node2.visit(Opcodes.V1_8, 0, "TestClass", "TestClass", "java/lang/Object", null);
    node2.visitEnd();
    ClassDiff diff = ClassDiffUtils.diff(node1, node2);
    Assertions.assertFalse(diff.isEmpty());
    Assertions.assertFalse(diff.signature.isEmpty());
  }

  @Test
  void test_diff_5() {
    ClassNode node1 = base;
    ClassNode node2 = new ClassNode();
    node2.visit(Opcodes.V1_8, 0, "TestClass", null, "SomeClass", null);
    node2.visitEnd();
    ClassDiff diff = ClassDiffUtils.diff(node1, node2);
    Assertions.assertFalse(diff.isEmpty());
    Assertions.assertFalse(diff.superName.isEmpty());
  }

  @Test
  void test_diff_6() {
    ClassNode node1 = base;
    ClassNode node2 = new ClassNode();
    node2.visit(Opcodes.V1_8, 0, "TestClass", null, "java/lang/Object", new String[]{"java/io/Serializable"});
    node2.visitEnd();
    ClassDiff diff = ClassDiffUtils.diff(node1, node2);
    Assertions.assertFalse(diff.isEmpty());
    Assertions.assertFalse(diff.interfaces.isEmpty());
  }

  private ClassDiff diff(String oldClassPath, String newClassPath) {
    var oldClassNode = TestUtils.readClassNode(oldClassPath);
    var newClassNode = TestUtils.readClassNode(newClassPath);
    return ClassDiffUtils.diff(oldClassNode, newClassNode);
  }

  @Test
  void test_diff_sample_0() {
    ClassDiff diff = diff("/C1.class", "/C2.class");
    Assertions.assertFalse(diff.isEmpty());
    Assertions.assertFalse(diff.name.isEmpty());
  }

  @Test
  void test_diff_sample_1() {
    ClassDiff diff = diff("/C10.class", "/C11.class");
    Assertions.assertFalse(diff.isEmpty());
    Assertions.assertFalse(diff.methods.isEmpty());
  }
}