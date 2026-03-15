package com.koyomiji.asmpatch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NullableDifferTest {
  @Test
  void testUnchanged() {
    NullableDiffer<String, ValuePatch<String>> differ = new NullableDiffer<>(new ValueDiffer<>());
    var patch = differ.diff(null, null);
    var entries = patch.entries;
    Assertions.assertEquals(0, entries.size());
  }

  @Test
  void testAdded() {
    NullableDiffer<String, ValuePatch<String>> differ = new NullableDiffer<>(new ValueDiffer<>());
    var patch = differ.diff(null, "new");
    var entries = patch.entries;
    Assertions.assertEquals(1, entries.size());
    Assertions.assertEquals(NullablePatch.EntryType.ADD, entries.get(0).type);
    Assertions.assertEquals("new", entries.get(0).newValue);
  }

  @Test
  void testRemoved() {
    NullableDiffer<String, ValuePatch<String>> differ = new NullableDiffer<>(new ValueDiffer<>());
    var patch = differ.diff("old", null);
    var entries = patch.entries;
    Assertions.assertEquals(1, entries.size());
    Assertions.assertEquals(NullablePatch.EntryType.REMOVE, entries.get(0).type);
  }

  @Test
  void testMatched() {
    NullableDiffer<String, ValuePatch<String>> differ = new NullableDiffer<>(new ValueDiffer<>());
    var patch = differ.diff("old", "new");
    var entries = patch.entries;
    Assertions.assertEquals(1, entries.size());
    Assertions.assertEquals(NullablePatch.EntryType.MATCH, entries.get(0).type);
    Assertions.assertEquals("new", entries.get(0).patch.newValue);
  }
}
