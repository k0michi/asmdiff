package com.koyomiji.asmpatch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NullablePatcherTest {
  @Test
  void testPatch_match() {
    NullableDiffer<String, ValuePatch<String>> differ = new NullableDiffer<>(new ValueDiffer<>());
    NullablePatcher<String, ValuePatch<String>> patcher = new NullablePatcher<>(new ValuePatcher<>());
    var patch = differ.diff("old", "new");
    var patched = patcher.patch("old", patch);
    Assertions.assertEquals("new", patched);
  }

  @Test
  void testPatch_add() {
    NullableDiffer<String, ValuePatch<String>> differ = new NullableDiffer<>(new ValueDiffer<>());
    NullablePatcher<String, ValuePatch<String>> patcher = new NullablePatcher<>(new ValuePatcher<>());
    var patch = differ.diff(null, "new");
    var patched = patcher.patch(null, patch);
    Assertions.assertEquals("new", patched);
  }

  @Test
  void testPatch_remove() {
    NullableDiffer<String, ValuePatch<String>> differ = new NullableDiffer<>(new ValueDiffer<>());
    NullablePatcher<String, ValuePatch<String>> patcher = new NullablePatcher<>(new ValuePatcher<>());
    var patch = differ.diff("old", null);
    var patched = patcher.patch("old", patch);
    Assertions.assertNull(patched);
  }

  @Test
  void testCanPatch() {
    NullableDiffer<String, ValuePatch<String>> differ = new NullableDiffer<>(new ValueDiffer<>());
    NullablePatcher<String, ValuePatch<String>> patcher = new NullablePatcher<>(new ValuePatcher<>());
    var patch = differ.diff("old", "new");
    Assertions.assertTrue(patcher.canPatch("old", patch));
    Assertions.assertFalse(patcher.canPatch(null, patch));
  }
}
