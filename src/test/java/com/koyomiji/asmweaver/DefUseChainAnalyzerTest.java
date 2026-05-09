package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.analysis.DefUseChainAnalyzer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

class DefUseChainAnalyzerTest {
  @Test
  void test_analyze_0() throws AnalyzerException {
    DefUseChainAnalyzer analyzer = new DefUseChainAnalyzer();
    MethodNode methodNode = new MethodNode(Opcodes.ACC_PUBLIC, "method", "()V", null, null);
    methodNode.maxLocals = 1;
    methodNode.maxStack = 1;
    methodNode.instructions.add(new IntInsnNode(Opcodes.BIPUSH, 42));
    methodNode.instructions.add(new VarInsnNode(Opcodes.ISTORE, 0));
    methodNode.instructions.add(new VarInsnNode(Opcodes.ILOAD, 0));
    methodNode.instructions.add(new InsnNode(Opcodes.POP));
    methodNode.instructions.add(new InsnNode(Opcodes.RETURN));
    var result = analyzer.analyze("A", methodNode);
    Assertions.assertEquals(1, result.getGroups().size());

  }
}
