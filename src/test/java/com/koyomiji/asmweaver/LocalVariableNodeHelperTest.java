package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.BinaryReader;
import com.koyomiji.asmweaver.io.BinaryWriter;
import com.koyomiji.asmweaver.util.AutoIncrementBiHashMap;
import com.koyomiji.asmweaver.util.tuple.Triplet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;

import java.io.*;
import java.util.Map;
import java.util.Objects;

import static com.koyomiji.asmweaver.LabelNodes.*;

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
    LocalVariableNode node1 = new LocalVariableNode("name", "desc", "signature", l0, l1, 0);
    LocalVariableNode node2 = new LocalVariableNode("name", "desc", "signature", l2, l3, 0);
    Map<LabelNode, LabelNode> labelMap = Map.of(
            l0, l2,
            l1, l3
    );
    Map<Triplet<LabelNode, LabelNode, Integer>, Triplet<LabelNode, LabelNode, Integer>> localMap = Map.of(
            Triplet.of(l0, l1, 0), Triplet.of(l2, l3, 0)
    );
    Assertions.assertTrue(LocalVariableNodeHelper.equals(node1, node2, (a, b) -> Objects.equals(labelMap.get(a), b), (a, b) -> Objects.equals(localMap.get(a), b)));
  }

  @Test
  void test_equals_11() {
    LocalVariableNode node1 = new LocalVariableNode("name", "desc", "signature", l0, l1, 0);
    LocalVariableNode node2 = new LocalVariableNode("name", "desc", "signature", l2, l3, 1);
    Map<LabelNode, LabelNode> labelMap = Map.of(
            l0, l2,
            l1, l3
    );
    Map<Triplet<LabelNode, LabelNode, Integer>, Triplet<LabelNode, LabelNode, Integer>> localMap = Map.of(
            Triplet.of(l0, l1, 0), Triplet.of(l2, l3, 1)
    );
    Assertions.assertTrue(LocalVariableNodeHelper.equals(node1, node2, (a, b) -> Objects.equals(labelMap.get(a), b), (a, b) -> Objects.equals(localMap.get(a), b)));
  }

  @Test
  void test_readWrite() throws IOException {
    LocalVariableNode node1 = new LocalVariableNode("name", "desc", "signature", l0, l1, 0);
    AutoIncrementBiHashMap<LabelNode> labelToIndex = new AutoIncrementBiHashMap<>();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BinaryWriter dos = new BinaryWriter(baos);
    LocalVariableNodeHelper.write(node1, dos, labelToIndex::get);

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    BinaryReader dis = new BinaryReader(bais);
    LocalVariableNode read = LocalVariableNodeHelper.read(dis, labelToIndex::getKey);
    Assertions.assertTrue(LocalVariableNodeHelper.equals(node1, read));
  }
}
