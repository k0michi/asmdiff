package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.CustomDataInput;
import com.koyomiji.asmweaver.io.DataStreamHelper;
import org.objectweb.asm.tree.InnerClassNode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

public class InnerClassNodeHelper {
  public static boolean equals(InnerClassNode node1, InnerClassNode node2) {
    if (node1 == node2) {
      return true;
    }

    if (node1 == null || node2 == null) {
      return false;
    }

    return Objects.equals(node1.name, node2.name)
            && Objects.equals(node1.outerName, node2.outerName)
            && Objects.equals(node1.innerName, node2.innerName)
            && node1.access == node2.access;
  }

  public static int hashCode(InnerClassNode node) {
    if (node == null) {
      return 0;
    }

    return Objects.hash(node.name, node.outerName, node.innerName, node.access);
  }

  public static void write(InnerClassNode node, DataOutputStream out) throws IOException {
    out.writeUTF(node.name);
    DataStreamHelper.writeUTFNullable(out, node.outerName);
    DataStreamHelper.writeUTFNullable(out, node.innerName);
    out.writeInt(node.access);
  }

  public static InnerClassNode read(CustomDataInput in) throws IOException {
    String name = in.readUTF();
    String outerName = DataStreamHelper.readUTFNullable(in);
    String innerName = DataStreamHelper.readUTFNullable(in);
    int access = in.readInt();
    return new InnerClassNode(name, outerName, innerName, access);
  }
}
