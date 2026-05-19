package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.AutoIncrementBiHashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

class MethodNodeHelperTest {
  List<MethodNode> generateUnique() {
    MethodNode node1 = new MethodNode(0, "method", "()V", null, null);
    MethodNode node2 = new MethodNode(0, "method_", "()V", null, null);
    MethodNode node3 = new MethodNode(0, "method", "(I)V", null, null);
    MethodNode node4 = new MethodNode(0, "method", "()V", "()V", null);
    MethodNode node5 = new MethodNode(0, "method", "()V", null, new String[]{"java/lang/Exception"});
    MethodNode node6 = new MethodNode(0, "method", "()V", null, null);
    node6.parameters = List.of(new ParameterNode("param", 0));

    return List.of(node1, node2, node3, node4, node5, node6);
  }

  @Test
  void test_equals() {
    TestUtils.verifyEquals(this::generateUnique, MethodNodeHelper::equals);
  }

  @Test
  void test_hashCode() {
    TestUtils.verifyHashCode(this::generateUnique, MethodNodeHelper::hashCode);
  }

  @Test
  void test_readWrite_roundTrip() throws IOException {
    AutoIncrementBiHashMap<LabelNode> labels = new AutoIncrementBiHashMap<>();

    TestUtils.verifyRoundTrip(
            this::generateUnique,
            (value, out) -> MethodNodeHelper.write(value, out, labels::get),
            (in) -> MethodNodeHelper.read(in, labels::getKey),
            MethodNodeHelper::equals
    );
  }

  @Test
  void test_equals_labelMap_0() throws IOException {
    LabelNodes.reset();

    MethodNode node1 = new MethodNode(0, "method", "()V", null, null);
    node1.instructions.add(LabelNodes.l0);
    MethodNode node2 = new MethodNode(0, "method", "()V", null, null);
    node2.instructions.add(LabelNodes.l1);

    Assertions.assertFalse(MethodNodeHelper.equals(node1, node2));

    HashMap<LabelNode, LabelNode> map = new HashMap<>();
    Assertions.assertTrue(
            MethodNodeHelper.equals(node1, node2, (l1, l2) -> MapHelper.putIfAbsentAndTest(map, l1, l2))
    );
  }

  @Test
  void test_equals_labelMap_1() throws IOException {
    LabelNodes.reset();

    MethodNode node1 = new MethodNode(0, "method", "()V", null, null);
    node1.instructions.add(LabelNodes.l0);
    node1.instructions.add(new InsnNode(Opcodes.NOP));
    node1.instructions.add(LabelNodes.l1);
    node1.localVariables.add(new LocalVariableNode("name", "desc", "signature", LabelNodes.l0, LabelNodes.l1, 0));
    MethodNode node2 = new MethodNode(0, "method", "()V", null, null);
    node2.instructions.add(LabelNodes.l2);
    node2.instructions.add(new InsnNode(Opcodes.NOP));
    node2.instructions.add(LabelNodes.l3);
    node2.localVariables.add(new LocalVariableNode("name", "desc", "signature", LabelNodes.l2, LabelNodes.l3, 0));

    Assertions.assertFalse(MethodNodeHelper.equals(node1, node2));

    HashMap<LabelNode, LabelNode> map = new HashMap<>();
    Assertions.assertTrue(
            MethodNodeHelper.equals(node1, node2, (l1, l2) -> MapHelper.putIfAbsentAndTest(map, l1, l2))
    );
  }

  @Test
  void test_hashCode_labelMap_0() throws IOException {
    LabelNodes.reset();

    MethodNode node1 = new MethodNode(0, "method", "()V", null, null);
    node1.instructions.add(LabelNodes.l0);
    node1.instructions.add(new InsnNode(Opcodes.NOP));
    node1.instructions.add(LabelNodes.l1);
    node1.localVariables.add(new LocalVariableNode("name", "desc", "signature", LabelNodes.l0, LabelNodes.l1, 0));
    MethodNode node2 = new MethodNode(0, "method", "()V", null, null);
    node2.instructions.add(LabelNodes.l2);
    node2.instructions.add(new InsnNode(Opcodes.NOP));
    node2.instructions.add(LabelNodes.l3);
    node2.localVariables.add(new LocalVariableNode("name", "desc", "signature", LabelNodes.l2, LabelNodes.l3, 0));

    Assertions.assertEquals(
            MethodNodeHelper.hashCode(node1, (new AutoIncrementBiHashMap<>())::get),
            MethodNodeHelper.hashCode(node2, (new AutoIncrementBiHashMap<>())::get)
    );
  }
}
