package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.HashCodeBuilder;
import org.objectweb.asm.tree.ModuleNode;

import java.util.Objects;

public class ModuleNodeHelper {
  public static boolean equals(ModuleNode node1, ModuleNode node2) {
    if (node1 == node2) {
      return true;
    }

    if (node1 == null || node2 == null) {
      return false;
    }

    if (node1.getClass() != node2.getClass()) {
      return false;
    }

    return Objects.equals(node1.name, node2.name)
            && node1.access == node2.access
            && Objects.equals(node1.version, node2.version)
            && Objects.equals(node1.mainClass, node2.mainClass)
            && Objects.equals(node1.packages, node2.packages)
            && ListHelper.equals(node1.requires, node2.requires, ModuleRequireNodeHelper::equals)
            && ListHelper.equals(node1.exports, node2.exports, ModuleExportNodeHelper::equals)
            && ListHelper.equals(node1.opens, node2.opens, ModuleOpenNodeHelper::equals)
            && ListHelper.equals(node1.uses, node2.uses)
            && ListHelper.equals(node1.provides, node2.provides, ModuleProvideNodeHelper::equals);
  }

  public static int hashCode(ModuleNode node) {
    if (node == null) {
      return 0;
    }

    return new HashCodeBuilder()
            .append(node.name)
            .append(node.access)
            .append(node.version)
            .append(node.mainClass)
            .append(node.packages)
            .append(node.requires,
                    l->ListHelper.hashCode(l, ModuleRequireNodeHelper::hashCode)
            )
            .append(node.exports,
                    l->ListHelper.hashCode(l, ModuleExportNodeHelper::hashCode)
            )
            .append(node.opens,
                    l->ListHelper.hashCode(l, ModuleOpenNodeHelper::hashCode)
            )
            .append(node.uses)
            .append(node.provides,
                    l->ListHelper.hashCode(l, ModuleProvideNodeHelper::hashCode)
            )
            .build();
  }
}
