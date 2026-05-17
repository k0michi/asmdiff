package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.CustomDataInput;
import com.koyomiji.asmweaver.io.DataStreamHelper;
import com.koyomiji.asmweaver.util.HashCodeBuilder;
import org.objectweb.asm.tree.FieldNode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

public class FieldNodeHelper {
  public static boolean equals(FieldNode node1, FieldNode node2) {
    if (node1 == node2) {
      return true;
    }

    if (node1 == null || node2 == null) {
      return false;
    }

    if (node1.getClass() != node2.getClass()) {
      return false;
    }

    return node1.access == node2.access
            && Objects.equals(node1.name, node2.name)
            && Objects.equals(node1.desc, node2.desc)
            && Objects.equals(node1.signature, node2.signature)
            && Objects.equals(node1.value, node2.value)
            && ListHelper.equalsNullToEmpty(node1.visibleAnnotations, node2.visibleAnnotations, AnnotationNodeHelper::equals)
            && ListHelper.equalsNullToEmpty(node1.invisibleAnnotations, node2.invisibleAnnotations, AnnotationNodeHelper::equals)
            && ListHelper.equalsNullToEmpty(node1.visibleTypeAnnotations, node2.visibleTypeAnnotations, AnnotationNodeHelper::equals)
            && ListHelper.equalsNullToEmpty(node1.invisibleTypeAnnotations, node2.invisibleTypeAnnotations, AnnotationNodeHelper::equals)
            && ListHelper.equalsNullToEmpty(node1.attrs, node2.attrs, Objects::equals);
  }

  public static int hashCode(FieldNode node) {
    if (node == null) {
      return 0;
    }

    return new HashCodeBuilder()
            .append(node.access)
            .append(node.name)
            .append(node.desc)
            .append(node.signature)
            .append(node.value)
            .append(node.visibleAnnotations,
                    (l) -> ListHelper.hashCodeNullToEmpty(l, AnnotationNodeHelper::hashCode)
            )
            .append(node.invisibleAnnotations,
                    (l) -> ListHelper.hashCodeNullToEmpty(l, AnnotationNodeHelper::hashCode)
            )
            .append(node.visibleTypeAnnotations,
                    (l) -> ListHelper.hashCodeNullToEmpty(l, AnnotationNodeHelper::hashCode)
            )
            .append(node.invisibleTypeAnnotations,
                    (l) -> ListHelper.hashCodeNullToEmpty(l, AnnotationNodeHelper::hashCode)
            )
            .append(node.attrs,
                    (l) -> ListHelper.hashCodeNullToEmpty(l, Objects::hashCode)
            ).build();
  }

  public static void write(FieldNode node, DataOutputStream out) throws IOException {
    out.writeInt(node.access);
    out.writeUTF(node.name);
    out.writeUTF(node.desc);
    DataStreamHelper.writeUTFNullable(out, node.signature);
    NullableHelper.write(node.value, out, ConstantHelper::write);
    ListHelper.write(
            ListHelper.nullToEmpty(node.visibleAnnotations),
            out,
            AnnotationNodeHelper::write
    );
    ListHelper.write(
            ListHelper.nullToEmpty(node.invisibleAnnotations),
            out,
            AnnotationNodeHelper::write
    );
    ListHelper.write(
            ListHelper.nullToEmpty(node.visibleTypeAnnotations),
            out,
            AnnotationNodeHelper::write
    );
    ListHelper.write(
            ListHelper.nullToEmpty(node.invisibleTypeAnnotations),
            out,
            AnnotationNodeHelper::write
    );
    // TODO: attrs
  }

  public static FieldNode read(CustomDataInput in) throws IOException {
    int access = in.readInt();
    String name = in.readUTF();
    String desc = in.readUTF();
    String signature = DataStreamHelper.readUTFNullable(in);
    Object value = NullableHelper.read(in, ConstantHelper::read);
    FieldNode node = new FieldNode(
            access, name, desc, signature, value
    );
    node.visibleAnnotations = ListHelper.read(
            in,
            AnnotationNodeHelper::readAnnotationNode
    );
    node.invisibleAnnotations = ListHelper.read(
            in,
            AnnotationNodeHelper::readAnnotationNode
    );
    node.visibleTypeAnnotations = ListHelper.read(
            in,
            AnnotationNodeHelper::readTypeAnnotationNode
    );
    node.invisibleTypeAnnotations = ListHelper.read(
            in,
            AnnotationNodeHelper::readTypeAnnotationNode
    );

    return node;
  }
}
