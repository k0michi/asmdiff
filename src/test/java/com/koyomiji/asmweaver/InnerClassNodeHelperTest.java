package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.InnerClassNode;

class InnerClassNodeHelperTest {
  @Test
  void test_equals_0() {
    InnerClassNode node1 = new InnerClassNode("A", "B", "C", 0);
    InnerClassNode node2 = new InnerClassNode("A", "B", "C", 0);
    Assertions.assertTrue(InnerClassNodeHelper.equals(node1, node2));
  }

  @Test
  void test_equals_1() {
    InnerClassNode node1 = new InnerClassNode("A", "B", "C", 0);
    InnerClassNode node2 = new InnerClassNode("A_", "B", "C", 0);
    Assertions.assertFalse(InnerClassNodeHelper.equals(node1, node2));
  }

  @Test
  void test_equals_2() {
    InnerClassNode node1 = new InnerClassNode("A", "B", "C", 0);
    InnerClassNode node2 = new InnerClassNode("A", "B_", "C", 0);
    Assertions.assertFalse(InnerClassNodeHelper.equals(node1, node2));
  }

  @Test
  void test_equals_3() {
    InnerClassNode node1 = new InnerClassNode("A", "B", "C", 0);
    InnerClassNode node2 = new InnerClassNode("A", "B", "C_", 0);
    Assertions.assertFalse(InnerClassNodeHelper.equals(node1, node2));
  }

  @Test
  void test_equals_4() {
    InnerClassNode node1 = new InnerClassNode("A", "B", "C", 0);
    InnerClassNode node2 = new InnerClassNode("A", "B", "C", 1);
    Assertions.assertFalse(InnerClassNodeHelper.equals(node1, node2));
  }

  @Test
  void test_equals_5() {
    InnerClassNode node1 = new InnerClassNode("A", "B", "C", 0);
    Assertions.assertFalse(InnerClassNodeHelper.equals(node1, null));
  }

  @Test
  void test_equals_6() {
    InnerClassNode node2 = new InnerClassNode("A", "B", "C", 0);
    Assertions.assertFalse(InnerClassNodeHelper.equals(null, node2));
  }

  @Test
  void test_equals_7() {
    Assertions.assertTrue(InnerClassNodeHelper.equals(null, null));
  }
}
