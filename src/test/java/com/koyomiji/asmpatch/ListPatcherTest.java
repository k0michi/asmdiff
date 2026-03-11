package com.koyomiji.asmpatch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ListPatcherTest {
  @Test
  void testPatch() {
    var patcher = new ListPatcher<String, ValuePatch<String>, String>(new ValuePatcher<>(), new ValueKeyProvider<>());
    var oldList = List.of("a", "b", "c");
    var patch = new ListPatch<String, ValuePatch<String>, String>(List.of(
            ListPatch.Entry.match("a", ValuePatch.unchanged()),
            ListPatch.Entry.add("x"),
            ListPatch.Entry.match("b", ValuePatch.unchanged()),
            ListPatch.Entry.remove("c")
    ));

    var newList = patcher.patch(oldList, patch);
    Assertions.assertEquals(List.of("a", "x", "b"), newList);
  }

  @Test
  void testCanPatch() {
    var patcher = new ListPatcher<String, ValuePatch<String>, String>(new ValuePatcher<>(), new ValueKeyProvider<>());
    var oldList = List.of("a", "b", "c");

    Assertions.assertTrue(patcher.canPatch(oldList, new ListPatch<String, ValuePatch<String>, String>(List.of(
            ListPatch.Entry.match("a", ValuePatch.unchanged()),
            ListPatch.Entry.add("x"),
            ListPatch.Entry.match("b", ValuePatch.unchanged()),
            ListPatch.Entry.remove("c")
    ))));
    Assertions.assertFalse(patcher.canPatch(oldList, new ListPatch<String, ValuePatch<String>, String>(List.of(
            ListPatch.Entry.match("a", ValuePatch.unchanged()),
            ListPatch.Entry.add("x"),
            ListPatch.Entry.match("b", ValuePatch.unchanged()),
            ListPatch.Entry.remove("d")
    ))));
  }
}
