package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.BiHashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import java.io.*;
import java.util.Map;
import java.util.Objects;

class TryCatchBlockNodeHelperTest {
  LabelNode l0 = new LabelNode();
  LabelNode l1 = new LabelNode();
  LabelNode l2 = new LabelNode();
  LabelNode l3 = new LabelNode();

  @Test
  void test_equals_0() {
    TryCatchBlockNode node1 = new TryCatchBlockNode(l0, l1, l2, "java/lang/Exception");
    TryCatchBlockNode node2 = new TryCatchBlockNode(l0, l1, l2, "java/lang/Exception");
    Assertions.assertTrue(TryCatchBlockNodeHelper.equals(node1, node2));
  }

  @Test
  void test_equals_1() {
    TryCatchBlockNode node1 = new TryCatchBlockNode(l0, l1, l2, "java/lang/Exception");
    TryCatchBlockNode node2 = new TryCatchBlockNode(l0, l1, l2, "java/lang/RuntimeException");
    Assertions.assertFalse(TryCatchBlockNodeHelper.equals(node1, node2));
  }

  @Test
  void test_equals_2() {
    TryCatchBlockNode node1 = new TryCatchBlockNode(l0, l1, l2, "java/lang/Exception");
    Assertions.assertFalse(TryCatchBlockNodeHelper.equals(node1, null));
  }

  @Test
  void test_equals_3() {
    TryCatchBlockNode node2 = new TryCatchBlockNode(l0, l1, l2, "java/lang/Exception");
    Assertions.assertFalse(TryCatchBlockNodeHelper.equals(null, node2));
  }

  @Test
  void test_equals_4() {
    Assertions.assertTrue(TryCatchBlockNodeHelper.equals(null, null));
  }

  @Test
  void test_equals_5() {
    TryCatchBlockNode node1 = new TryCatchBlockNode(l0, l1, l2, null);
    TryCatchBlockNode node2 = new TryCatchBlockNode(l1, l1, l2, null);
    Assertions.assertFalse(TryCatchBlockNodeHelper.equals(node1, node2));
  }

  @Test
  void test_equals_6() {
    TryCatchBlockNode node1 = new TryCatchBlockNode(l0, l1, l2, null);
    TryCatchBlockNode node2 = new TryCatchBlockNode(l0, l2, l2, null);
    Assertions.assertFalse(TryCatchBlockNodeHelper.equals(node1, node2));
  }

  @Test
  void test_equals_7() {
    TryCatchBlockNode node1 = new TryCatchBlockNode(l0, l1, l2, null);
    TryCatchBlockNode node2 = new TryCatchBlockNode(l0, l1, l3, null);
    Assertions.assertFalse(TryCatchBlockNodeHelper.equals(node1, node2));
  }

  @Test
  void test_equals_8() {
    TryCatchBlockNode node1 = new TryCatchBlockNode(l0, l0, l0, null);
    TryCatchBlockNode node2 = new TryCatchBlockNode(l1, l1, l1, null);
    Map<LabelNode, LabelNode> labelMap = Map.of(l0, l1);
    Assertions.assertTrue(TryCatchBlockNodeHelper.equals(node1, node2, (lA, lB) -> Objects.equals(labelMap.get(lA), lB)));
  }

  @Test
  void test_readWrite() throws IOException {
    TryCatchBlockNode node1 = new TryCatchBlockNode(l0, l1, l2, null);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    BiHashMap<LabelNode, Integer> labelToIndex = new BiHashMap<>();
    TryCatchBlockNodeHelper.write(node1, dos, l -> labelToIndex.computeIfAbsent(l, k -> labelToIndex.size()));

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    DataInputStream dis = new DataInputStream(bais);
    TryCatchBlockNode read = TryCatchBlockNodeHelper.read(dis, labelToIndex::getKey);
    Assertions.assertTrue(TryCatchBlockNodeHelper.equals(node1, read));
  }
}
