package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;

import java.util.ListIterator;

public class InsnListListAdapterTest {
  @Test
  void test_get() {
    InsnList insnList = new InsnList();
    InsnListListAdapter adapter = new InsnListListAdapter(insnList);
    insnList.add(new InsnNode(Opcodes.NOP));

    Assertions.assertEquals(Opcodes.NOP, adapter.get(0).getOpcode());
  }

  @Test
  void test_size() {
    InsnList insnList = new InsnList();
    InsnListListAdapter adapter = new InsnListListAdapter(insnList);
    Assertions.assertEquals(0, adapter.size());

    insnList.add(new InsnNode(Opcodes.NOP));
    Assertions.assertEquals(1, adapter.size());
  }

  @Test
  void test_set() {
    InsnList insnList = new InsnList();
    InsnListListAdapter adapter = new InsnListListAdapter(insnList);
    insnList.add(new InsnNode(Opcodes.NOP));

    AbstractInsnNode old = adapter.set(0, new InsnNode(Opcodes.ACONST_NULL));
    Assertions.assertEquals(Opcodes.NOP, old.getOpcode());
    Assertions.assertEquals(Opcodes.ACONST_NULL, adapter.get(0).getOpcode());
  }

  @Test
  void test_add() {
    InsnList insnList = new InsnList();
    InsnListListAdapter adapter = new InsnListListAdapter(insnList);
    adapter.add(0, new InsnNode(Opcodes.NOP));
    Assertions.assertEquals(Opcodes.NOP, adapter.get(0).getOpcode());

    adapter.add(1, new InsnNode(Opcodes.ACONST_NULL));
    Assertions.assertEquals(Opcodes.ACONST_NULL, adapter.get(1).getOpcode());
  }

  @Test
  void test_remove() {
    InsnList insnList = new InsnList();
    InsnListListAdapter adapter = new InsnListListAdapter(insnList);
    insnList.add(new InsnNode(Opcodes.NOP));
    insnList.add(new InsnNode(Opcodes.ACONST_NULL));

    AbstractInsnNode removed = adapter.remove(0);
    Assertions.assertEquals(Opcodes.NOP, removed.getOpcode());
    Assertions.assertEquals(1, adapter.size());
    Assertions.assertEquals(Opcodes.ACONST_NULL, adapter.get(0).getOpcode());
  }

  @Test
  void test_iterator() {
    InsnList insnList = new InsnList();
    InsnListListAdapter adapter = new InsnListListAdapter(insnList);
    insnList.add(new InsnNode(Opcodes.NOP));
    insnList.add(new InsnNode(Opcodes.ACONST_NULL));

    int[] opcodes = {Opcodes.NOP, Opcodes.ACONST_NULL};
    int index = 0;
    for (AbstractInsnNode insn : adapter) {
      Assertions.assertEquals(opcodes[index], insn.getOpcode());
      index++;
    }
  }

  @Test
  void test_listIterator() {
    InsnList insnList = new InsnList();
    InsnListListAdapter adapter = new InsnListListAdapter(insnList);
    insnList.add(new InsnNode(Opcodes.NOP));
    insnList.add(new InsnNode(Opcodes.ACONST_NULL));

    int[] opcodes = {Opcodes.NOP, Opcodes.ACONST_NULL};
    ListIterator<AbstractInsnNode> iterator = adapter.listIterator();

    int index = 0;

    while (iterator.hasNext()) {
      AbstractInsnNode insn = iterator.next();
      Assertions.assertEquals(opcodes[index], insn.getOpcode());
      index++;
    }
  }

  @Test
  void test_listIterator_withIndex() {
    InsnList insnList = new InsnList();
    InsnListListAdapter adapter = new InsnListListAdapter(insnList);
    insnList.add(new InsnNode(Opcodes.NOP));
    insnList.add(new InsnNode(Opcodes.ACONST_NULL));

    int[] opcodes = {Opcodes.NOP, Opcodes.ACONST_NULL};
    ListIterator<AbstractInsnNode> iterator = adapter.listIterator(1);

    int index = 1;

    while (iterator.hasNext()) {
      AbstractInsnNode insn = iterator.next();
      Assertions.assertEquals(opcodes[index], insn.getOpcode());
      index++;
    }
  }
}
