package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.DataStreamHelper;
import com.koyomiji.asmweaver.util.HashCodeBuilder;
import org.objectweb.asm.tree.ModuleNode;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

public class ModuleNodeHelper {
  public static boolean equals(ModuleNode node1, ModuleNode node2) {
    if (node1 == node2) {
      return true;
    }

    if (node1 == null || node2 == null) {
      return false;
    }

    if (node1.getClass() != node2.getClass()) {
      return false;
    }

    return Objects.equals(node1.name, node2.name)
            && node1.access == node2.access
            && Objects.equals(node1.version, node2.version)
            && Objects.equals(node1.mainClass, node2.mainClass)
            && ListHelper.equalsNullToEmpty(node1.packages, node2.packages, String::equals)
            && ListHelper.equalsNullToEmpty(node1.requires, node2.requires, ModuleRequireNodeHelper::equals)
            && ListHelper.equalsNullToEmpty(node1.exports, node2.exports, ModuleExportNodeHelper::equals)
            && ListHelper.equalsNullToEmpty(node1.opens, node2.opens, ModuleOpenNodeHelper::equals)
            && ListHelper.equalsNullToEmpty(node1.uses, node2.uses, String::equals)
            && ListHelper.equalsNullToEmpty(node1.provides, node2.provides, ModuleProvideNodeHelper::equals);
  }

  public static int hashCode(ModuleNode node) {
    if (node == null) {
      return 0;
    }

    return new HashCodeBuilder()
            .append(node.name)
            .append(node.access)
            .append(node.version)
            .append(node.mainClass)
            .append(node.packages,
                    l -> ListHelper.hashCodeNullToEmpty(l, String::hashCode)
            )
            .append(node.requires,
                    l -> ListHelper.hashCodeNullToEmpty(l, ModuleRequireNodeHelper::hashCode)
            )
            .append(node.exports,
                    l -> ListHelper.hashCodeNullToEmpty(l, ModuleExportNodeHelper::hashCode)
            )
            .append(node.opens,
                    l -> ListHelper.hashCodeNullToEmpty(l, ModuleOpenNodeHelper::hashCode)
            )
            .append(node.uses,
                    l -> ListHelper.hashCodeNullToEmpty(l, String::hashCode)
            )
            .append(node.provides,
                    l -> ListHelper.hashCodeNullToEmpty(l, ModuleProvideNodeHelper::hashCode)
            )
            .build();
  }

  public static void write(ModuleNode node, DataOutputStream out) throws IOException {
    out.writeUTF(node.name);
    out.writeInt(node.access);
    DataStreamHelper.writeUTFNullable(out, node.version);
    DataStreamHelper.writeUTFNullable(out, node.mainClass);
    ListHelper.write(
            ListHelper.nullToEmpty(node.packages),
            out,
            (element, stream) -> stream.writeUTF(element)
    );
    ListHelper.write(
            ListHelper.nullToEmpty(node.requires),
            out,
            ModuleRequireNodeHelper::write
    );
    ListHelper.write(
            ListHelper.nullToEmpty(node.exports),
            out,
            ModuleExportNodeHelper::write
    );
    ListHelper.write(
            ListHelper.nullToEmpty(node.opens),
            out,
            ModuleOpenNodeHelper::write
    );
    ListHelper.write(
            ListHelper.nullToEmpty(node.uses),
            out,
            (element, stream) -> stream.writeUTF(element)
    );
    ListHelper.write(
            ListHelper.nullToEmpty(node.provides),
            out,
            ModuleProvideNodeHelper::write
    );
  }

  public static ModuleNode read(DataInputStream in) throws IOException {
    String name = in.readUTF();
    int access = in.readInt();
    String version = DataStreamHelper.readUTFNullable(in);
    ModuleNode node = new ModuleNode(
            name, access, version
    );
    node.mainClass = DataStreamHelper.readUTFNullable(in);
    node.packages = ListHelper.read(
            in,
            DataInput::readUTF
    );
    node.requires = ListHelper.read(
            in,
            ModuleRequireNodeHelper::read
    );
    node.exports = ListHelper.read(
            in,
            ModuleExportNodeHelper::read
    );
    node.opens = ListHelper.read(
            in,
            ModuleOpenNodeHelper::read
    );
    node.uses = ListHelper.read(
            in,
            DataInput::readUTF
    );
    node.provides = ListHelper.read(
            in,
            ModuleProvideNodeHelper::read
    );
    return node;
  }
}
