package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class ModuleNodeHelperTest {
  @Test
  void test_equals() {
    TestUtils.verifyEquals(
            ModuleNodes::generateUnique,
            ModuleNodeHelper::equals
    );
  }

  @Test
  void test_hashCode() {
    TestUtils.verifyHashCode(
            ModuleNodes::generateUnique,
            ModuleNodeHelper::hashCode
    );
  }

  @Test
  void test_roundTrip() throws IOException {
    TestUtils.verifyRoundTrip(
            ModuleNodes::generateUnique,
            ModuleNodeHelper::write,
            ModuleNodeHelper::read,
            ModuleNodeHelper::equals
    );
  }
}
