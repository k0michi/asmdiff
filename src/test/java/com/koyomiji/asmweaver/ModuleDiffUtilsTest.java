package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.BinaryReader;
import com.koyomiji.asmweaver.io.BinaryWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ModuleNode;

import java.io.*;
import java.util.List;

public class ModuleDiffUtilsTest {
  @Test
  void test_diff() {
    TestUtils.verifyDiffEmpty(ModuleNodes::generateUnique, ModuleDiffUtils::diff);
  }

  @Test
  void test_patch() {
    List<ModuleNode> unique = ModuleNodes.generateUnique();

    for (int i = 0; i < unique.size(); i++) {
      for (int j = 0; j < unique.size(); j++) {
        var diff = ModuleDiffUtils.diff(unique.get(i), unique.get(j));
        var patchedNode = ModuleDiffUtils.patch(unique.get(i), diff);
        Assertions.assertTrue(ModuleNodeHelper.equals(patchedNode, unique.get(j)), "i=" + i + ", j=" + j);
      }
    }
  }

  @Test
  void test_readWrite() throws IOException {
    List<ModuleNode> unique = ModuleNodes.generateUnique();

    for (int i = 0; i < unique.size(); i++) {
      for (int j = 0; j < unique.size(); j++) {
        var diff = ModuleDiffUtils.diff(unique.get(i), unique.get(j));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BinaryWriter dos = new BinaryWriter(baos);
        ModuleDiffUtils.write(diff, dos);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        BinaryReader dis = new BinaryReader(bais);

        var readDiff = ModuleDiffUtils.read(dis);
        var patchedNode2 = ModuleDiffUtils.patch(unique.get(i), readDiff);
        Assertions.assertTrue(ModuleNodeHelper.equals(patchedNode2, unique.get(j)));
      }
    }
  }
}
