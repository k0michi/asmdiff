package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ListDiffUtilsTest {
  @Test
  void test_diff_0() {
    var oldList = List.of(1, 2, 3);
    var newList = List.of(1, 2, 3);
    var diff = ListDiffUtils.diff(oldList, newList, Integer::equals);
    Assertions.assertEquals(3, diff.operations.size());
    Assertions.assertEquals(ListDiff.Operation.Type.MATCH, diff.operations.get(0).type);
    Assertions.assertEquals(ListDiff.Operation.Type.MATCH, diff.operations.get(1).type);
    Assertions.assertEquals(ListDiff.Operation.Type.MATCH, diff.operations.get(2).type);
  }

  @Test
  void test_diff_1() {
    var oldList = List.of(1, 2, 3);
    var newList = List.of(1, 4, 3);
    var diff = ListDiffUtils.diff(oldList, newList, Integer::equals);
    Assertions.assertEquals(4, diff.operations.size());
    Assertions.assertEquals(ListDiff.Operation.Type.MATCH, diff.operations.get(0).type);
    Assertions.assertEquals(ListDiff.Operation.Type.DELETE, diff.operations.get(1).type);
    Assertions.assertEquals(ListDiff.Operation.Type.INSERT, diff.operations.get(2).type);
    Assertions.assertEquals(ListDiff.Operation.Type.MATCH, diff.operations.get(3).type);
  }

  @Test
  void test_diff_2() {
    List<Integer> oldList = List.of();
    var newList = List.of(4, 5, 6);
    var diff = ListDiffUtils.diff(oldList, newList, Integer::equals);
    Assertions.assertEquals(3, diff.operations.size());
    Assertions.assertEquals(ListDiff.Operation.Type.INSERT, diff.operations.get(0).type);
    Assertions.assertEquals(ListDiff.Operation.Type.INSERT, diff.operations.get(1).type);
    Assertions.assertEquals(ListDiff.Operation.Type.INSERT, diff.operations.get(2).type);
  }

  @Test
  void test_diff_3() {
    var oldList = List.of(1, 2, 3);
    List<Integer> newList = List.of();
    var diff = ListDiffUtils.diff(oldList, newList, Integer::equals);
    Assertions.assertEquals(3, diff.operations.size());
    Assertions.assertEquals(ListDiff.Operation.Type.DELETE, diff.operations.get(0).type);
    Assertions.assertEquals(ListDiff.Operation.Type.DELETE, diff.operations.get(1).type);
    Assertions.assertEquals(ListDiff.Operation.Type.DELETE, diff.operations.get(2).type);
  }

  @Test
  void test_commute_0() throws ConflictException {
    var list1 = List.of(1, 2, 3);
    var list2 = List.of(2, 2, 3);
    var list3 = List.of(2, 2, 4);

    var diff12 = ListDiffUtils.diff(list1, list2, Integer::equals);
    var diff23 = ListDiffUtils.diff(list2, list3, Integer::equals);

    var commuted = ListDiffUtils.commute(diff12, diff23, Integer::equals);
    var commuted1 = commuted.first;
    var commuted2 = commuted.second;

    Assertions.assertEquals(4, commuted1.operations.size());
    // 1 2 3 -> 1 2 4
    Assertions.assertEquals(ListDiff.Operation.Type.MATCH, commuted1.operations.get(0).type);
    Assertions.assertEquals(1, commuted1.operations.get(0).operand);
    Assertions.assertEquals(ListDiff.Operation.Type.MATCH, commuted1.operations.get(1).type);
    Assertions.assertEquals(2, commuted1.operations.get(1).operand);
    Assertions.assertEquals(ListDiff.Operation.Type.DELETE, commuted1.operations.get(2).type);
    Assertions.assertEquals(3, commuted1.operations.get(2).operand);
    Assertions.assertEquals(ListDiff.Operation.Type.INSERT, commuted1.operations.get(3).type);
    Assertions.assertEquals(4, commuted1.operations.get(3).operand);

    Assertions.assertEquals(4, commuted2.operations.size());
    // 1 2 4 -> 2 2 4
    Assertions.assertEquals(ListDiff.Operation.Type.DELETE, commuted2.operations.get(0).type);
    Assertions.assertEquals(1, commuted2.operations.get(0).operand);
    Assertions.assertEquals(ListDiff.Operation.Type.INSERT, commuted2.operations.get(1).type);
    Assertions.assertEquals(2, commuted2.operations.get(1).operand);
    Assertions.assertEquals(ListDiff.Operation.Type.MATCH, commuted2.operations.get(2).type);
    Assertions.assertEquals(2, commuted2.operations.get(2).operand);
    Assertions.assertEquals(ListDiff.Operation.Type.MATCH, commuted2.operations.get(3).type);
    Assertions.assertEquals(4, commuted2.operations.get(3).operand);
  }

  @Test
  void test_commute_1() throws ConflictException {
    var list1 = List.of(1);
    var list2 = List.of(2);
    var list3 = List.of(3);

    var diff12 = ListDiffUtils.diff(list1, list2, Integer::equals);
    var diff23 = ListDiffUtils.diff(list2, list3, Integer::equals);

    Assertions.assertThrows(ConflictException.class, () -> {
      ListDiffUtils.commute(diff12, diff23, Integer::equals);
    });
  }

  @Test
  void test_commute_2() throws ConflictException {
    var list1 = List.of(1, 2, 3);
    var list2 = List.of(1, 2, 3);
    var list3 = List.of(1, 2, 3);

    var diff12 = ListDiffUtils.diff(list1, list2, Integer::equals);
    var diff23 = ListDiffUtils.diff(list2, list3, Integer::equals);

    var commuted = ListDiffUtils.commute(diff12, diff23, Integer::equals);
    var commuted1 = commuted.first;
    var commuted2 = commuted.second;

    Assertions.assertEquals(3, commuted1.operations.size());
    Assertions.assertEquals(ListDiff.Operation.Type.MATCH, commuted1.operations.get(0).type);
    Assertions.assertEquals(1, commuted1.operations.get(0).operand);
    Assertions.assertEquals(ListDiff.Operation.Type.MATCH, commuted1.operations.get(1).type);
    Assertions.assertEquals(2, commuted1.operations.get(1).operand);
    Assertions.assertEquals(ListDiff.Operation.Type.MATCH, commuted1.operations.get(2).type);
    Assertions.assertEquals(3, commuted1.operations.get(2).operand);

    Assertions.assertEquals(3, commuted2.operations.size());
    Assertions.assertEquals(ListDiff.Operation.Type.MATCH, commuted2.operations.get(0).type);
    Assertions.assertEquals(1, commuted2.operations.get(0).operand);
    Assertions.assertEquals(ListDiff.Operation.Type.MATCH, commuted2.operations.get(1).type);
    Assertions.assertEquals(2, commuted2.operations.get(1).operand);
    Assertions.assertEquals(ListDiff.Operation.Type.MATCH, commuted2.operations.get(2).type);
    Assertions.assertEquals(3, commuted2.operations.get(2).operand);
  }
}
