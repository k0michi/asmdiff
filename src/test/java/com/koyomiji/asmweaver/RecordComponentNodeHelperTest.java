package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.RecordComponentNode;

import java.util.List;

class RecordComponentNodeHelperTest {
  static List<RecordComponentNode> generateUnique() {
    return List.of(
            new RecordComponentNode("component1", "I", null),
            new RecordComponentNode("component2", "I", null),
            new RecordComponentNode("component1", "J", null),
            new RecordComponentNode("component1", "I", "Ljava/lang/Object;")
    );
  }

  @Test
  void test_equals_0() {
    var uniqueNodes1 = generateUnique();
    var uniqueNodes2 = generateUnique();

    for (int i = 0; i < uniqueNodes1.size(); i++) {
      Assertions.assertTrue(RecordComponentNodeHelper.equals(uniqueNodes1.get(i), uniqueNodes2.get(i)));
    }
  }

  @Test
  void test_equals_1() {
    var uniqueNodes = generateUnique();

    for (int i = 0; i < uniqueNodes.size(); i++) {
      for (int j = 0; j < uniqueNodes.size(); j++) {
        if (i != j) {
          Assertions.assertFalse(RecordComponentNodeHelper.equals(uniqueNodes.get(i), uniqueNodes.get(j)));
        }
      }
    }
  }

  @Test
  void test_hashCode_0() {
    var uniqueNodes = generateUnique();

    for (int i = 0; i < uniqueNodes.size(); i++) {
      Assertions.assertEquals(RecordComponentNodeHelper.hashCode(uniqueNodes.get(i)), RecordComponentNodeHelper.hashCode(uniqueNodes.get(i)));
    }
  }
}
