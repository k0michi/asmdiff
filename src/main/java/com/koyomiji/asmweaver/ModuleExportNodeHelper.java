package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.CustomDataInput;
import com.koyomiji.asmweaver.io.CustomDataOutput;
import com.koyomiji.asmweaver.util.HashCodeBuilder;
import org.objectweb.asm.tree.ModuleExportNode;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ModuleExportNodeHelper {
  public static boolean equals(ModuleExportNode node1, ModuleExportNode node2) {
    if (node1 == node2) {
      return true;
    }

    if (node1 == null || node2 == null) {
      return false;
    }

    if (node1.getClass() != node2.getClass()) {
      return false;
    }

    return Objects.equals(node1.packaze, node2.packaze)
            && node1.access == node2.access
            && ListHelper.equalsNullToEmpty(node1.modules, node2.modules, String::equals);
  }

  public static int hashCode(ModuleExportNode node) {
    if (node == null) {
      return 0;
    }

    return new HashCodeBuilder()
            .append(node.packaze)
            .append(node.access)
            .append(node.modules,
                    (l) -> ListHelper.hashCodeNullToEmpty(l, String::hashCode)
            ).build();
  }

  public static void write(ModuleExportNode node, CustomDataOutput out) throws IOException {
    out.writeUTF(node.packaze);
    out.writeInt(node.access);
    ListHelper.write(
            ListHelper.nullToEmpty(node.modules),
            out,
            (element, stream) -> {
              stream.writeUTF(element);
            }
    );
  }

  public static ModuleExportNode read(CustomDataInput in) throws IOException {
    String packaze = in.readUTF();
    int access = in.readInt();
    List<String> modules = ListHelper.read(
            in,
            DataInput::readUTF
    );
    return new ModuleExportNode(packaze, access, modules);
  }
}
