package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.ModuleProvideNode;

import java.util.Objects;

public class ModuleProvideNodeHelper {
  public static boolean equals(ModuleProvideNode node1, ModuleProvideNode node2) {
    if (node1 == node2) {
      return true;
    }

    if (node1 == null || node2 == null) {
      return false;
    }

    if (node1.getClass() != node2.getClass()) {
      return false;
    }

    return Objects.equals(node1.service, node2.service)
            && Objects.equals(node1.providers, node2.providers);
  }

  public static int hashCode(ModuleProvideNode node) {
    if (node == null) {
      return 0;
    }

    return Objects.hash(node.service, node.providers);
  }
}
