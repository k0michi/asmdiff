package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MethodDiffUtilsTest {
  @Test
  void test_diff() {
    var unique1 = MethodNodeHelperTest.generateUnique();
    var unique2 = MethodNodeHelperTest.generateUnique();

    for (int i = 0; i < unique1.size(); i++) {
      for (int j = 0; j < unique2.size(); j++) {
        MethodDiff diff = MethodDiffUtils.diff(unique1.get(i), unique2.get(j));

        if (i == j) {
          Assertions.assertTrue(diff.isEmpty(), "i=" + i + ", j=" + j);
        } else {
          Assertions.assertFalse(diff.isEmpty(), "i=" + i + ", j=" + j);
        }
      }
    }
  }
}
