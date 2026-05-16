package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;

class RecordComponentDiffUtilsTest {
  @Test
  void test_diff_0() {
    var unique = RecordComponentNodeHelperTest.generateUnique();

    for (int i = 0; i < unique.size(); i++) {
      for (int j = 0; j < unique.size(); j++) {
        var diff = RecordComponentDiffUtils.diff(unique.get(i), unique.get(j));

        if (i != j) {
          Assertions.assertFalse(diff.isEmpty());
        } else {
          Assertions.assertTrue(diff.isEmpty());
        }
      }
    }
  }

  @Test
  void test_patch_0() {
    var unique = RecordComponentNodeHelperTest.generateUnique();

    for (int i = 0; i < unique.size(); i++) {
      for (int j = 0; j < unique.size(); j++) {
        var diff = RecordComponentDiffUtils.diff(unique.get(i), unique.get(j));
        var patchedNode = RecordComponentDiffUtils.patch(unique.get(i), diff);
        Assertions.assertTrue(RecordComponentNodeHelper.equals(patchedNode, unique.get(j)));
      }
    }
  }

  @Test
  void test_readWrite() throws IOException {
    var unique = RecordComponentNodeHelperTest.generateUnique();

    for (int i = 0; i < unique.size(); i++) {
      for (int j = 0; j < unique.size(); j++) {
        var diff = RecordComponentDiffUtils.diff(unique.get(i), unique.get(j));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        RecordComponentDiffUtils.write(diff, dos);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DataInputStream dis = new DataInputStream(bais);

        var readDiff = RecordComponentDiffUtils.read(dis);
        var patchedNode2 = RecordComponentDiffUtils.patch(unique.get(i), readDiff);
        Assertions.assertTrue(RecordComponentNodeHelper.equals(patchedNode2, unique.get(j)));
      }
    }
  }
}
