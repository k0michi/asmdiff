package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.InnerClassNode;

import java.util.List;

class InnerClassNodeHelperTest {
  static List<InnerClassNode> generateUnique() {
    return List.of(
            new InnerClassNode("A", "B", "C", 0),
            new InnerClassNode("A_", "B", "C", 0),
            new InnerClassNode("A", "B_", "C", 0),
            new InnerClassNode("A", "B", "C_", 0),
            new InnerClassNode("A", "B", "C", 1)
    );
  }

  @Test
  void test_equals_0() {
    var uniqueNodes1 = generateUnique();
    var uniqueNodes2 = generateUnique();

    for (int i = 0; i < uniqueNodes1.size(); i++) {
      Assertions.assertTrue(InnerClassNodeHelper.equals(uniqueNodes1.get(i), uniqueNodes2.get(i)));
    }
  }

  @Test
  void test_equals_1() {
    var uniqueNodes = generateUnique();

    for (int i = 0; i < uniqueNodes.size(); i++) {
      for (int j = 0; j < uniqueNodes.size(); j++) {
        if (i != j) {
          Assertions.assertFalse(InnerClassNodeHelper.equals(uniqueNodes.get(i), uniqueNodes.get(j)));
        }
      }
    }
  }

  @Test
  void test_hashCode_0() {
    var uniqueNodes = generateUnique();

    for (int i = 0; i < uniqueNodes.size(); i++) {
      Assertions.assertEquals(InnerClassNodeHelper.hashCode(uniqueNodes.get(i)), InnerClassNodeHelper.hashCode(uniqueNodes.get(i)));
    }
  }
}
