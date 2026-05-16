package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.DataStreamHelper;
import org.objectweb.asm.tree.FieldNode;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FieldDiffUtils {
  public static FieldDiff diff(FieldNode node1, FieldNode node2) {
    FieldDiff diff = new FieldDiff();
//    diff.access = ListDiffUtils.diff(ListHelper.ofNullable(node1.access), ListHelper.ofNullable(node2.access), Integer::equals);
//    diff.name = ListDiffUtils.diff(ListHelper.ofNullable(node1.name), ListHelper.ofNullable(node2.name), String::equals);
//    diff.desc = ListDiffUtils.diff(ListHelper.ofNullable(node1.desc), ListHelper.ofNullable(node2.desc), String::equals);
//    diff.signature = ListDiffUtils.diff(ListHelper.ofNullable(node1.signature), ListHelper.ofNullable(node2.signature), String::equals);
//    diff.value = ListDiffUtils.diff(ListHelper.ofNullable(node1.value), ListHelper.ofNullable(node2.value), Object::equals);
    diff.access = ListDiffUtils.diff(
            ListHelper.ofNonNullable(node1.access),
            ListHelper.ofNonNullable(node2.access),
            Integer::equals
    );
    diff.name = ListDiffUtils.diff(
            ListHelper.ofNonNullable(node1.name),
            ListHelper.ofNonNullable(node2.name),
            String::equals
    );
    diff.desc = ListDiffUtils.diff(
            ListHelper.ofNonNullable(node1.desc),
            ListHelper.ofNonNullable(node2.desc),
            String::equals
    );
    diff.signature = ListDiffUtils.diff(
            ListHelper.ofNullable(node1.signature),
            ListHelper.ofNullable(node2.signature),
            String::equals
    );
    diff.value = ListDiffUtils.diff(
            ListHelper.ofNullable(node1.value),
            ListHelper.ofNullable(node2.value),
            Object::equals
    );
    diff.visibleAnnotations = ListDiffUtils.diff(
            ListHelper.nullToEmpty(node1.visibleAnnotations),
            ListHelper.nullToEmpty(node2.visibleAnnotations),
            AnnotationNodeHelper::equals
    );
    diff.invisibleAnnotations = ListDiffUtils.diff(
            ListHelper.nullToEmpty(node1.invisibleAnnotations),
            ListHelper.nullToEmpty(node2.invisibleAnnotations),
            AnnotationNodeHelper::equals
    );
    diff.visibleTypeAnnotations = ListDiffUtils.diff(
            ListHelper.nullToEmpty(node1.visibleTypeAnnotations),
            ListHelper.nullToEmpty(node2.visibleTypeAnnotations),
            AnnotationNodeHelper::equals
    );
    diff.invisibleTypeAnnotations = ListDiffUtils.diff(
            ListHelper.nullToEmpty(node1.invisibleTypeAnnotations),
            ListHelper.nullToEmpty(node2.invisibleTypeAnnotations),
            AnnotationNodeHelper::equals
    );
    // attributes
    return diff;
  }

  public static FieldNode patch(FieldNode node, FieldDiff diff) {
    int access = ListDiffUtils.patchNonNullableValue(node.access, diff.access);
    String name = ListDiffUtils.patchNonNullableValue(node.name, diff.name);
    String desc = ListDiffUtils.patchNonNullableValue(node.desc, diff.desc);
    String signature = ListDiffUtils.patchNullableValue(node.signature, diff.signature);
    Object value = ListDiffUtils.patchNullableValue(node.value, diff.value);
    FieldNode patchedNode = new FieldNode(access, name, desc, signature, value);
    patchedNode.visibleAnnotations = ListDiffUtils.patch(node.visibleAnnotations, diff.visibleAnnotations);
    patchedNode.invisibleAnnotations = ListDiffUtils.patch(node.invisibleAnnotations, diff.invisibleAnnotations);
    patchedNode.visibleTypeAnnotations = ListDiffUtils.patch(node.visibleTypeAnnotations, diff.visibleTypeAnnotations);
    patchedNode.invisibleTypeAnnotations = ListDiffUtils.patch(node.invisibleTypeAnnotations, diff.invisibleTypeAnnotations);
    // attributes
    return patchedNode;
  }

  public static void write(FieldDiff diff, DataOutputStream out) throws IOException {
    ListDiffUtils.write(diff.access, out, (d, out2) -> out2.writeInt(d));
    ListDiffUtils.write(diff.name, out, (d, out2) -> out2.writeUTF(d));
    ListDiffUtils.write(diff.desc, out, (d, out2) -> out2.writeUTF(d));
    ListDiffUtils.write(diff.signature, out, (d, out2) -> out2.writeUTF(d));
    ListDiffUtils.write(diff.value, out, ConstantHelper::write);
    ListDiffUtils.write(diff.visibleAnnotations, out, AnnotationNodeHelper::write);
    ListDiffUtils.write(diff.invisibleAnnotations, out, AnnotationNodeHelper::write);
    ListDiffUtils.write(diff.visibleTypeAnnotations, out, AnnotationNodeHelper::write);
    ListDiffUtils.write(diff.invisibleTypeAnnotations, out, AnnotationNodeHelper::write);
  }

  public static FieldDiff read(DataInputStream in) throws IOException {
    FieldDiff diff = new FieldDiff();
    diff.access = ListDiffUtils.read(in, DataInput::readInt);
    diff.name = ListDiffUtils.read(in, DataInput::readUTF);
    diff.desc = ListDiffUtils.read(in, DataInput::readUTF);
    diff.signature = ListDiffUtils.read(in, DataInput::readUTF);
    diff.value = ListDiffUtils.read(in, ConstantHelper::read);
    diff.visibleAnnotations = ListDiffUtils.read(in, AnnotationNodeHelper::readAnnotationNode);
    diff.invisibleAnnotations = ListDiffUtils.read(in, AnnotationNodeHelper::readAnnotationNode);
    diff.visibleTypeAnnotations = ListDiffUtils.read(
            in,
            AnnotationNodeHelper::readTypeAnnotationNode
    );
    diff.invisibleTypeAnnotations = ListDiffUtils.read(
            in,
            AnnotationNodeHelper::readTypeAnnotationNode
    );
    return diff;
  }
}
