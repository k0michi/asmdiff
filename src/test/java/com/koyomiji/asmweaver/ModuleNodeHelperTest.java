package com.koyomiji.asmweaver;

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
    TestUtils.verifyEquals(
            ModuleNodeHelperTest::generateUnique,
            ModuleNodeHelper::equals
    );
  }

  @Test
  void test_hashCode() {
    TestUtils.verifyHashCode(
            ModuleNodeHelperTest::generateUnique,
            ModuleNodeHelper::hashCode
    );
  }
}
