package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, null, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, null, new InsnNode(Opcodes.NOP))
    ));
    Pair<InsnListDiff, InsnListDiff> commuted = InsnListDiffUtils.commute(diff1, diff2);
    Assertions.assertEquals(1, commuted.first.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.MATCH, commuted.first.operations.get(0).type);
    Assertions.assertTrue(InsnListDiffUtils.compareInsnsIgnoreLabels(commuted.first.operations.get(0).operand, new InsnNode(Opcodes.NOP)));
    Assertions.assertEquals(1, commuted.second.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.MATCH, commuted.second.operations.get(0).type);
    Assertions.assertTrue(InsnListDiffUtils.compareInsnsIgnoreLabels(commuted.second.operations.get(0).operand, new InsnNode(Opcodes.NOP)));
  }

  @Test
  void test_commute_1() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP))
    ));
    Pair<InsnListDiff, InsnListDiff> commuted = InsnListDiffUtils.commute(diff1, diff2);
    Assertions.assertEquals(0, commuted.first.operations.size());
    Assertions.assertEquals(1, commuted.second.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.INSERT, commuted.second.operations.get(0).type);
    Assertions.assertTrue(InsnListDiffUtils.compareInsnsIgnoreLabels(commuted.second.operations.get(0).operand, new InsnNode(Opcodes.NOP)));
  }

  @Test
  void test_commute_2() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP))
    ));
    Pair<InsnListDiff, InsnListDiff> commuted = InsnListDiffUtils.commute(diff1, diff2);
    Assertions.assertEquals(1, commuted.first.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.INSERT, commuted.first.operations.get(0).type);
    Assertions.assertTrue(InsnListDiffUtils.compareInsnsIgnoreLabels(commuted.first.operations.get(0).operand, new InsnNode(Opcodes.NOP)));
    Assertions.assertEquals(1, commuted.second.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.MATCH, commuted.second.operations.get(0).type);
    Assertions.assertTrue(InsnListDiffUtils.compareInsnsIgnoreLabels(commuted.second.operations.get(0).operand, new InsnNode(Opcodes.NOP)));
  }

  @Test
  void test_commute_3() {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, null, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
    ));
    Assertions.assertThrows(IllegalDiffException.class, () -> {
      InsnListDiffUtils.commute(diff1, diff2);
    });
  }

  @Test
  void test_commute_4() {
    InsnListDiff diff1 = new InsnListDiff(List.of(
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, null, new InsnNode(Opcodes.NOP))
    ));
    Assertions.assertThrows(IllegalDiffException.class, () -> {
      InsnListDiffUtils.commute(diff1, diff2);
    });
  }

  @Test
  void test_commute_5() {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.DELETE, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP))
    ));
    Assertions.assertThrows(ConflictException.class, () -> {
      InsnListDiffUtils.commute(diff1, diff2);
    });
  }

  @Test
  void test_merge_0() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, null, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, null, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff merged = InsnListDiffUtils.merge(diff1, diff2);
    Assertions.assertEquals(1, merged.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.MATCH, merged.operations.get(0).type);
    Assertions.assertTrue(InsnListDiffUtils.compareInsnsIgnoreLabels(merged.operations.get(0).operand, new InsnNode(Opcodes.NOP)));
  }

  @Test
  void test_merge_1() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, null, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, null, new InsnNode(Opcodes.NOP)),
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff merged = InsnListDiffUtils.merge(diff1, diff2);
    Assertions.assertEquals(2, merged.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.MATCH, merged.operations.get(0).type);
    Assertions.assertTrue(InsnListDiffUtils.compareInsnsIgnoreLabels(merged.operations.get(0).operand, new InsnNode(Opcodes.NOP)));
    Assertions.assertEquals(InsnListDiff.Operation.Type.INSERT, merged.operations.get(1).type);
    Assertions.assertTrue(InsnListDiffUtils.compareInsnsIgnoreLabels(merged.operations.get(1).operand, new InsnNode(Opcodes.NOP)));
  }

  @Test
  void test_merge_2() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, null, new InsnNode(Opcodes.NOP))
    ));
    Assertions.assertThrows(IllegalDiffException.class, () -> {
      InsnListDiffUtils.merge(diff1, diff2);
    });
  }

  @Test
  void test_merge_3() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.DELETE, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.DELETE, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP))
    ));
    Assertions.assertThrows(ConflictException.class, () -> {
      InsnListDiffUtils.merge(diff1, diff2);
    });
  }

  @Test
  void test_merge_4() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP))
    ));
    Assertions.assertThrows(ConflictException.class, () -> {
      InsnListDiffUtils.merge(diff1, diff2);
    });
  }

  // match -> match
  @Test
  void test_compose_0() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, null, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, null, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff composed = InsnListDiffUtils.compose(diff1, diff2);
    Assertions.assertEquals(1, composed.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.MATCH, composed.operations.get(0).type);
    Assertions.assertTrue(InsnListDiffUtils.compareInsnsIgnoreLabels(composed.operations.get(0).operand, new InsnNode(Opcodes.NOP)));
  }

  @Test
  void test_compose_1() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, null, new InsnNode(Opcodes.NOP)),
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, null, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, null, new InsnNode(Opcodes.NOP))
    ));
    Assertions.assertThrows(IllegalDiffException.class, () -> {
      InsnListDiffUtils.compose(diff1, diff2);
    });
  }

  @Test
  void test_compose_2() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, null, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, null, new InsnNode(Opcodes.ACONST_NULL))
    ));
    Assertions.assertThrows(IllegalDiffException.class, () -> {
      InsnListDiffUtils.compose(diff1, diff2);
    });
  }

  @Test
  void test_compose_3() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, null, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, null, new InsnNode(Opcodes.NOP)),
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff composed = InsnListDiffUtils.compose(diff1, diff2);
    Assertions.assertEquals(2, composed.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.MATCH, composed.operations.get(0).type);
    Assertions.assertTrue(InsnListDiffUtils.compareInsnsIgnoreLabels(composed.operations.get(0).operand, new InsnNode(Opcodes.NOP)));
    Assertions.assertEquals(InsnListDiff.Operation.Type.INSERT, composed.operations.get(1).type);
    Assertions.assertTrue(InsnListDiffUtils.compareInsnsIgnoreLabels(composed.operations.get(1).operand, new InsnNode(Opcodes.NOP)));
  }

  // insert -> delete
  @Test
  void test_compose_4() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.DELETE, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff composed = InsnListDiffUtils.compose(diff1, diff2);
    Assertions.assertEquals(0, composed.operations.size());
  }

  // delete -> insert
  @Test
  void test_compose_5() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.DELETE, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff composed = InsnListDiffUtils.compose(diff1, diff2);
    Assertions.assertEquals(1, composed.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.MATCH, composed.operations.get(0).type);
    Assertions.assertTrue(InsnListDiffUtils.compareInsnsIgnoreLabels(composed.operations.get(0).operand, new InsnNode(Opcodes.NOP)));
  }

  // match -> delete
  @Test
  void test_compose_6() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, null, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.DELETE, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff composed = InsnListDiffUtils.compose(diff1, diff2);
    Assertions.assertEquals(1, composed.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.DELETE, composed.operations.get(0).type);
    Assertions.assertTrue(InsnListDiffUtils.compareInsnsIgnoreLabels(composed.operations.get(0).operand, new InsnNode(Opcodes.NOP)));
  }

  // insert -> match
  @Test
  void test_compose_7() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, null, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff composed = InsnListDiffUtils.compose(diff1, diff2);
    Assertions.assertEquals(1, composed.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.INSERT, composed.operations.get(0).type);
    Assertions.assertTrue(InsnListDiffUtils.compareInsnsIgnoreLabels(composed.operations.get(0).operand, new InsnNode(Opcodes.NOP)));
  }

  @Test
  void test_compose_invert_0() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = InsnListDiffUtils.invert(diff1);
    InsnListDiff composed = InsnListDiffUtils.compose(diff1, diff2);
    Assertions.assertEquals(0, composed.operations.size());
  }

  @Test
  void test_compose_invert_1() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.DELETE, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = InsnListDiffUtils.invert(diff1);
    InsnListDiff composed = InsnListDiffUtils.compose(diff1, diff2);
    Assertions.assertEquals(1, composed.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.MATCH, composed.operations.get(0).type);
    Assertions.assertTrue(InsnListDiffUtils.compareInsnsIgnoreLabels(composed.operations.get(0).operand, new InsnNode(Opcodes.NOP)));
  }

  @Test
  void test_diff_0() {
    InsnList list1 = new InsnList();
    list1.add(new InsnNode(Opcodes.NOP));
    InsnList list2 = new InsnList();
    list2.add(new InsnNode(Opcodes.NOP));
    InsnListDiff diff = InsnListDiffUtils.diff(
            new InsnListListAdapter(list1),
            new InsnListListAdapter(list2)
    );
    Assertions.assertEquals(1, diff.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.MATCH, diff.operations.get(0).type);
    Assertions.assertTrue(InsnListDiffUtils.compareInsnsIgnoreLabels(diff.operations.get(0).operand, new InsnNode(Opcodes.NOP)));
  }

  @Test
  void test_diff_1() {
    InsnList list1 = new InsnList();
    list1.add(new InsnNode(Opcodes.NOP));
    InsnList list2 = new InsnList();
    list2.add(new InsnNode(Opcodes.ACONST_NULL));
    InsnListDiff diff = InsnListDiffUtils.diff(
            new InsnListListAdapter(list1),
            new InsnListListAdapter(list2)
    );
    Assertions.assertEquals(2, diff.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.DELETE, diff.operations.get(0).type);
    Assertions.assertTrue(InsnListDiffUtils.compareInsnsIgnoreLabels(diff.operations.get(0).operand, new InsnNode(Opcodes.NOP)));
    Assertions.assertEquals(InsnListDiff.Operation.Type.INSERT, diff.operations.get(1).type);
    Assertions.assertTrue(InsnListDiffUtils.compareInsnsIgnoreLabels(diff.operations.get(1).operand, new InsnNode(Opcodes.ACONST_NULL)));
  }

  @Test
  void test_diff_2() {
    InsnList list1 = new InsnList();
    list1.add(new InsnNode(Opcodes.NOP));
    list1.add(new InsnNode(Opcodes.NOP));
    InsnList list2 = new InsnList();
    list2.add(new InsnNode(Opcodes.NOP));
    InsnListDiff diff = InsnListDiffUtils.diff(
            new InsnListListAdapter(list1),
            new InsnListListAdapter(list2)
    );
    Assertions.assertEquals(2, diff.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.DELETE, diff.operations.get(0).type);
    Assertions.assertTrue(InsnListDiffUtils.compareInsnsIgnoreLabels(diff.operations.get(0).operand, new InsnNode(Opcodes.NOP)));
    Assertions.assertEquals(InsnListDiff.Operation.Type.MATCH, diff.operations.get(1).type);
    Assertions.assertTrue(InsnListDiffUtils.compareInsnsIgnoreLabels(diff.operations.get(1).operand, new InsnNode(Opcodes.NOP)));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 10, 100, 1000, 10000, 100000})
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  void test_diff_3(int numInsns) {
    InsnList list1 = new InsnList();
    for (int i = 0; i < numInsns; i++) {
      list1.add(new InsnNode(Opcodes.NOP));
    }
    InsnList list2 = new InsnList();
//    list2.add(new InsnNode(Opcodes.NOP));
    for (int i = 0; i < numInsns; i++) {
      list2.add(new InsnNode(Opcodes.NOP));
    }
    InsnListDiff diff = InsnListDiffUtils.diff(
            new InsnListListAdapter(list1),
            new InsnListListAdapter(list2)
    );
    Assertions.assertEquals(numInsns, diff.operations.size());
    for (int i = 0; i < numInsns; i++) {
      Assertions.assertEquals(InsnListDiff.Operation.Type.MATCH, diff.operations.get(i).type);
      Assertions.assertTrue(InsnListDiffUtils.compareInsnsIgnoreLabels(diff.operations.get(i).operand, new InsnNode(Opcodes.NOP)));
    }
  }
}
