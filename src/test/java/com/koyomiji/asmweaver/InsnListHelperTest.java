package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LineNumberNode;

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

  @Test
  void test_splitLineNumbers_0() {
    List<AbstractInsnNode> list = new ArrayList<>();
    list.add(LabelNodes.l0);
    list.add(new LineNumberNode(10, LabelNodes.l0));
    list.add(new InsnNode(Opcodes.NOP));
    list.add(LabelNodes.l1);
    list.add(new LineNumberNode(10, LabelNodes.l1));
    list.add(new InsnNode(Opcodes.NOP));

    Pair<List<AbstractInsnNode>, List<LineNumberNode>> split = InsnListHelper.splitLineNumbers(list);
    Assertions.assertEquals(4, split.first.size());
    Assertions.assertTrue(AbstractInsnNodeHelper.equals(split.first.get(0), list.get(0)));
    Assertions.assertTrue(AbstractInsnNodeHelper.equals(split.first.get(1), list.get(2)));
    Assertions.assertTrue(AbstractInsnNodeHelper.equals(split.first.get(2), list.get(3)));
    Assertions.assertTrue(AbstractInsnNodeHelper.equals(split.first.get(3), list.get(5)));
    Assertions.assertEquals(2, split.second.size());
    Assertions.assertTrue(AbstractInsnNodeHelper.equals(split.second.get(0), list.get(1)));
    Assertions.assertTrue(AbstractInsnNodeHelper.equals(split.second.get(1), list.get(4)));
  }

  @Test
  void test_splitLineNumbers_roundTrip() {
    List<AbstractInsnNode> list = new ArrayList<>();
    list.add(LabelNodes.l0);
    list.add(new LineNumberNode(10, LabelNodes.l0));
    list.add(new InsnNode(Opcodes.NOP));
    list.add(LabelNodes.l1);
    list.add(new LineNumberNode(10, LabelNodes.l1));
    list.add(new InsnNode(Opcodes.NOP));

    Pair<List<AbstractInsnNode>, List<LineNumberNode>> split = InsnListHelper.splitLineNumbers(list);
    List<AbstractInsnNode> merged = InsnListHelper.mergeLineNumbers(split.first, split.second);

    Assertions.assertTrue(ListHelper.equals(list, merged, AbstractInsnNodeHelper::equals));
  }
}
