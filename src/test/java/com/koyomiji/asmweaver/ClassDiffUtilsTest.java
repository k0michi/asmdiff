package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.BinaryReader;
import com.koyomiji.asmweaver.io.BinaryWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

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
    Assertions.assertNull(diff);
  }

  @Test
  void test_distance_0() {
    ClassNode node1 = base;
    ClassNode node2 = new ClassNode();
    node2.visit(Opcodes.V1_8, 0, "TestClass", null, "java/lang/Object", null);
    node2.visitEnd();
    ClassDiff diff = ClassDiffUtils.diff(node1, node2);
    Assertions.assertEquals(0, ClassDiffUtils.distance(diff));
  }

  @Test
  void test_diff_1() {
    ClassNode node1 = base;
    ClassNode node2 = new ClassNode();
    node2.visit(Opcodes.V9, 0, "TestClass", null, "java/lang/Object", null);
    node2.visitEnd();
    ClassDiff diff = ClassDiffUtils.diff(node1, node2);
    Assertions.assertNotNull(diff.version);
  }

  @Test
  void test_diff_2() {
    ClassNode node1 = base;
    ClassNode node2 = new ClassNode();
    node2.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "TestClass", null, "java/lang/Object", null);
    node2.visitEnd();
    ClassDiff diff = ClassDiffUtils.diff(node1, node2);
    Assertions.assertNotNull(diff.access);
  }

  @Test
  void test_diff_3() {
    ClassNode node1 = base;
    ClassNode node2 = new ClassNode();
    node2.visit(Opcodes.V1_8, 0, "TestClass2", null, "java/lang/Object", null);
    node2.visitEnd();
    ClassDiff diff = ClassDiffUtils.diff(node1, node2);
    Assertions.assertNotNull(diff.name);
  }

  @Test
  void test_diff_4() {
    ClassNode node1 = base;
    ClassNode node2 = new ClassNode();
    node2.visit(Opcodes.V1_8, 0, "TestClass", "TestClass", "java/lang/Object", null);
    node2.visitEnd();
    ClassDiff diff = ClassDiffUtils.diff(node1, node2);
    Assertions.assertNotNull(diff.signature);
  }

  @Test
  void test_diff_5() {
    ClassNode node1 = base;
    ClassNode node2 = new ClassNode();
    node2.visit(Opcodes.V1_8, 0, "TestClass", null, "SomeClass", null);
    node2.visitEnd();
    ClassDiff diff = ClassDiffUtils.diff(node1, node2);
    Assertions.assertNotNull(diff.superName);
  }

  @Test
  void test_diff_6() {
    ClassNode node1 = base;
    ClassNode node2 = new ClassNode();
    node2.visit(Opcodes.V1_8, 0, "TestClass", null, "java/lang/Object", new String[]{"java/io/Serializable"});
    node2.visitEnd();
    ClassDiff diff = ClassDiffUtils.diff(node1, node2);
    Assertions.assertNotNull(diff.interfaces);
  }

  private ClassDiff diff(String oldClassPath, String newClassPath) {
    var oldClassNode = TestUtils.readClassNode(oldClassPath);
    var newClassNode = TestUtils.readClassNode(newClassPath);
    return ClassDiffUtils.diff(oldClassNode, newClassNode);
  }

  @Test
  void test_diff_sample_0() {
    ClassDiff diff = diff("/C1.class", "/C2.class");
    Assertions.assertNotNull(diff.name);
  }

  @Test
  void test_diff_sample_1() {
    ClassDiff diff = diff("/C10.class", "/C11.class");
    Assertions.assertNotNull(diff.methods);
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

      Assertions.assertNull(diff12, "i=" + i);
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

      Assertions.assertNull(commuted.first, "i=" + i);
      Assertions.assertNull(commuted.second, "i=" + i);
    }
  }

  @Test
  void test_invert_empty() {
    var unique1 = ClassNodeHelperTest.generateUnique();

    for (int i = 0; i < unique1.size(); i++) {
      ClassNode node1 = unique1.get(i);
      ClassDiff diff12 = ClassDiffUtils.diff(node1, node1);

      var inverted = ClassDiffUtils.invert(diff12);

      Assertions.assertNull(inverted, "i=" + i);
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

      Assertions.assertNull(composed, "i=" + i);
    }
  }

  @Test
  void test_commute_0() throws ConflictException {
    ClassNode node1 = new ClassNode();
    node1.visit(0, 0, "A", null, "java/lang/Object", null);
    ClassNode node2 = new ClassNode();
    node2.visit(0, 0, "A", "A", "java/lang/Object", null);
    ClassNode node3 = new ClassNode();
    node3.visit(0, 0, "A", "A", "java/lang/Object", new String[]{"B"});

    ClassDiff diff12 = ClassDiffUtils.diff(node1, node2);
    ClassDiff diff23 = ClassDiffUtils.diff(node2, node3);
    var commuted = ClassDiffUtils.commute(diff12, diff23);
    var patched = ClassDiffUtils.patch(ClassDiffUtils.patch(node1, commuted.first), commuted.second);
    Assertions.assertTrue(ClassNodeHelper.equalsNormalizeLabels(node3, patched));
  }

  @Test
  void test_merge() {
    var unique1 = ClassNodeHelperTest.generateUnique();
    var unique2 = ClassNodeHelperTest.generateUnique();

    // Disjoint insertions
    for (int i = 1; i < unique1.size(); i++) {
      for (int j = 1; j < unique2.size(); j++) {
        if (i != j) {
          ClassNode node1 = unique1.get(0);
          ClassNode node2 = unique1.get(i);
          ClassNode node3 = unique2.get(j);

          ClassDiff diff12 = ClassDiffUtils.diff(node1, node2);
          ClassDiff diff13 = ClassDiffUtils.diff(node1, node3);

          var merged = Assertions.assertDoesNotThrow(() -> ClassDiffUtils.merge(diff12, diff13));
          Assertions.assertNotNull(merged, "i=" + i + ", j=" + j);
          Assertions.assertTrue(ClassDiffUtils.distance(merged) > 0, "i=" + i + ", j=" + j);
        }
      }
    }
  }
}