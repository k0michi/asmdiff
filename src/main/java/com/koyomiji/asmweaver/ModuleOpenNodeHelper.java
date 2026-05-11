package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.ModuleOpenNode;

import java.util.Objects;

public class ModuleOpenNodeHelper {
  public static boolean equals(ModuleOpenNode node1, ModuleOpenNode node2) {
    if (node1 == node2) {
      return true;
    }

    if (node1 == null || node2 == null) {
      return false;
    }

    if (node1.getClass() != node2.getClass()) {
      return false;
    }

    return Objects.equals(node1.packaze, node2.packaze)
            && node1.access == node2.access
            && Objects.equals(node1.modules, node2.modules);
  }

  public static int hashCode(ModuleOpenNode node) {
    if (node == null) {
      return 0;
    }

    return Objects.hash(node.packaze, node.access, node.modules);
  }
}
