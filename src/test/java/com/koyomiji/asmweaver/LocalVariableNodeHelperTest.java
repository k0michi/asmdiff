package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;

import java.util.Map;
import java.util.Objects;

import static com.koyomiji.asmweaver.LabelNodes.l0;
import static com.koyomiji.asmweaver.LabelNodes.l1;

class LocalVariableNodeHelperTest {
  @Test
  void test_equals_0() {
    LocalVariableNode node1 = new LocalVariableNode("name", "desc", "signature", l0, l1, 0);
    LocalVariableNode node2 = new LocalVariableNode("name", "desc", "signature", l0, l1, 0);
    Assertions.assertTrue(LocalVariableNodeHelper.equals(node1, node2));
  }

  @Test
  void test_equals_1() {
    LocalVariableNode node1 = new LocalVariableNode("name", "desc", "signature", l0, l1, 0);
    LocalVariableNode node2 = new LocalVariableNode("name_", "desc", "signature", l0, l1, 0);
    Assertions.assertFalse(LocalVariableNodeHelper.equals(node1, node2));
  }

  @Test
  void test_equals_2() {
    LocalVariableNode node1 = new LocalVariableNode("name", "desc", "signature", l0, l1, 0);
    LocalVariableNode node2 = new LocalVariableNode("name", "desc_", "signature", l0, l1, 0);
    Assertions.assertFalse(LocalVariableNodeHelper.equals(node1, node2));
  }

  @Test
  void test_equals_3() {
    LocalVariableNode node1 = new LocalVariableNode("name", "desc", "signature", l0, l1, 0);
    LocalVariableNode node2 = new LocalVariableNode("name", "desc", "signature_", l0, l1, 0);
    Assertions.assertFalse(LocalVariableNodeHelper.equals(node1, node2));
  }

  @Test
  void test_equals_4() {
    LocalVariableNode node1 = new LocalVariableNode("name", "desc", "signature", l0, l1, 0);
    LocalVariableNode node2 = new LocalVariableNode("name", "desc", "signature", l1, l1, 0);
    Assertions.assertFalse(LocalVariableNodeHelper.equals(node1, node2));
  }

  @Test
  void test_equals_5() {
    LocalVariableNode node1 = new LocalVariableNode("name", "desc", "signature", l0, l1, 0);
    LocalVariableNode node2 = new LocalVariableNode("name", "desc", "signature", l0, l0, 0);
    Assertions.assertFalse(LocalVariableNodeHelper.equals(node1, node2));
  }

  @Test
  void test_equals_6() {
    LocalVariableNode node1 = new LocalVariableNode("name", "desc", "signature", l0, l1, 0);
    LocalVariableNode node2 = new LocalVariableNode("name", "desc", "signature", l0, l1, 1);
    Assertions.assertFalse(LocalVariableNodeHelper.equals(node1, node2));
  }

  @Test
  void test_equals_7() {
    LocalVariableNode node1 = new LocalVariableNode("name", "desc", "signature", l0, l1, 0);
    Assertions.assertFalse(LocalVariableNodeHelper.equals(node1, null));
  }

  @Test
  void test_equals_8() {
    LocalVariableNode node2 = new LocalVariableNode("name", "desc", "signature", l0, l1, 0);
    Assertions.assertFalse(LocalVariableNodeHelper.equals(null, node2));
  }

  @Test
  void test_equals_9() {
    Assertions.assertTrue(LocalVariableNodeHelper.equals(null, null));
  }

  @Test
  void test_equals_10() {
    LocalVariableNode node1 = new LocalVariableNode("name", "desc", "signature", l0, l0, 0);
    LocalVariableNode node2 = new LocalVariableNode("name", "desc", "signature", l1, l1, 0);
    Map<LabelNode, LabelNode> labelMap = Map.of(l0, l1);
    Map<Pair<LabelNode, Integer>, Pair<LabelNode, Integer>> localMap = Map.of(Pair.of(l0, 0), Pair.of(l1, 0));
    Assertions.assertTrue(LocalVariableNodeHelper.equals(node1, node2, (a, b) -> Objects.equals(labelMap.get(a), b), (a, b) -> Objects.equals(localMap.get(a), b)));
  }

  @Test
  void test_equals_11() {
    LocalVariableNode node1 = new LocalVariableNode("name", "desc", "signature", l0, l0, 0);
    LocalVariableNode node2 = new LocalVariableNode("name", "desc", "signature", l1, l1, 1);
    Map<LabelNode, LabelNode> labelMap = Map.of(l0, l1);
    Map<Pair<LabelNode, Integer>, Pair<LabelNode, Integer>> localMap = Map.of(Pair.of(l0, 0), Pair.of(l1, 1));
    Assertions.assertTrue(LocalVariableNodeHelper.equals(node1, node2, (a, b) -> Objects.equals(labelMap.get(a), b), (a, b) -> Objects.equals(localMap.get(a), b)));
  }
}
