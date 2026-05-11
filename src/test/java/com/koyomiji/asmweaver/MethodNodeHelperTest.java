package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
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
  void test_equals_0() {
    List<MethodNode> unique = generateUnique();

    for (int i = 0; i < unique.size(); i++) {
      for (int j = 0; j < unique.size(); j++) {
        if (i == j) {
          Assertions.assertTrue(MethodNodeHelper.equals(unique.get(i), unique.get(j)));
        } else {
          Assertions.assertFalse(MethodNodeHelper.equals(unique.get(i), unique.get(j)));
        }
      }
    }
  }

  @Test
  void test_hashCode() {
    List<MethodNode> unique = generateUnique();

    for (int i = 0; i < unique.size(); i++) {
      Assertions.assertEquals(MethodNodeHelper.hashCode(unique.get(i)), MethodNodeHelper.hashCode(unique.get(i)));
    }
  }
}
