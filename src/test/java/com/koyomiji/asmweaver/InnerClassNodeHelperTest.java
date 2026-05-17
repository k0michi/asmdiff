package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.BinaryReader;
import com.koyomiji.asmweaver.io.BinaryWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.InnerClassNode;

import java.io.*;
import java.util.List;

class InnerClassNodeHelperTest {
  static List<InnerClassNode> generateUnique() {
    return List.of(
            new InnerClassNode("A", "B", "C", 0),
            new InnerClassNode("A_", "B", "C", 0),
            new InnerClassNode("A", "B_", "C", 0),
            new InnerClassNode("A", "B", "C_", 0),
            new InnerClassNode("A", "B", "C", 1)
    );
  }

  @Test
  void test_equals_0() {
    var uniqueNodes1 = generateUnique();
    var uniqueNodes2 = generateUnique();

    for (int i = 0; i < uniqueNodes1.size(); i++) {
      Assertions.assertTrue(InnerClassNodeHelper.equals(uniqueNodes1.get(i), uniqueNodes2.get(i)));
    }
  }

  @Test
  void test_equals_1() {
    var uniqueNodes = generateUnique();

    for (int i = 0; i < uniqueNodes.size(); i++) {
      for (int j = 0; j < uniqueNodes.size(); j++) {
        if (i != j) {
          Assertions.assertFalse(InnerClassNodeHelper.equals(uniqueNodes.get(i), uniqueNodes.get(j)));
        }
      }
    }
  }

  @Test
  void test_hashCode_0() {
    var uniqueNodes = generateUnique();

    for (int i = 0; i < uniqueNodes.size(); i++) {
      Assertions.assertEquals(InnerClassNodeHelper.hashCode(uniqueNodes.get(i)), InnerClassNodeHelper.hashCode(uniqueNodes.get(i)));
    }
  }

  @Test
  void test_readWrite_roundTrip() throws IOException {
    var uniqueNodes = generateUnique();

    for (int i = 0; i < uniqueNodes.size(); i++) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      BinaryWriter out = new BinaryWriter(baos);
      InnerClassNodeHelper.write(uniqueNodes.get(i), out);

      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      BinaryReader in = new BinaryReader(bais);
      InnerClassNode read = InnerClassNodeHelper.read(in);

      Assertions.assertTrue(InnerClassNodeHelper.equals(uniqueNodes.get(i), read));
    }
  }
}
