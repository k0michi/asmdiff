package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldNode;

import java.io.IOException;
import java.util.List;

public class FieldNodeHelperTest {
  static List<FieldNode> generateUnique() {
    return List.of(
            new FieldNode(0, "field1", "I", null, null),
            new FieldNode(Opcodes.ACC_PUBLIC, "field1", "I", null, null),
            new FieldNode(0, "field2", "I", null, null),
            new FieldNode(0, "field1", "J", null, null),
            new FieldNode(0, "field1", "I", "Ljava/lang/Object;", null),
            new FieldNode(0, "field1", "I", null, 123),
            new FieldNode(0, "field1", "I", null, (float) 123),
            new FieldNode(0, "field1", "I", null, (long) 123),
            new FieldNode(0, "field1", "I", null, (double) 123),
            new FieldNode(0, "field1", "I", null, "a")
    );
  }

  @Test
  void test_equals() {
    TestUtils.verifyEquals(
            FieldNodeHelperTest::generateUnique,
            FieldNodeHelper::equals
    );
  }

  @Test
  void test_hashCode() {
    TestUtils.verifyHashCode(
            FieldNodeHelperTest::generateUnique,
            FieldNodeHelper::hashCode
    );
  }

  @Test
  void test_roundTrip() throws IOException {
    TestUtils.verifyRoundTrip(
            FieldNodeHelperTest::generateUnique,
            FieldNodeHelper::write,
            FieldNodeHelper::read,
            FieldNodeHelper::equals
    );
  }
}
