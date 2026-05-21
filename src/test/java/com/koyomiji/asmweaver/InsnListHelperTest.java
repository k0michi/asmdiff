package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;

import java.util.ArrayList;
import java.util.List;

class InsnListHelperTest {
  List<List<AbstractInsnNode>> generateUnique() {
    List<AbstractInsnNode> list1 = new ArrayList<>();

    List<AbstractInsnNode> list2 = new ArrayList<>();
    list2.add(new InsnNode(Opcodes.NOP));

    return List.of(list1, list2);
  }

  @Test
  void test_equals() {
    var uniqueList1 = generateUnique();
    var uniqueList2 = generateUnique();

    for (int i = 0; i < uniqueList1.size(); i++) {
      for (int j = 0; j < uniqueList2.size(); j++) {
        if (i == j) {
          Assertions.assertTrue(InsnListHelper.equalsNormalizeLabels(uniqueList1.get(i), uniqueList2.get(j)));
        } else {
          Assertions.assertFalse(InsnListHelper.equalsNormalizeLabels(uniqueList1.get(i), uniqueList2.get(j)));
        }
      }
    }
  }

  @Test
  void test_hashCode() {
    var uniqueList = generateUnique();

    for (int i = 0; i < uniqueList.size(); i++) {
      Assertions.assertEquals(InsnListHelper.hashCodeNormalizeLabels(uniqueList.get(i)), InsnListHelper.hashCodeNormalizeLabels(uniqueList.get(i)));
    }
  }
}
