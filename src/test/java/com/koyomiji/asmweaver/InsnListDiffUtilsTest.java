package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.BinaryReader;
import com.koyomiji.asmweaver.io.BinaryWriter;
import com.koyomiji.asmweaver.util.AutoIncrementBiHashMap;
import com.koyomiji.asmweaver.util.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class InsnListDiffUtilsTest {
//  @Test
//  void test_compareInsnLists_same() {
//    InsnList list1 = new InsnList();
//    list1.add(new InsnNode(Opcodes.NOP));
//    InsnList list2 = new InsnList();
//    list2.add(new InsnNode(Opcodes.NOP));
//    Assertions.assertTrue(InsnListDiffUtils.compareInsnLists(
//            new InsnListListAdapter(list1),
//            new InsnListListAdapter(list2)
//    ));
//  }
//
//  @Test
//  void test_compareInsnLists_different() {
//    InsnList list1 = new InsnList();
//    list1.add(new InsnNode(Opcodes.NOP));
//    InsnList list2 = new InsnList();
//    list2.add(new InsnNode(Opcodes.ACONST_NULL));
//    Assertions.assertFalse(InsnListDiffUtils.compareInsnLists(
//            new InsnListListAdapter(list1),
//            new InsnListListAdapter(list2)
//    ));
//  }
//
//  @Test
//  void test_compareInsnLists_differentSize() {
//    InsnList list1 = new InsnList();
//    list1.add(new InsnNode(Opcodes.NOP));
//    InsnList list2 = new InsnList();
//    list2.add(new InsnNode(Opcodes.NOP));
//    list2.add(new InsnNode(Opcodes.NOP));
//    Assertions.assertFalse(InsnListDiffUtils.compareInsnLists(
//            new InsnListListAdapter(list1),
//            new InsnListListAdapter(list2)
//    ));
//  }

  @Test
  void test_commute_0() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP), new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP), new InsnNode(Opcodes.NOP))
    ));
    Pair<InsnListDiff, InsnListDiff> commuted = InsnListDiffUtils.commute(diff1, diff2);
    Assertions.assertEquals(1, commuted.first.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.MATCH, commuted.first.operations.get(0).type);
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(commuted.first.operations.get(0).operand1, new InsnNode(Opcodes.NOP)));
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(commuted.first.operations.get(0).operand2, new InsnNode(Opcodes.NOP)));

    Assertions.assertEquals(1, commuted.second.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.MATCH, commuted.second.operations.get(0).type);
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(commuted.second.operations.get(0).operand1, new InsnNode(Opcodes.NOP)));
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(commuted.second.operations.get(0).operand2, new InsnNode(Opcodes.NOP)));
  }

  @Test
  void test_commute_1() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT, InsnListDiff.Operation.Mode.BETWEEN, null, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP), new InsnNode(Opcodes.NOP))
    ));
    Pair<InsnListDiff, InsnListDiff> commuted = InsnListDiffUtils.commute(diff1, diff2);
    Assertions.assertEquals(0, commuted.first.operations.size());
    Assertions.assertEquals(1, commuted.second.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.INSERT, commuted.second.operations.get(0).type);
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(commuted.second.operations.get(0).operand1, null));
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(commuted.second.operations.get(0).operand2, new InsnNode(Opcodes.NOP)));
  }

  @Test
  void test_commute_2() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT, InsnListDiff.Operation.Mode.BETWEEN, null, new InsnNode(Opcodes.NOP))
    ));
    Pair<InsnListDiff, InsnListDiff> commuted = InsnListDiffUtils.commute(diff1, diff2);
    Assertions.assertEquals(1, commuted.first.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.INSERT, commuted.first.operations.get(0).type);
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(commuted.first.operations.get(0).operand1, null));
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(commuted.first.operations.get(0).operand2, new InsnNode(Opcodes.NOP)));
    Assertions.assertEquals(1, commuted.second.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.MATCH, commuted.second.operations.get(0).type);
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(commuted.second.operations.get(0).operand1, new InsnNode(Opcodes.NOP)));
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(commuted.second.operations.get(0).operand2, new InsnNode(Opcodes.NOP)));
  }

  @Test
  void test_commute_3() {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP), new InsnNode(Opcodes.NOP))
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
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP), new InsnNode(Opcodes.NOP))
    ));
    Assertions.assertThrows(IllegalDiffException.class, () -> {
      InsnListDiffUtils.commute(diff1, diff2);
    });
  }

  @Test
  void test_commute_5() {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT, InsnListDiff.Operation.Mode.BETWEEN, null, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.DELETE, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP), null)
    ));
    Assertions.assertThrows(ConflictException.class, () -> {
      InsnListDiffUtils.commute(diff1, diff2);
    });
  }

  // match -> match
  @Test
  void test_compose_0() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP), new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP), new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff composed = InsnListDiffUtils.compose(diff1, diff2);
    Assertions.assertEquals(1, composed.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.MATCH, composed.operations.get(0).type);
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(composed.operations.get(0).operand1, new InsnNode(Opcodes.NOP)));
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(composed.operations.get(0).operand2, new InsnNode(Opcodes.NOP)));
  }

  @Test
  void test_compose_1() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP), new InsnNode(Opcodes.NOP)),
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP), new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP), new InsnNode(Opcodes.NOP))
    ));
    Assertions.assertThrows(IllegalDiffException.class, () -> {
      InsnListDiffUtils.compose(diff1, diff2);
    });
  }

  @Test
  void test_compose_2() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP), new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.ACONST_NULL), new InsnNode(Opcodes.ACONST_NULL))
    ));
    Assertions.assertThrows(IllegalDiffException.class, () -> {
      InsnListDiffUtils.compose(diff1, diff2);
    });
  }

  @Test
  void test_compose_3() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP), new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP), new InsnNode(Opcodes.NOP)),
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT, InsnListDiff.Operation.Mode.BETWEEN, null, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff composed = InsnListDiffUtils.compose(diff1, diff2);
    Assertions.assertEquals(2, composed.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.MATCH, composed.operations.get(0).type);
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(composed.operations.get(0).operand1, new InsnNode(Opcodes.NOP)));
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(composed.operations.get(0).operand2, new InsnNode(Opcodes.NOP)));
    Assertions.assertEquals(InsnListDiff.Operation.Type.INSERT, composed.operations.get(1).type);
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(composed.operations.get(1).operand1, null));
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(composed.operations.get(1).operand2, new InsnNode(Opcodes.NOP)));
  }

  // insert -> delete
  @Test
  void test_compose_4() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT, InsnListDiff.Operation.Mode.BETWEEN, null, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.DELETE, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP), null)
    ));
    InsnListDiff composed = InsnListDiffUtils.compose(diff1, diff2);
    Assertions.assertEquals(0, composed.operations.size());
  }

  // delete -> insert
//  @Test
//  void test_compose_5() throws ConflictException {
//    InsnListDiff diff1 = new InsnListDiff(List.of(
//            new InsnListDiff.Operation(InsnListDiff.Operation.Type.DELETE, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP), null)
//    ));
//    InsnListDiff diff2 = new InsnListDiff(List.of(
//            new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT, InsnListDiff.Operation.Mode.BETWEEN, null, new InsnNode(Opcodes.NOP))
//    ));
//    InsnListDiff composed = InsnListDiffUtils.compose(diff1, diff2);
//    Assertions.assertEquals(1, composed.operations.size());
//    Assertions.assertEquals(InsnListDiff.Operation.Type.MATCH, composed.operations.get(0).type);
//    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(composed.operations.get(0).operand1, new InsnNode(Opcodes.NOP)));
//    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(composed.operations.get(0).operand2, new InsnNode(Opcodes.NOP)));
//  }

  // match -> delete
  @Test
  void test_compose_6() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP), new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.DELETE, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP), null)
    ));
    InsnListDiff composed = InsnListDiffUtils.compose(diff1, diff2);
    Assertions.assertEquals(1, composed.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.DELETE, composed.operations.get(0).type);
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(composed.operations.get(0).operand1, new InsnNode(Opcodes.NOP)));
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(composed.operations.get(0).operand2, null));
  }

  // insert -> match
  @Test
  void test_compose_7() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT, InsnListDiff.Operation.Mode.BETWEEN, null, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP), new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff composed = InsnListDiffUtils.compose(diff1, diff2);
    Assertions.assertEquals(1, composed.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.INSERT, composed.operations.get(0).type);
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(composed.operations.get(0).operand1, null));
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(composed.operations.get(0).operand2, new InsnNode(Opcodes.NOP)));
  }

  @Test
  void test_compose_invert_0() throws ConflictException {
    InsnListDiff diff1 = new InsnListDiff(List.of(
            new InsnListDiff.Operation(InsnListDiff.Operation.Type.INSERT, InsnListDiff.Operation.Mode.BETWEEN, null, new InsnNode(Opcodes.NOP))
    ));
    InsnListDiff diff2 = InsnListDiffUtils.invert(diff1);
    InsnListDiff composed = InsnListDiffUtils.compose(diff1, diff2);
    Assertions.assertEquals(0, composed.operations.size());
  }
//
//  @Test
//  void test_compose_invert_1() throws ConflictException {
//    InsnListDiff diff1 = new InsnListDiff(List.of(
//            new InsnListDiff.Operation(InsnListDiff.Operation.Type.DELETE, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP))
//    ));
//    InsnListDiff diff2 = InsnListDiffUtils.invert(diff1);
//    InsnListDiff composed = InsnListDiffUtils.compose(diff1, diff2);
//    Assertions.assertEquals(1, composed.operations.size());
//    Assertions.assertEquals(InsnListDiff.Operation.Type.MATCH, composed.operations.get(0).type);
//    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(composed.operations.get(0).operand, new InsnNode(Opcodes.NOP)));
//  }

  @Test
  void test_compose_mismatch_0() {
    InsnListDiff diff1 = new InsnListDiff(
            List.of(
            )
    );

    InsnListDiff diff2 = new InsnListDiff(
            List.of(
                    new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP), new InsnNode(Opcodes.NOP))
            )
    );

    Assertions.assertThrows(IllegalDiffException.class, () -> {
      InsnListDiffUtils.compose(diff1, diff2);
    });
  }

  @Test
  void test_compose_mismatch_1() {
    InsnListDiff diff1 = new InsnListDiff(
            List.of(
                    new InsnListDiff.Operation(InsnListDiff.Operation.Type.MATCH, InsnListDiff.Operation.Mode.BETWEEN, new InsnNode(Opcodes.NOP), new InsnNode(Opcodes.NOP))
            )
    );

    InsnListDiff diff2 = new InsnListDiff(
            List.of(
            )
    );

    Assertions.assertThrows(IllegalDiffException.class, () -> {
      InsnListDiffUtils.compose(diff1, diff2);
    });
  }

  @Test
  void test_diff_0() {
    InsnList list1 = new InsnList();
    list1.add(new InsnNode(Opcodes.NOP));
    InsnList list2 = new InsnList();
    list2.add(new InsnNode(Opcodes.NOP));
    InsnListDiff diff = InsnListDiffUtils.diff(
            new InsnListListAdapter(list1),
            (insn) -> -1,
            new InsnListListAdapter(list2),
            (insn) -> -1
    );
    Assertions.assertEquals(1, diff.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.MATCH, diff.operations.get(0).type);
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(diff.operations.get(0).operand1, new InsnNode(Opcodes.NOP)));
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(diff.operations.get(0).operand2, new InsnNode(Opcodes.NOP)));
  }

  @Test
  void test_diff_1() {
    InsnList list1 = new InsnList();
    list1.add(new InsnNode(Opcodes.NOP));
    InsnList list2 = new InsnList();
    list2.add(new InsnNode(Opcodes.ACONST_NULL));
    InsnListDiff diff = InsnListDiffUtils.diff(
            new InsnListListAdapter(list1),
            (insn) -> -1,
            new InsnListListAdapter(list2),
            (insn) -> -1
    );
    Assertions.assertEquals(2, diff.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.DELETE, diff.operations.get(0).type);
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(diff.operations.get(0).operand1, new InsnNode(Opcodes.NOP)));
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(diff.operations.get(0).operand2, null));
    Assertions.assertEquals(InsnListDiff.Operation.Type.INSERT, diff.operations.get(1).type);
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(diff.operations.get(1).operand1, null));
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(diff.operations.get(1).operand2, new InsnNode(Opcodes.ACONST_NULL)));
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
            (insn) -> -1,
            new InsnListListAdapter(list2),
            (insn) -> -1
    );
    Assertions.assertEquals(2, diff.operations.size());
    Assertions.assertEquals(InsnListDiff.Operation.Type.DELETE, diff.operations.get(0).type);
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(diff.operations.get(0).operand1, new InsnNode(Opcodes.NOP)));
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(diff.operations.get(0).operand2, null));
    Assertions.assertEquals(InsnListDiff.Operation.Type.MATCH, diff.operations.get(1).type);
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(diff.operations.get(1).operand1, new InsnNode(Opcodes.NOP)));
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(diff.operations.get(1).operand2, new InsnNode(Opcodes.NOP)));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 5, 10, 50, 100, 500, 1000, 5000, 10000, 50000, 100000, 500000})
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  void test_diff_3(int numInsns) {
    InsnList list1 = new InsnList();
    for (int i = 0; i < numInsns; i++) {
      list1.add(new InsnNode(Opcodes.NOP));
    }
    InsnList list2 = new InsnList();
    for (int i = 0; i < numInsns; i++) {
      list2.add(new InsnNode(Opcodes.NOP));
    }
    InsnListDiff diff = InsnListDiffUtils.diff(
            new InsnListListAdapter(list1),
            (insn) -> -1,
            new InsnListListAdapter(list2),
            (insn) -> -1
    );
    Assertions.assertEquals(numInsns, diff.operations.size());
    for (int i = 0; i < numInsns; i++) {
      Assertions.assertEquals(InsnListDiff.Operation.Type.MATCH, diff.operations.get(i).type);
      Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(diff.operations.get(i).operand1, new InsnNode(Opcodes.NOP)));
      Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(diff.operations.get(i).operand2, new InsnNode(Opcodes.NOP)));
    }
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 5, 10, 50, 100, 500, 1000})
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  void test_diff_4(int numInsns) {
    InsnList list1 = new InsnList();
    for (int i = 0; i < numInsns; i++) {
      list1.add(new InsnNode(Opcodes.NOP));
    }
    InsnList list2 = new InsnList();
    InsnListDiff diff = InsnListDiffUtils.diff(
            new InsnListListAdapter(list1),
            (insn) -> -1,
            new InsnListListAdapter(list2),
            (insn) -> -1
    );
    Assertions.assertEquals(numInsns, diff.operations.size());
    for (int i = 0; i < numInsns; i++) {
      Assertions.assertEquals(InsnListDiff.Operation.Type.DELETE, diff.operations.get(i).type);
      Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(diff.operations.get(i).operand1, new InsnNode(Opcodes.NOP)));
      Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(diff.operations.get(i).operand2, null));
    }
  }

  @Test
  void test_compareInsnsIgnoreLabelsExactLocals_0() {
    Assertions.assertTrue(AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(
            new LabelNode(),
            new LabelNode()
    ));
  }

  @Test
  void test_distance_0() {
    InsnList list1 = new InsnList();
    list1.add(new InsnNode(Opcodes.NOP));
    InsnList list2 = new InsnList();
    list2.add(new InsnNode(Opcodes.NOP));
    InsnListDiff diff = InsnListDiffUtils.diff(
            new InsnListListAdapter(list1),
            (insn) -> -1,
            new InsnListListAdapter(list2),
            (insn) -> -1
    );
    Assertions.assertEquals(0, diff.distance());
  }

  private int distance(InsnListDiff diff) {
    int distance = 0;
    for (var op : diff.operations) {
      if (op.type == InsnListDiff.Operation.Type.INSERT || op.type == InsnListDiff.Operation.Type.DELETE) {
        distance++;
      }
    }
    return distance;
  }

//  @Test
//  void test_diff_local_0() throws AnalyzerException {
//    DefUseChainAnalyzer analyzer = new DefUseChainAnalyzer();
//    MethodNode methodNode = new MethodNode(Opcodes.ACC_PUBLIC, "method", "()V", null, null);
//    methodNode.maxLocals = 100;
//    methodNode.maxStack = 1;
////    methodNode.instructions.add(new IntInsnNode(Opcodes.BIPUSH, 42));
////    methodNode.instructions.add(new VarInsnNode(Opcodes.ISTORE, 0));
////    methodNode.instructions.add(new VarInsnNode(Opcodes.ILOAD, 0));
////    methodNode.instructions.add(new InsnNode(Opcodes.POP));
//    for (int i = 0; i < 100; i++) {
//      for (int j = 0; j < 10; j++) {
//        methodNode.instructions.add(new IntInsnNode(Opcodes.BIPUSH, 42));
//        methodNode.instructions.add(new VarInsnNode(Opcodes.ISTORE, i));
//        methodNode.instructions.add(new VarInsnNode(Opcodes.ILOAD, i));
//        methodNode.instructions.add(new InsnNode(Opcodes.POP));
//      }
//    }
//    methodNode.instructions.add(new InsnNode(Opcodes.RETURN));
//    var result = analyzer.analyze("Test", methodNode);
//
////    HashMap<AbstractInsnNode, Integer> map1 = new HashMap<>();
////
////    for (int i = 0; i < result.getGroups().size(); i++) {
////      for (var defUse : result.getGroups().get(i)) {
////        map1.put(methodNode.instructions.get(defUse.insnIndex), i);
////      }
////    }
//
//    DefUseChainAnalyzer analyzer2 = new DefUseChainAnalyzer();
//    MethodNode methodNode2 = new MethodNode(Opcodes.ACC_PUBLIC, "method", "()V", null, null);
//    methodNode2.maxLocals = 100;
//    methodNode2.maxStack = 1;
//    for (int i = 0; i < 100; i++) {
//      for (int j = 0; j < 10; j++) {
//        methodNode2.instructions.add(new IntInsnNode(Opcodes.BIPUSH, 42));
//        methodNode2.instructions.add(new VarInsnNode(Opcodes.ISTORE, i));
//        methodNode2.instructions.add(new VarInsnNode(Opcodes.ILOAD, i));
//        methodNode2.instructions.add(new InsnNode(Opcodes.POP));
//      }
//    }
//    methodNode2.instructions.add(new InsnNode(Opcodes.RETURN));
//    var result2 = analyzer2.analyze("Test", methodNode2);
//
////    HashMap<AbstractInsnNode, Integer> map2 = new HashMap<>();
////
////    for (int i = 0; i < result2.getGroups().size(); i++) {
////      for (var defUse : result2.getGroups().get(i)) {
////        map2.put(methodNode2.instructions.get(defUse.insnIndex), i);
////      }
////    }
//
//    var diff = InsnListDiffUtils.diff(
//            new InsnListListAdapter(methodNode.instructions),
////            map1::get,
//            (insn) -> {
//              // FIXME
//              VarInsnNode varInsn = (VarInsnNode) insn;
//              return result.find(new DefUse(methodNode.instructions.indexOf(insn), insn, varInsn.var)).hashCode();
//            },
//            new InsnListListAdapter(methodNode2.instructions),

  /// /            map2::get
//            (insn) -> {
//              VarInsnNode varInsn = (VarInsnNode) insn;
//              return result2.find(new DefUse(methodNode2.instructions.indexOf(insn), insn, varInsn.var)).hashCode();
//            }
//    );
//
//    Assertions.assertEquals(0, distance(diff));
//  }
  @Test
  void test_patch_0() {
    List<AbstractInsnNode> list1 = new ArrayList<>();
    list1.add(LabelNodes.l0);
    List<AbstractInsnNode> list2 = new ArrayList<>();
    list2.add(LabelNodes.l1);
    list2.add(new JumpInsnNode(Opcodes.GOTO, LabelNodes.l1));
    InsnListDiff diff = InsnListDiffUtils.diff(
            list1,
            (insn) -> -1,
            list2,
            (insn) -> -1
    );

    List<AbstractInsnNode> list3 = new ArrayList<>();
    list3.add(LabelNodes.l2);
    HashMap<LabelNode, LabelNode> labelMap = new HashMap<>();
    List<AbstractInsnNode> patched = InsnListDiffUtils.patch(
            list3,
            diff,
            labelMap
    );

    Assertions.assertEquals(LabelNodes.l2, ((JumpInsnNode) patched.get(1)).label);
    Assertions.assertEquals(LabelNodes.l2, patched.get(0));
  }

  @Test
  void test_extractLabels_0() {
    List<AbstractInsnNode> list1 = new ArrayList<>();
    list1.add(LabelNodes.l0);
    List<AbstractInsnNode> list2 = new ArrayList<>();
    list2.add(LabelNodes.l1);
    InsnListDiff diff = InsnListDiffUtils.diff(
            list1,
            (insn) -> -1,
            list2,
            (insn) -> -1
    );

    Map<LabelNode, LabelNode> labelMap = InsnListDiffUtils.extractLabelMap(list1, list2, diff);
    Assertions.assertEquals(LabelNodes.l1, labelMap.get(LabelNodes.l0));
  }

  @Test
  void test_readWrite_roundTrip_0() throws IOException {
    List<AbstractInsnNode> list1 = new ArrayList<>();
    list1.add(new InsnNode(Opcodes.NOP));
    List<AbstractInsnNode> list2 = new ArrayList<>();
    list2.add(new InsnNode(Opcodes.NOP));

    AutoIncrementBiHashMap<LabelNode> labelMap = new AutoIncrementBiHashMap<>();
    InsnListDiff diff = InsnListDiffUtils.diff(
            list1,
            (insn) -> -1,
            list2,
            (insn) -> -1
    );

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    InsnListDiffUtils.write(diff, new BinaryWriter(baos), labelMap::get);

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    InsnListDiff read = InsnListDiffUtils.read(new BinaryReader(bais), labelMap::getKey);

    List<AbstractInsnNode> patched = InsnListDiffUtils.patch(
            list1,
            read,
            new HashMap<>()
    );
    Assertions.assertTrue(InsnListHelper.equalsNormalizeLabels(list1, patched));
  }
}
