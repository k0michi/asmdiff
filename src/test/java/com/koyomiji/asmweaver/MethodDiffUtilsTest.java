package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Test;

class MethodDiffUtilsTest {
  @Test
  void test_diff() {
    TestUtils.verifyDiffEmpty(MethodNodeHelperTest::generateUnique, MethodDiffUtils::diff);
  }
}
