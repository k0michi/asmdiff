package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;

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
}
