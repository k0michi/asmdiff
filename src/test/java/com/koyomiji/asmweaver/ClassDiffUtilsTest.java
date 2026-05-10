package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

class ClassDiffUtilsTest {
  @Test
  void test_diff_0() {
    ClassNode node1 = new ClassNode();
    node1.visit(Opcodes.V1_8, 0, "TestClass", null, "java/lang/Object", null);
    node1.visitEnd();
    ClassNode node2 = new ClassNode();
    node2.visit(Opcodes.V1_8, 0, "TestClass", null, "java/lang/Object", null);
    node2.visitEnd();
    ClassDiff diff = ClassDiffUtils.diff(node1, node2);
    Assertions.assertTrue(diff.isEmpty());
  }
}
