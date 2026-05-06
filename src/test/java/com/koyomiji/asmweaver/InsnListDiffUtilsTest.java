package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;

import java.util.List;

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

  @Test
  void test_commute_0() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, new InsnNode(Opcodes.NOP))
    ));
    Pair<InsnListDiff, InsnListDiff> commuted = InsnListDiffUtils.commute(diff1, diff2);
    Assertions.assertEquals(1, commuted.first.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.MATCH, commuted.first.operations.get(0).type);
    Assertions.assertTrue(InsnListDiffUtils.compareInsns(commuted.first.operations.get(0).operand, new InsnNode(Opcodes.NOP)));
    Assertions.assertEquals(1, commuted.second.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.MATCH, commuted.second.operations.get(0).type);
    Assertions.assertTrue(InsnListDiffUtils.compareInsns(commuted.second.operations.get(0).operand, new InsnNode(Opcodes.NOP)));
  }

  @Test
  void test_commute_1() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT_EXACT, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, new InsnNode(Opcodes.NOP))
    ));
    Pair<InsnListDiff, InsnListDiff> commuted = InsnListDiffUtils.commute(diff1, diff2);
    Assertions.assertEquals(0, commuted.first.operations.size());
    Assertions.assertEquals(1, commuted.second.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.INSERT_EXACT, commuted.second.operations.get(0).type);
    Assertions.assertTrue(InsnListDiffUtils.compareInsns(commuted.second.operations.get(0).operand, new InsnNode(Opcodes.NOP)));
  }

  @Test
  void test_commute_2() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT_EXACT, new InsnNode(Opcodes.NOP))
    ));
    Pair<InsnListDiff, InsnListDiff> commuted = InsnListDiffUtils.commute(diff1, diff2);
    Assertions.assertEquals(1, commuted.first.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.INSERT_EXACT, commuted.first.operations.get(0).type);
    Assertions.assertTrue(InsnListDiffUtils.compareInsns(commuted.first.operations.get(0).operand, new InsnNode(Opcodes.NOP)));
    Assertions.assertEquals(1, commuted.second.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.MATCH, commuted.second.operations.get(0).type);
    Assertions.assertTrue(InsnListDiffUtils.compareInsns(commuted.second.operations.get(0).operand, new InsnNode(Opcodes.NOP)));
  }

  @Test
  void test_commute_3() {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
    ));
    Assertions.assertThrows(RuntimeException.class, () -> {
      InsnListDiffUtils.commute(diff1, diff2);
    });
  }

  @Test
  void test_commute_4() {
    InsnListDiff diff1 = new InsnListDiff(List.of(
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, new InsnNode(Opcodes.NOP))
    ));
    Assertions.assertThrows(RuntimeException.class, () -> {
      InsnListDiffUtils.commute(diff1, diff2);
    });
  }

  @Test
  void test_commute_5() {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT_EXACT, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.DELETE, new InsnNode(Opcodes.NOP))
    ));
    Assertions.assertThrows(ConflictException.class, () -> {
      InsnListDiffUtils.commute(diff1, diff2);
    });
  }
}
