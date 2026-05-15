package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.RecordComponentNode;

import java.io.IOException;
import java.util.List;

class RecordComponentNodeHelperTest {
  static List<RecordComponentNode> generateUnique() {
    return List.of(
            new RecordComponentNode("component1", "I", null),
            new RecordComponentNode("component2", "I", null),
            new RecordComponentNode("component1", "J", null),
            new RecordComponentNode("component1", "I", "Ljava/lang/Object;")
    );
  }

  @Test
  void test_equals() {
    TestUtils.verifyEquals(
            RecordComponentNodeHelperTest::generateUnique,
            RecordComponentNodeHelper::equals
    );
  }

  @Test
  void test_hashCode() {
    TestUtils.verifyHashCode(
            RecordComponentNodeHelperTest::generateUnique,
            RecordComponentNodeHelper::hashCode
    );
  }

  @Test
  void test_roundTrip() throws IOException {
    TestUtils.verifyRoundTrip(
            RecordComponentNodeHelperTest::generateUnique,
            RecordComponentNodeHelper::write,
            RecordComponentNodeHelper::read,
            RecordComponentNodeHelper::equals
    );
  }
}
