package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.InnerClassNode;

import java.util.Objects;

public class InnerClassNodeHelper {
  public static boolean equals(InnerClassNode node1, InnerClassNode node2) {
    if (node1 == node2) {
      return true;
    }
    if (node1 == null || node2 == null) {
      return false;
    }
    return Objects.equals(node1.name, node2.name)
        && Objects.equals(node1.outerName, node2.outerName)
        && Objects.equals(node1.innerName, node2.innerName)
        && node1.access == node2.access;
  }
}
