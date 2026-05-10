package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.MethodNode;

class MethodDiffUtilsTest {
  @Test
  void test_diff_0() {
    MethodNode node1 = new MethodNode(0, "method1", "()V", null, null);
    MethodNode node2 = new MethodNode(0, "method1", "()V", null, null);
    MethodDiff diff = MethodDiffUtils.diff(node1, node2);
    Assertions.assertTrue(diff.isEmpty());
  }
}
