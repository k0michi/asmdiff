package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.BinaryReader;
import com.koyomiji.asmweaver.io.BinaryWriter;
import com.koyomiji.asmweaver.util.AutoIncrementBiHashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

class MethodDiffUtilsTest {
  @Test
  void test_diff() {
    TestUtils.verifyDiffEmpty(MethodNodeHelperTest::generateUnique, MethodDiffUtils::diff);
  }

  @Test
  void test_diff_readWrite() throws IOException {
    var unique1 = MethodNodeHelperTest.generateUnique();
    var unique2 = MethodNodeHelperTest.generateUnique();

    for (int i = 0; i < unique1.size(); i++) {
      for (int j = 0; j < unique2.size(); j++) {
        MethodNode node1 = unique1.get(i);
        MethodNode node2 = unique2.get(j);
        MethodDiff diff = MethodDiffUtils.diff(node1, node2);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MethodDiffUtils.write(diff, new BinaryWriter(baos));
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        MethodDiff read = MethodDiffUtils.read(new BinaryReader(bais));

        MethodNode patched = MethodDiffUtils.patch(node1, read);

        MethodDiff d = MethodDiffUtils.diff(node2, patched);
        Assertions.assertNull(d, "i=" + i + ", j=" + j);
        Assertions.assertTrue(MethodNodeHelper.equalsNormalizeLabels(node2, patched), "i=" + i + ", j=" + j);
      }
    }
  }
}
