package com.koyomiji.asmpatch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ListDifferTest {
  @Test
  void testSameLists() {
    var differ = new ListDiffer<String, ValuePatch<String>, String>(new ValueDiffer<>(), new ValueKeyProvider<>());
    var listA = List.of("a", "b", "c");
    var listB = List.of("a", "b", "c");
    var diff = differ.diff(listA, listB);
    Assertions.assertEquals(3, diff.entries.size());
    Assertions.assertTrue(diff.entries.stream().allMatch(
        e -> e.type == ListPatch.EntryType.MATCH));
  }

  @Test
  void testDifferentLists() {
    var differ = new ListDiffer<String, ValuePatch<String>, String>(new ValueDiffer<>(), new ValueKeyProvider<>());
    var listA = List.of("a", "b", "c");
    var listB = List.of("a", "x", "c");
    var diff = differ.diff(listA, listB);
    Assertions.assertEquals(4, diff.entries.size());
    Assertions.assertSame(diff.entries.get(0).type, ListPatch.EntryType.MATCH);
    Assertions.assertSame(diff.entries.get(1).type, ListPatch.EntryType.REMOVE);
    Assertions.assertSame(diff.entries.get(2).type, ListPatch.EntryType.ADD);
    Assertions.assertSame(diff.entries.get(3).type, ListPatch.EntryType.MATCH);
  }

  @Test
  void testEmptyLists() {
    var differ = new ListDiffer<String, ValuePatch<String>, String>(new ValueDiffer<>(), new ValueKeyProvider<>());
    List<String> listA = List.of();
    List<String> listB = List.of();
    var diff = differ.diff(listA, listB);
    Assertions.assertTrue(diff.entries.isEmpty());
  }
}
