package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.CustomDataInput;
import org.objectweb.asm.tree.RecordComponentNode;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RecordComponentDiffUtils {
  public static RecordComponentDiff diff(RecordComponentNode node1, RecordComponentNode node2) {
    RecordComponentDiff diff = new RecordComponentDiff();
    diff.name = ListDiffUtils.diffNonNullableValue(node1.name, node2.name, String::equals);
    diff.descriptor = ListDiffUtils.diffNonNullableValue(node1.descriptor, node2.descriptor, String::equals);
    diff.signature = ListDiffUtils.diffNullableValue(node1.signature, node2.signature, String::equals);
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
    // attrs
    return diff;
  }

  public static RecordComponentNode patch(RecordComponentNode node, RecordComponentDiff diff) {
    String name = ListDiffUtils.patchNonNullableValue(node.name, diff.name);
    String descriptor = ListDiffUtils.patchNonNullableValue(node.descriptor, diff.descriptor);
    String signature = ListDiffUtils.patchNullableValue(node.signature, diff.signature);
    RecordComponentNode patchedNode = new RecordComponentNode(name, descriptor, signature);
    patchedNode.visibleAnnotations = ListDiffUtils.patch(
            ListHelper.nullToEmpty(node.visibleAnnotations),
            diff.visibleAnnotations
    );
    patchedNode.invisibleAnnotations = ListDiffUtils.patch(
            ListHelper.nullToEmpty(node.invisibleAnnotations),
            diff.invisibleAnnotations
    );
    patchedNode.visibleTypeAnnotations = ListDiffUtils.patch(
            ListHelper.nullToEmpty(node.visibleTypeAnnotations),
            diff.visibleTypeAnnotations
    );
    patchedNode.invisibleTypeAnnotations = ListDiffUtils.patch(
            ListHelper.nullToEmpty(node.invisibleTypeAnnotations),
            diff.invisibleTypeAnnotations
    );
    // attrs
    return patchedNode;
  }

  public static void write(RecordComponentDiff diff, DataOutputStream out) throws IOException {
    ListDiffUtils.write(diff.name, out, (e, s) -> s.writeUTF(e));
    ListDiffUtils.write(diff.descriptor, out, (e, s) -> s.writeUTF(e));
    ListDiffUtils.write(diff.signature, out, (e, s) -> s.writeUTF(e));
    ListDiffUtils.write(diff.visibleAnnotations, out, AnnotationNodeHelper::write);
    ListDiffUtils.write(diff.invisibleAnnotations, out, AnnotationNodeHelper::write);
    ListDiffUtils.write(diff.visibleTypeAnnotations, out, AnnotationNodeHelper::write);
    ListDiffUtils.write(diff.invisibleTypeAnnotations, out, AnnotationNodeHelper::write);
  }

  public static RecordComponentDiff read(CustomDataInput in) throws IOException {
    RecordComponentDiff diff = new RecordComponentDiff();
    diff.name = ListDiffUtils.read(in, DataInput::readUTF);
    diff.descriptor = ListDiffUtils.read(in, DataInput::readUTF);
    diff.signature = ListDiffUtils.read(in, DataInput::readUTF);
    diff.visibleAnnotations = ListDiffUtils.read(in, AnnotationNodeHelper::readAnnotationNode);
    diff.invisibleAnnotations = ListDiffUtils.read(in, AnnotationNodeHelper::readAnnotationNode);
    diff.visibleTypeAnnotations = ListDiffUtils.read(in, AnnotationNodeHelper::readTypeAnnotationNode);
    diff.invisibleTypeAnnotations = ListDiffUtils.read(in, AnnotationNodeHelper::readTypeAnnotationNode);
    return diff;
  }
}
