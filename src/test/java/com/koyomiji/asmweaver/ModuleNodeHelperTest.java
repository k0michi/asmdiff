package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ModuleExportNode;
import org.objectweb.asm.tree.ModuleNode;
import org.objectweb.asm.tree.ModuleProvideNode;
import org.objectweb.asm.tree.ModuleRequireNode;

import java.util.List;

class ModuleNodeHelperTest {
  List<ModuleNode> generateUnique() {
    ModuleNode node1 = new ModuleNode("module", 0, null);
    ModuleNode node2 = new ModuleNode("module_", 0, null);
    ModuleNode node3 = new ModuleNode("module", 1, null);
    ModuleNode node4 = new ModuleNode("module", 0, "version");
    ModuleNode node5 = new ModuleNode("module", 0, null);
    node5.mainClass = "mainClass";

    ModuleNode node6 = new ModuleNode("module", 0, null);
    node6.packages = List.of("package");

    ModuleNode node7 = new ModuleNode("module", 0, null);
    node7.packages = List.of("package2");

    ModuleNode node8 = new ModuleNode("module", 0, null);
    node8.requires = List.of(new ModuleRequireNode("module", 0, null));

    ModuleNode node9 = new ModuleNode("module", 0, null);
    node9.requires = List.of(new ModuleRequireNode("module", 1, null));

    ModuleNode node10 = new ModuleNode("module", 0, null);
    node10.requires = List.of(new ModuleRequireNode("module", 0, "version"));

    ModuleNode node11 = new ModuleNode("module", 0, null);
    node11.exports = List.of(new ModuleExportNode("package", 0, null));

    ModuleNode node12 = new ModuleNode("module", 0, null);
    node12.exports = List.of(new ModuleExportNode("package", 1, null));

    ModuleNode node13 = new ModuleNode("module", 0, null);
    node13.exports = List.of(new ModuleExportNode("package", 0, List.of("module")));

    ModuleNode node14 = new ModuleNode("module", 0, null);
    node14.uses = List.of("service");

    ModuleNode node15 = new ModuleNode("module", 0, null);
    node15.uses = List.of("service2");

    ModuleNode node16 = new ModuleNode("module", 0, null);
    node16.provides = List.of(new ModuleProvideNode("service", List.of("provider")));

    ModuleNode node17 = new ModuleNode("module", 0, null);
    node17.provides = List.of(new ModuleProvideNode("service", List.of("provider2")));

    return List.of(node1, node2, node3, node4, node5, node6, node7, node8, node9, node10, node11, node12, node13, node14, node15, node16, node17);
  }

  @Test
  void test_equals() {
    List<ModuleNode> unique = generateUnique();

    for (int i = 0; i < unique.size(); i++) {
      for (int j = 0; j < unique.size(); j++) {
        if (i == j) {
          Assertions.assertTrue(ModuleNodeHelper.equals(unique.get(i), unique.get(j)), "i=" + i + ", j=" + j);
        } else {
          Assertions.assertFalse(ModuleNodeHelper.equals(unique.get(i), unique.get(j)), "i=" + i + ", j=" + j);
        }
      }
    }
  }

  @Test
  void test_hashCode() {
    List<ModuleNode> unique = generateUnique();

    for (int i = 0; i < unique.size(); i++) {
      Assertions.assertEquals(ModuleNodeHelper.hashCode(unique.get(i)), ModuleNodeHelper.hashCode(unique.get(i)), "i=" + i);
    }
  }
}
