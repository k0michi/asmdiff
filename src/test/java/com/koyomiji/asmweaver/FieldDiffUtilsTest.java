package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class FieldDiffUtilsTest {
  @Test
  void test_diff_0() {
    var unique = FieldNodeHelperTest.generateUnique();

    for (int i = 0; i < unique.size(); i++) {
      for (int j = 0; j < unique.size(); j++) {
        var diff = FieldDiffUtils.diff(unique.get(i), unique.get(j));

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
    var unique = FieldNodeHelperTest.generateUnique();

    for (int i = 0; i < unique.size(); i++) {
      for (int j = 0; j < unique.size(); j++) {
        if (i != j) {
          var diff = FieldDiffUtils.diff(unique.get(i), unique.get(j));
          var patchedNode = FieldDiffUtils.patch(unique.get(i), diff);
          Assertions.assertTrue(FieldNodeHelper.equals(patchedNode, unique.get(j)), "Failed to patch from index " + i + " to index " + j);
        }
      }
    }
  }
}
