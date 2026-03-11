package com.koyomiji.asmpatch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValuePatcherTest {
  @Test
  void testPatchUnchanged() {
    var patcher = new ValuePatcher<String>();
    var oldValue = "old";
    ValuePatch<String> patch = ValuePatch.unchanged();
    var newValue = patcher.patch(oldValue, patch);
    Assertions.assertEquals("old", newValue);
  }

  @Test
  void testPatchChanged() {
    var patcher = new ValuePatcher<String>();
    var oldValue = "old";
    ValuePatch<String> patch = ValuePatch.changed(oldValue, "new");
    var newValue = patcher.patch(oldValue, patch);
    Assertions.assertEquals("new", newValue);
  }
}
