package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.ParameterNode;

import java.util.Objects;

public class ParameterNodeHelper {
  public static boolean equals(ParameterNode node1, ParameterNode node2) {
    if (node1 == node2) {
      return true;
    }

    if (node1 == null || node2 == null) {
      return false;
    }

    if (node1.getClass() != node2.getClass()) {
      return false;
    }

    if (node1.getClass() == ParameterNode.class) {
      return Objects.equals(node1.name, node2.name)
              && node1.access == node2.access;
    }

    // Non-standard
    return Objects.equals(node1, node2);
  }

  public static int hashCode(ParameterNode node) {
    if (node == null) {
      return 0;
    }

    return Objects.hash(node.name, node.access);
  }
}
