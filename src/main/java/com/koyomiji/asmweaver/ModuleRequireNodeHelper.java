package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.ModuleRequireNode;

import java.util.Objects;

public class ModuleRequireNodeHelper {
  public static boolean equals(ModuleRequireNode node1, ModuleRequireNode node2) {
    if (node1 == node2) {
      return true;
    }

    if (node1 == null || node2 == null) {
      return false;
    }

    if (node1.getClass() != node2.getClass()) {
      return false;
    }

    return Objects.equals(node1.module, node2.module)
            && node1.access == node2.access
            && Objects.equals(node1.version, node2.version);
  }

  public static int hashCode(ModuleRequireNode node) {
    if (node == null) {
      return 0;
    }

    return Objects.hash(node.module, node.access, node.version);
  }
}
