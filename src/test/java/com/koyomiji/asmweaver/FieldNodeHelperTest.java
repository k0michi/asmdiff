package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldNode;

import java.util.List;

public class FieldNodeHelperTest {
  static List<FieldNode> generateUnique() {
    return List.of(
            new FieldNode(0, "field1", "I", null, null),
            new FieldNode(Opcodes.ACC_PUBLIC, "field1", "I", null, null),
            new FieldNode(0, "field2", "I", null, null),
            new FieldNode(0, "field1", "J", null, null),
            new FieldNode(0, "field1", "I", "Ljava/lang/Object;", null),
            new FieldNode(0, "field1", "I", null, 123)
    );
  }

  @Test
  void test_equals_0() {
    var uniqueNodes1 = generateUnique();
    var uniqueNodes2 = generateUnique();

    for (int i = 0; i < uniqueNodes1.size(); i++) {
      Assertions.assertTrue(FieldNodeHelper.equals(uniqueNodes1.get(i), uniqueNodes2.get(i)));
    }
  }

  @Test
  void test_equals_1() {
    var uniqueNodes = generateUnique();

    for (int i = 0; i < uniqueNodes.size(); i++) {
      for (int j = 0; j < uniqueNodes.size(); j++) {
        if (i != j) {
          Assertions.assertFalse(FieldNodeHelper.equals(uniqueNodes.get(i), uniqueNodes.get(j)));
        }
      }
    }
  }

  @Test
  void test_hashCode_0() {
    var uniqueNodes = generateUnique();

    for (int i = 0; i < uniqueNodes.size(); i++) {
      Assertions.assertEquals(FieldNodeHelper.hashCode(uniqueNodes.get(i)), FieldNodeHelper.hashCode(uniqueNodes.get(i)));
    }
  }
}
