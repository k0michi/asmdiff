package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ParameterNode;

import java.util.List;

public class ParameterNodeHelperTest {
  List<ParameterNode> generateUnique() {
    return List.of(
            new ParameterNode("param1", 0),
            new ParameterNode("param2", 0),
            new ParameterNode(null, 0),
            new ParameterNode("param1", 1)
    );
  }

  @Test
  void test_equals() {
    TestUtils.verifyEquals(
            this::generateUnique,
            ParameterNodeHelper::equals
    );
  }

  @Test
  void test_hashCode() {
    TestUtils.verifyHashCode(
            this::generateUnique,
            ParameterNodeHelper::hashCode
    );
  }
}
