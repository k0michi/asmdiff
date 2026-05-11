package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.HashCodeBuilder;
import org.objectweb.asm.tree.ModuleExportNode;

import java.util.Objects;

public class ModuleExportNodeHelper {
  public static boolean equals(ModuleExportNode node1, ModuleExportNode node2) {
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
            && ListHelper.equalsNullToEmpty(node1.modules, node2.modules, String::equals);
  }

  public static int hashCode(ModuleExportNode node) {
    if (node == null) {
      return 0;
    }

    return new HashCodeBuilder()
            .append(node.packaze)
            .append(node.access)
            .append(node.modules,
                    (l) -> ListHelper.hashCodeNullToEmpty(l, String::hashCode)
            ).build();
  }
}
