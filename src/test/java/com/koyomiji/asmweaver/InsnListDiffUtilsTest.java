package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;

class InsnListDiffUtilsTest {
  @Test
  void test_compareInsnLists_same() {
    InsnList list1 = new InsnList();
    list1.add(new InsnNode(Opcodes.NOP));
    InsnList list2 = new InsnList();
    list2.add(new InsnNode(Opcodes.NOP));
    Assertions.assertTrue(InsnListDiffUtils.compareInsnLists(
            new InsnListListAdapter(list1),
            new InsnListListAdapter(list2)
    ));
  }

  @Test
  void test_compareInsnLists_different() {
    InsnList list1 = new InsnList();
    list1.add(new InsnNode(Opcodes.NOP));
    InsnList list2 = new InsnList();
    list2.add(new InsnNode(Opcodes.ACONST_NULL));
    Assertions.assertFalse(InsnListDiffUtils.compareInsnLists(
            new InsnListListAdapter(list1),
            new InsnListListAdapter(list2)
    ));
  }

  @Test
  void test_compareInsnLists_differentSize() {
    InsnList list1 = new InsnList();
    list1.add(new InsnNode(Opcodes.NOP));
    InsnList list2 = new InsnList();
    list2.add(new InsnNode(Opcodes.NOP));
    list2.add(new InsnNode(Opcodes.NOP));
    Assertions.assertFalse(InsnListDiffUtils.compareInsnLists(
            new InsnListListAdapter(list1),
            new InsnListListAdapter(list2)
    ));
  }
}
