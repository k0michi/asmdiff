package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ModuleNode;

import java.util.List;

public class ModuleDiffUtilsTest {
  @Test
  void test_diff() {
    List<ModuleNode> unique = ModuleNodeHelperTest.generateUnique();

    for (int i = 0; i < unique.size(); i++) {
      for (int j = 0; j < unique.size(); j++) {
        var diff = ModuleDiffUtils.diff(unique.get(i), unique.get(j));

        if (i != j) {
          Assertions.assertFalse(diff.isEmpty(), "i=" + i + ", j=" + j);
        } else {
          Assertions.assertTrue(diff.isEmpty(), "i=" + i + ", j=" + j);
        }
      }
    }
  }

  @Test
  void test_patch() {
    List<ModuleNode> unique = ModuleNodeHelperTest.generateUnique();

    for (int i = 0; i < unique.size(); i++) {
      for (int j = 0; j < unique.size(); j++) {
        var diff = ModuleDiffUtils.diff(unique.get(i), unique.get(j));
        var patchedNode = ModuleDiffUtils.patch(unique.get(i), diff);
        Assertions.assertTrue(ModuleNodeHelper.equals(patchedNode, unique.get(j)), "i=" + i + ", j=" + j);
      }
    }
  }
}
