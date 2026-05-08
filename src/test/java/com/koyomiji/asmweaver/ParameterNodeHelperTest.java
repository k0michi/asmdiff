package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ParameterNode;

public class ParameterNodeHelperTest {
  @Test
  void test_equals_0() {
    ParameterNode node1 = new ParameterNode("param1", 0);
    ParameterNode node2 = new ParameterNode("param1", 0);
    Assertions.assertTrue(ParameterNodeHelper.equals(node1, node2));
  }

  @Test
  void test_equals_1() {
    ParameterNode node1 = new ParameterNode("param1", 0);
    ParameterNode node2 = new ParameterNode("param2", 0);
    Assertions.assertFalse(ParameterNodeHelper.equals(node1, node2));
  }

  @Test
  void test_equals_2() {
    ParameterNode node1 = new ParameterNode("param1", 0);
    ParameterNode node2 = new ParameterNode("param1", 1);
    Assertions.assertFalse(ParameterNodeHelper.equals(node1, node2));
  }

  @Test
  void test_equals_3() {
    ParameterNode node1 = new ParameterNode("param1", 0);
    Assertions.assertFalse(ParameterNodeHelper.equals(node1, null));
  }

  @Test
  void test_equals_5() {
    ParameterNode node2 = new ParameterNode("param1", 0);
    Assertions.assertFalse(ParameterNodeHelper.equals(null, node2));
  }

  @Test
  void test_equals_6() {
    Assertions.assertTrue(ParameterNodeHelper.equals(null, null));
  }
}
