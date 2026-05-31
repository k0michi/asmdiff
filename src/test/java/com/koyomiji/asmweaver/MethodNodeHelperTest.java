package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.AutoIncrementBiHashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.TypeReference;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

class MethodNodeHelperTest {
  static List<MethodNode> generateUnique() {
    List<MethodNode> list = new ArrayList<>();

    MethodNode node1 = new MethodNode(0, "method", "()V", null, null);
    list.add(node1);

    MethodNode node2 = new MethodNode(0, "method_", "()V", null, null);
    list.add(node2);

    MethodNode node3 = new MethodNode(0, "method", "(I)V", null, null);
    list.add(node3);

    MethodNode node4 = new MethodNode(0, "method", "()V", "()V", null);
    list.add(node4);

    MethodNode node5 = new MethodNode(0, "method", "()V", null, new String[]{"java/lang/Exception"});
    list.add(node5);

    MethodNode node6 = new MethodNode(0, "method", "()V", null, null);
    node6.parameters = List.of(new ParameterNode("param", 0));
    list.add(node6);

    MethodNode node7 = new MethodNode(0, "method", "()V", null, null);
    node7.visibleAnnotations = List.of(new AnnotationNode("LA;"));
    list.add(node7);

    MethodNode node8 = new MethodNode(0, "method", "()V", null, null);
    node8.invisibleAnnotations = List.of(new AnnotationNode("LA;"));
    list.add(node8);

    MethodNode node9 = new MethodNode(0, "method", "()V", null, null);
    node9.visibleTypeAnnotations = List.of(
            new TypeAnnotationNode(TypeReference.CLASS_TYPE_PARAMETER, TypePath.fromString("."), "LA;")
    );
    list.add(node9);

    MethodNode node10 = new MethodNode(0, "method", "()V", null, null);
    node10.invisibleTypeAnnotations = List.of(
            new TypeAnnotationNode(TypeReference.CLASS_TYPE_PARAMETER, TypePath.fromString("."), "LA;")
    );
    list.add(node10);

    MethodNode node11 = new MethodNode(0, "method", "()V", null, null);
    node11.annotationDefault = 1;
    list.add(node11);

    MethodNode node12 = new MethodNode(0, "method", "()V", null, null);
    node12.visibleAnnotableParameterCount = 1;
    list.add(node12);

    MethodNode node13 = new MethodNode(0, "method", "()V", null, null);
    node13.visibleParameterAnnotations = new List[]{
            List.of(new AnnotationNode("LA;"))
    };
    list.add(node13);

    MethodNode node14 = new MethodNode(0, "method", "()V", null, null);
    node14.invisibleAnnotableParameterCount = 1;
    list.add(node14);

    MethodNode node15 = new MethodNode(0, "method", "()V", null, null);
    node15.invisibleParameterAnnotations = new List[]{
            List.of(new AnnotationNode("LA;"))
    };
    list.add(node15);

    MethodNode node16 = new MethodNode(0, "method", "()V", null, null);
    node16.instructions.add(new InsnNode(Opcodes.NOP));
    list.add(node16);

    LabelNode l0 = new LabelNode(), l1 = new LabelNode(), l2 = new LabelNode();
    MethodNode node17 = new MethodNode(0, "method", "()V", null, null);
    node17.instructions.add(l0);
    node17.instructions.add(new InsnNode(Opcodes.NOP));
    node17.instructions.add(l1);
    node17.instructions.add(new InsnNode(Opcodes.NOP));
    node17.instructions.add(l2);
    node17.tryCatchBlocks = List.of(
            new TryCatchBlockNode(l0, l1, l2, null)
    );
    list.add(node17);

    MethodNode node18 = new MethodNode(0, "method", "()V", null, null);
    node18.maxStack = 1;
    list.add(node18);

    MethodNode node19 = new MethodNode(0, "method", "()V", null, null);
    node19.maxLocals = 1;
    list.add(node19);

    l0 = new LabelNode();
    l1 = new LabelNode();
    MethodNode node20 = new MethodNode(0, "method", "()V", null, null);
    node20.instructions.add(l0);
    node20.instructions.add(new InsnNode(Opcodes.NOP));
    node20.instructions.add(l1);
    node20.localVariables = List.of(
            new LocalVariableNode("name", "desc", "signature", l0, l1, 0)
    );
    list.add(node20);

    l0 = new LabelNode();
    l1 = new LabelNode();
    MethodNode node21 = new MethodNode(0, "method", "()V", null, null);
    node21.instructions.add(l0);
    node21.instructions.add(new InsnNode(Opcodes.NOP));
    node21.instructions.add(l1);
    node21.visibleLocalVariableAnnotations = List.of(
            new LocalVariableAnnotationNode(TypeReference.CLASS_TYPE_PARAMETER, TypePath.fromString("."), new LabelNode[]{l0}, new LabelNode[]{l1}, new int[]{0}, "LA;")
    );
    list.add(node21);

    l0 = new LabelNode();
    l1 = new LabelNode();
    MethodNode node22 = new MethodNode(0, "method", "()V", null, null);
    node22.instructions.add(l0);
    node22.instructions.add(new InsnNode(Opcodes.NOP));
    node22.instructions.add(l1);
    node22.invisibleLocalVariableAnnotations = List.of(
            new LocalVariableAnnotationNode(TypeReference.CLASS_TYPE_PARAMETER, TypePath.fromString("."), new LabelNode[]{l0}, new LabelNode[]{l1}, new int[]{0}, "LA;")
    );
    list.add(node22);

    // LineNumber

    l0 = new LabelNode();
    l1 = new LabelNode();
    MethodNode node23 = new MethodNode(0, "method", "()V", null, null);
    node23.instructions.add(l0);
    node23.instructions.add(new LineNumberNode(1, l0));
    node23.instructions.add(new InsnNode(Opcodes.NOP));
    list.add(node23);

    l0 = new LabelNode();
    l1 = new LabelNode();
    MethodNode node24 = new MethodNode(0, "method", "()V", null, null);
    node24.instructions.add(l0);
    node24.instructions.add(new LineNumberNode(1, l0));
    node24.instructions.add(new InsnNode(Opcodes.NOP));
    node24.instructions.add(l1);
    node24.instructions.add(new LineNumberNode(1, l1));
    node24.instructions.add(new InsnNode(Opcodes.NOP));
    list.add(node24);

    // Jump

    l0 = new LabelNode();
    l1 = new LabelNode();
    MethodNode node25 = new MethodNode(0, "method", "()V", null, null);
    node25.instructions.add(l0);
    node25.instructions.add(new LineNumberNode(1, l0));
    node25.instructions.add(new JumpInsnNode(Opcodes.GOTO, l1));
    node25.instructions.add(new InsnNode(Opcodes.NOP));
    node25.instructions.add(l1);
    node25.instructions.add(new LineNumberNode(1, l1));
    node25.instructions.add(new JumpInsnNode(Opcodes.GOTO, l0));
    list.add(node25);

    // no localVariable

    MethodNode node26 = new MethodNode(Opcodes.ACC_ABSTRACT, "method", "()V", null, null);
    list.add(node26);

    return list;
  }

  @Test
  void test_equals() {
    TestUtils.verifyEquals(MethodNodeHelperTest::generateUnique, MethodNodeHelper::equalsNormalizeLabels);
  }

  @Test
  void test_hashCode() {
    TestUtils.verifyHashCode(MethodNodeHelperTest::generateUnique, MethodNodeHelper::hashCodeNormalizeLabels);
  }

  @Test
  void test_readWrite_roundTrip() throws IOException {
    AutoIncrementBiHashMap<LabelNode> labels = new AutoIncrementBiHashMap<>();

    TestUtils.verifyRoundTrip(
            MethodNodeHelperTest::generateUnique,
            MethodNodeHelper::write,
            MethodNodeHelper::read,
            MethodNodeHelper::equalsNormalizeLabels
    );
  }

  @Test
  void test_equals_labelMap_0() throws IOException {
    LabelNodes.reset();

    MethodNode node1 = new MethodNode(0, "method", "()V", null, null);
    node1.instructions.add(LabelNodes.l0);
    MethodNode node2 = new MethodNode(0, "method", "()V", null, null);
    node2.instructions.add(LabelNodes.l1);

    Assertions.assertFalse(MethodNodeHelper.equals(node1, node2, Objects::equals));

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

    Assertions.assertFalse(MethodNodeHelper.equals(node1, node2, Objects::equals));

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
