package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class AbstractInsnNodeHelperTest {
  static List<AbstractInsnNode> UNIQUE_NODES = List.of(
          new InsnNode(Opcodes.NOP),
          new InsnNode(Opcodes.ACONST_NULL),
          new IntInsnNode(Opcodes.BIPUSH, 123),
          new IntInsnNode(Opcodes.SIPUSH, 123),
          new IntInsnNode(Opcodes.SIPUSH, 12345),
          new VarInsnNode(Opcodes.ILOAD, 0),
          new VarInsnNode(Opcodes.ILOAD, 1),
          new VarInsnNode(Opcodes.ISTORE, 0),
          new TypeInsnNode(Opcodes.NEW, "java/lang/Object"),
          new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/String"),
          new FieldInsnNode(Opcodes.GETFIELD, "java/lang/Object", "field", "I"),
          new FieldInsnNode(Opcodes.GETFIELD, "java/lang/Object", "field2", "I"),
          new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "method", "()V", false),
          new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "method2", "()V", false),
          new InvokeDynamicInsnNode("method", "()V", new Handle(Opcodes.H_INVOKESTATIC, "java/lang/Object", "bootstrap", "()V"), new Object[0], false),
          new InvokeDynamicInsnNode("method", "()V", new Handle(Opcodes.H_INVOKESTATIC, "java/lang/Object", "bootstrap2", "()V"), new Object[0], false),
          new JumpInsnNode(Opcodes.IFEQ, new LabelNode()),
          new JumpInsnNode(Opcodes.IFEQ, new LabelNode()),
          new LabelNode(),
          new LabelNode(),
          new LdcInsnNode("constant"),
          new LdcInsnNode(123),
          new IincInsnNode(0, 1),
          new IincInsnNode(0, 2),
          new TableSwitchInsnNode(0, 1, new LabelNode(), new LabelNode(), new LabelNode()),
          new TableSwitchInsnNode(0, 1, new LabelNode(), new LabelNode(), new LabelNode()),
          new LookupSwitchInsnNode(new LabelNode(), new int[]{0}, new LabelNode[]{new LabelNode()}),
          new LookupSwitchInsnNode(new LabelNode(), new int[]{0}, new LabelNode[]{new LabelNode()}),
          new MultiANewArrayInsnNode("java/lang/Object", 2),
          new MultiANewArrayInsnNode("java/lang/Object", 3),
          new FrameNode(Opcodes.F_FULL, 0, null, 0, null),
          new FrameNode(Opcodes.F_FULL, 1, new Object[]{"java/lang/Object"}, 0, null),
          new LineNumberNode(123, new LabelNode()),
          new LineNumberNode(123, new LabelNode())
  );

  @Test
  void test_equals_0() {
    AbstractInsnNode n1 = new InsnNode(Opcodes.NOP);
    Assertions.assertTrue(AbstractInsnNodeHelper.equals(null, null, Objects::equals, Objects::equals));
    Assertions.assertFalse(AbstractInsnNodeHelper.equals(n1, null, Objects::equals, Objects::equals));
    Assertions.assertFalse(AbstractInsnNodeHelper.equals(null, n1, Objects::equals, Objects::equals));
  }

  static Stream<Arguments> provideAllPairs() {
    return IntStream.range(0, UNIQUE_NODES.size()).boxed().flatMap(i ->
            IntStream.range(0, UNIQUE_NODES.size()).mapToObj(j ->
                    Arguments.of(i, j, UNIQUE_NODES.get(i), UNIQUE_NODES.get(j))
            )
    );
  }

  @ParameterizedTest(name = "i={0}, j={1}")
  @MethodSource("provideAllPairs")
  void testEquality(int i, int j, AbstractInsnNode nodeA, AbstractInsnNode nodeB) {
    if (i == j) {
      Assertions.assertTrue(AbstractInsnNodeHelper.equals(nodeA, nodeB, Objects::equals, Objects::equals));
    } else {
      Assertions.assertFalse(AbstractInsnNodeHelper.equals(nodeA, nodeB, Objects::equals, Objects::equals));
    }
  }
}
