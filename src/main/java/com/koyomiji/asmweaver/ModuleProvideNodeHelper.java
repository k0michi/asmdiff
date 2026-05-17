package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.CustomDataInput;
import com.koyomiji.asmweaver.io.CustomDataOutput;
import com.koyomiji.asmweaver.util.HashCodeBuilder;
import org.objectweb.asm.tree.ModuleProvideNode;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ModuleProvideNodeHelper {
  public static boolean equals(ModuleProvideNode node1, ModuleProvideNode node2) {
    if (node1 == node2) {
      return true;
    }

    if (node1 == null || node2 == null) {
      return false;
    }

    if (node1.getClass() != node2.getClass()) {
      return false;
    }

    return Objects.equals(node1.service, node2.service)
            && ListHelper.equalsNullToEmpty(node1.providers, node2.providers);
  }

  public static int hashCode(ModuleProvideNode node) {
    if (node == null) {
      return 0;
    }

    return new HashCodeBuilder()
            .append(node.service)
            .append(node.providers,
                    (l) -> ListHelper.hashCodeNullToEmpty(l, String::hashCode)
            ).build();
  }

  public static void write(ModuleProvideNode node, CustomDataOutput out) throws IOException {
    out.writeUTF(node.service);
    ListHelper.write(
            // TODO: null check not necessary?
            ListHelper.nullToEmpty(node.providers),
            out,
            (element, stream) -> {
              stream.writeUTF(element);
            }
    );
  }

  public static ModuleProvideNode read(CustomDataInput in) throws IOException {
    String service = in.readUTF();
    List<String> providers = ListHelper.read(
            in,
            DataInput::readUTF
    );
    return new ModuleProvideNode(service, providers);
  }
}
