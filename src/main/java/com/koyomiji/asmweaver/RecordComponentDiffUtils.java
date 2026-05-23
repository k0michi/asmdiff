package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.CustomDataInput;
import com.koyomiji.asmweaver.io.CustomDataOutput;
import com.koyomiji.asmweaver.util.tuple.Pair;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.RecordComponentNode;
import org.objectweb.asm.tree.TypeAnnotationNode;

import java.io.DataInput;
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

  public static RecordComponentDiff invert(RecordComponentDiff diff) {
    RecordComponentDiff inverted = new RecordComponentDiff();
    inverted.name = ListDiffUtils.invert(diff.name);
    inverted.descriptor = ListDiffUtils.invert(diff.descriptor);
    inverted.signature = ListDiffUtils.invert(diff.signature);
    inverted.visibleAnnotations = ListDiffUtils.invert(
            diff.visibleAnnotations
    );
    inverted.invisibleAnnotations = ListDiffUtils.invert(
            diff.invisibleAnnotations
    );
    inverted.visibleTypeAnnotations = ListDiffUtils.invert(
            diff.visibleTypeAnnotations
    );
    inverted.invisibleTypeAnnotations = ListDiffUtils.invert(
            diff.invisibleTypeAnnotations
    );
    return inverted;
  }

  public static RecordComponentDiff compose(RecordComponentDiff diff1, RecordComponentDiff diff2) {
    RecordComponentDiff composed = new RecordComponentDiff();
    composed.name = ListDiffUtils.compose(diff1.name, diff2.name, String::equals);
    composed.descriptor = ListDiffUtils.compose(diff1.descriptor, diff2.descriptor, String::equals);
    composed.signature = ListDiffUtils.compose(diff1.signature, diff2.signature, String::equals);
    composed.visibleAnnotations = ListDiffUtils.compose(
            diff1.visibleAnnotations,
            diff2.visibleAnnotations,
            AnnotationNodeHelper::equals
    );
    composed.invisibleAnnotations = ListDiffUtils.compose(
            diff1.invisibleAnnotations,
            diff2.invisibleAnnotations,
            AnnotationNodeHelper::equals
    );
    composed.visibleTypeAnnotations = ListDiffUtils.compose(
            diff1.visibleTypeAnnotations,
            diff2.visibleTypeAnnotations,
            AnnotationNodeHelper::equals
    );
    composed.invisibleTypeAnnotations = ListDiffUtils.compose(
            diff1.invisibleTypeAnnotations,
            diff2.invisibleTypeAnnotations,
            AnnotationNodeHelper::equals
    );
    return composed;
  }

  public static Pair<RecordComponentDiff, RecordComponentDiff> commute(RecordComponentDiff diff1, RecordComponentDiff diff2) throws ConflictException {
    RecordComponentDiff diff2Prime = new RecordComponentDiff();
    RecordComponentDiff diff1Prime = new RecordComponentDiff();

    Pair<ListDiff<String>, ListDiff<String>> name = ListDiffUtils.commute(diff1.name, diff2.name, String::equals);
    diff2Prime.name = name.first;
    diff1Prime.name = name.second;

    Pair<ListDiff<String>, ListDiff<String>> descriptor = ListDiffUtils.commute(diff1.descriptor, diff2.descriptor, String::equals);
    diff2Prime.descriptor = descriptor.first;
    diff1Prime.descriptor = descriptor.second;

    Pair<ListDiff<String>, ListDiff<String>> signature = ListDiffUtils.commute(diff1.signature, diff2.signature, String::equals);
    diff2Prime.signature = signature.first;
    diff1Prime.signature = signature.second;

    Pair<ListDiff<AnnotationNode>, ListDiff<AnnotationNode>> visibleAnnotations = ListDiffUtils.commute(
            diff1.visibleAnnotations,
            diff2.visibleAnnotations,
            AnnotationNodeHelper::equals
    );
    diff2Prime.visibleAnnotations = visibleAnnotations.first;
    diff1Prime.visibleAnnotations = visibleAnnotations.second;

    Pair<ListDiff<AnnotationNode>, ListDiff<AnnotationNode>> invisibleAnnotations = ListDiffUtils.commute(
            diff1.invisibleAnnotations,
            diff2.invisibleAnnotations,
            AnnotationNodeHelper::equals
    );
    diff2Prime.invisibleAnnotations = invisibleAnnotations.first;
    diff1Prime.invisibleAnnotations = invisibleAnnotations.second;

    Pair<ListDiff<TypeAnnotationNode>, ListDiff<TypeAnnotationNode>> visibleTypeAnnotations = ListDiffUtils.commute(
            diff1.visibleTypeAnnotations,
            diff2.visibleTypeAnnotations,
            AnnotationNodeHelper::equals
    );
    diff2Prime.visibleTypeAnnotations = visibleTypeAnnotations.first;
    diff1Prime.visibleTypeAnnotations = visibleTypeAnnotations.second;

    Pair<ListDiff<TypeAnnotationNode>, ListDiff<TypeAnnotationNode>> invisibleTypeAnnotations = ListDiffUtils.commute(
            diff1.invisibleTypeAnnotations,
            diff2.invisibleTypeAnnotations,
            AnnotationNodeHelper::equals
    );
    diff2Prime.invisibleTypeAnnotations = invisibleTypeAnnotations.first;
    diff1Prime.invisibleTypeAnnotations = invisibleTypeAnnotations.second;

    return Pair.of(diff2Prime, diff1Prime);
  }

  public static void write(RecordComponentDiff diff, CustomDataOutput out) throws IOException {
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
