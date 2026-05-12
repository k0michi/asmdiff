package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

class ModuleNodeHelperTest {
  static List<ModuleNode> generateUnique() {
    ArrayList<ModuleNode> list = new ArrayList<>();

    list.add(new ModuleNode("module", 0, null));
    list.add(new ModuleNode("module_", 0, null));
    list.add(new ModuleNode("module", 1, null));
    list.add(new ModuleNode("module", 0, "version"));
    list.add(new ModuleNode("module", 0, null));
    list.getLast().mainClass = "mainClass";
    list.add(new ModuleNode("module", 0, null));
    list.getLast().packages = List.of("package");
    list.add(new ModuleNode("module", 0, null));
    list.getLast().packages = List.of("package2");
    list.add(new ModuleNode("module", 0, null));
    list.getLast().requires = List.of(new ModuleRequireNode("module", 0, null));
    list.add(new ModuleNode("module", 0, null));
    list.getLast().requires = List.of(new ModuleRequireNode("module2", 0, null));
    list.add(new ModuleNode("module", 0, null));
    list.getLast().requires = List.of(new ModuleRequireNode("module", 1, null));
    list.add(new ModuleNode("module", 0, null));
    list.getLast().requires = List.of(new ModuleRequireNode("module", 0, "version"));
    list.add(new ModuleNode("module", 0, null));
    list.getLast().exports = List.of(new ModuleExportNode("package", 0, null));
    list.add(new ModuleNode("module", 0, null));
    list.getLast().exports = List.of(new ModuleExportNode("package", 1, null));
    list.add(new ModuleNode("module", 0, null));
    list.getLast().exports = List.of(new ModuleExportNode("package", 0, List.of("module")));
    list.add(new ModuleNode("module", 0, null));
    list.getLast().opens = List.of(
            new ModuleOpenNode("package", 0, null)
    );
    list.add(new ModuleNode("module", 0, null));
    list.getLast().opens = List.of(
            new ModuleOpenNode("package2", 0, null)
    );
    list.add(new ModuleNode("module", 0, null));
    list.getLast().opens = List.of(
            new ModuleOpenNode("package", 1, null)
    );
    list.add(new ModuleNode("module", 0, null));
    list.getLast().opens = List.of(
            new ModuleOpenNode("package", 0, List.of("module"))
    );
    list.add(new ModuleNode("module", 0, null));
    list.getLast().uses = List.of("service");
    list.add(new ModuleNode("module", 0, null));
    list.getLast().uses = List.of("service2");
    list.add(new ModuleNode("module", 0, null));
    list.getLast().provides = List.of(new ModuleProvideNode("service", List.of("provider")));
    list.add(new ModuleNode("module", 0, null));
    list.getLast().provides = List.of(new ModuleProvideNode("service2", List.of("provider")));
    list.add(new ModuleNode("module", 0, null));
    list.getLast().provides = List.of(new ModuleProvideNode("service", List.of("provider2")));

    return list;
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
