package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.BinaryReader;
import com.koyomiji.asmweaver.io.BinaryWriter;
import com.koyomiji.asmweaver.util.AutoIncrementBiHashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LabelNode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

  @Test
  void test_patch() {
    var unique1 = ClassNodeHelperTest.generateUnique();
    var unique2 = ClassNodeHelperTest.generateUnique();

    for (int i = 0; i < unique1.size(); i++) {
      for (int j = 0; j < unique2.size(); j++) {
        ClassNode node1 = unique1.get(i);
        ClassNode node2 = unique2.get(j);
        ClassDiff diff = ClassDiffUtils.diff(node1, node2);

        ClassNode patched = ClassDiffUtils.patch(node1, diff);

        Assertions.assertTrue(ClassNodeHelper.equalsNormalizeLabels(node2, patched), "i=" + i + ", j=" + j);
      }
    }
  }

  @Test
  void test_diff_readWrite() throws IOException {
    var unique1 = ClassNodeHelperTest.generateUnique();
    var unique2 = ClassNodeHelperTest.generateUnique();

    for (int i = 0; i < unique1.size(); i++) {
      for (int j = 0; j < unique2.size(); j++) {
        ClassNode node1 = unique1.get(i);
        ClassNode node2 = unique2.get(j);
        ClassDiff diff = ClassDiffUtils.diff(node1, node2);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ClassDiffUtils.write(diff, new BinaryWriter(baos));
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ClassDiff read = ClassDiffUtils.read(new BinaryReader(bais));

        ClassNode patched = ClassDiffUtils.patch(node1, read);

        Assertions.assertTrue(ClassNodeHelper.equalsNormalizeLabels(node2, patched), "i=" + i + ", j=" + j);
      }
    }
  }

  @Test
  void test_isEmpty() {
    var unique1 = ClassNodeHelperTest.generateUnique();

    for (int i = 0; i < unique1.size(); i++) {
      ClassNode node1 = unique1.get(i);
      ClassDiff diff12 = ClassDiffUtils.diff(node1, node1);

      Assertions.assertTrue(diff12.isEmpty(), "i=" + i);
    }
  }

  @Test
  void test_commute_empty() throws ConflictException {
    var unique1 = ClassNodeHelperTest.generateUnique();

    for (int i = 0; i < unique1.size(); i++) {
      ClassNode node1 = unique1.get(i);
      ClassDiff diff12 = ClassDiffUtils.diff(node1, node1);
      ClassDiff diff23 = ClassDiffUtils.diff(node1, node1);

      var commuted = ClassDiffUtils.commute(diff12, diff23);

      Assertions.assertTrue(commuted.first.isEmpty(), "i=" + i);
      Assertions.assertTrue(commuted.second.isEmpty(), "i=" + i);
    }
  }

  @Test
  void test_invert_empty() {
    var unique1 = ClassNodeHelperTest.generateUnique();

    for (int i = 0; i < unique1.size(); i++) {
      ClassNode node1 = unique1.get(i);
      ClassDiff diff12 = ClassDiffUtils.diff(node1, node1);

      var inverted = ClassDiffUtils.invert(diff12);

      Assertions.assertTrue(inverted.isEmpty(), "i=" + i);
    }
  }

  @Test
  void test_compose_empty() {
    var unique1 = ClassNodeHelperTest.generateUnique();

    for (int i = 0; i < unique1.size(); i++) {
      ClassNode node1 = unique1.get(i);
      ClassDiff diff12 = ClassDiffUtils.diff(node1, node1);
      ClassDiff diff23 = ClassDiffUtils.diff(node1, node1);

      var composed = ClassDiffUtils.compose(diff12, diff23);

      Assertions.assertTrue(composed.isEmpty(), "i=" + i);
    }
  }
}