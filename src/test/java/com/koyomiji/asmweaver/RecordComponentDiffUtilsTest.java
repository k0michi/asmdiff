package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RecordComponentDiffUtilsTest {
  @Test
  void test_diff_0() {
    var unique = RecordComponentNodeHelperTest.generateUnique();

    for (int i = 0; i < unique.size(); i++) {
      for (int j = 0; j < unique.size(); j++) {
        var diff = RecordComponentDiffUtils.diff(unique.get(i), unique.get(j));

        if (i != j) {
          Assertions.assertFalse(diff.isEmpty());
        } else {
          Assertions.assertTrue(diff.isEmpty());
        }
      }
    }
  }

  @Test
  void test_patch_0() {
    var unique = RecordComponentNodeHelperTest.generateUnique();

    for (int i = 0; i < unique.size(); i++) {
      for (int j = 0; j < unique.size(); j++) {
        var diff = RecordComponentDiffUtils.diff(unique.get(i), unique.get(j));
        var patchedNode = RecordComponentDiffUtils.patch(unique.get(i), diff);
        Assertions.assertTrue(RecordComponentNodeHelper.equals(patchedNode, unique.get(j)));
      }
    }
  }
}
