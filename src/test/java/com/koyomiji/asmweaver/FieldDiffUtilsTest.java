package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.BinaryReader;
import com.koyomiji.asmweaver.io.BinaryWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;

class FieldDiffUtilsTest {
//  @Test
//  void test_diff_0() {
//    var unique = FieldNodeHelperTest.generateUnique();
//
//    for (int i = 0; i < unique.size(); i++) {
//      for (int j = 0; j < unique.size(); j++) {
//        var diff = FieldDiffUtils.diff(unique.get(i), unique.get(j));
//
//        if (i != j) {
//          Assertions.assertFalse(diff.isEmpty());
//        } else {
//          Assertions.assertTrue(diff.isEmpty());
//        }
//      }
//    }
//  }

  @Test
  void test_diff_empty() {
    TestUtils.verifyDiffEmpty(FieldNodeHelperTest::generateUnique, FieldDiffUtils::diff);
  }

  @Test
  void test_patch_0() {
    var unique = FieldNodeHelperTest.generateUnique();

    for (int i = 0; i < unique.size(); i++) {
      for (int j = 0; j < unique.size(); j++) {
        if (i != j) {
          var diff = FieldDiffUtils.diff(unique.get(i), unique.get(j));
          var patchedNode = FieldDiffUtils.patch(unique.get(i), diff);
          Assertions.assertTrue(FieldNodeHelper.equals(patchedNode, unique.get(j)), "Failed to patch from index " + i + " to index " + j);
        }
      }
    }
  }

  @Test
  void test_readWrite() throws IOException {
    var unique = FieldNodeHelperTest.generateUnique();

    for (int i = 0; i < unique.size(); i++) {
      for (int j = 0; j < unique.size(); j++) {
        if (i != j) {
          var diff = FieldDiffUtils.diff(unique.get(i), unique.get(j));
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          BinaryWriter dos = new BinaryWriter(baos);
          FieldDiffUtils.write(diff, dos);

          ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
          BinaryReader dis = new BinaryReader(bais);
          FieldDiff readDiff = FieldDiffUtils.read(dis);

          Assertions.assertTrue(FieldNodeHelper.equals(
                  FieldDiffUtils.patch(unique.get(i), readDiff),
                  unique.get(j)
          ));
        }
      }
    }
  }
}
