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

    return Objects.equals(node1.name, node2.name)
            && Objects.equals(node1.access, node2.access);
  }
}
