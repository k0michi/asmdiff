package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.CustomDataInput;
import com.koyomiji.asmweaver.io.CustomDataOutput;
import com.koyomiji.asmweaver.util.tuple.Pair;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.TypeAnnotationNode;

import java.io.DataInput;
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

    if (diff.access == null
            && diff.name == null
            && diff.desc == null
            && diff.signature == null
            && diff.value == null
            && diff.visibleAnnotations == null
            && diff.invisibleAnnotations == null
            && diff.visibleTypeAnnotations == null
            && diff.invisibleTypeAnnotations == null) {
      return null;
    }

    return diff;
  }

  public static FieldNode patch(FieldNode node, FieldDiff diff) {
    if (diff == null) {
      return node;
    }

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

  public static FieldDiff invert(FieldDiff diff) {
    if (diff == null) {
      return null;
    }

    FieldDiff inverted = new FieldDiff();
    inverted.access = ListDiffUtils.invert(diff.access);
    inverted.name = ListDiffUtils.invert(diff.name);
    inverted.desc = ListDiffUtils.invert(diff.desc);
    inverted.signature = ListDiffUtils.invert(diff.signature);
    inverted.value = ListDiffUtils.invert(diff.value);
    inverted.visibleAnnotations = ListDiffUtils.invert(diff.visibleAnnotations);
    inverted.invisibleAnnotations = ListDiffUtils.invert(diff.invisibleAnnotations);
    inverted.visibleTypeAnnotations = ListDiffUtils.invert(diff.visibleTypeAnnotations);
    inverted.invisibleTypeAnnotations = ListDiffUtils.invert(diff.invisibleTypeAnnotations);
    return inverted;
  }

  public static FieldDiff compose(FieldDiff diff1, FieldDiff diff2) {
    if (diff1 == null) {
      return diff2;
    }

    if (diff2 == null) {
      return diff1;
    }

    FieldDiff composed = new FieldDiff();
    composed.access = ListDiffUtils.compose(diff1.access, diff2.access, Integer::equals);
    composed.name = ListDiffUtils.compose(diff1.name, diff2.name, String::equals);
    composed.desc = ListDiffUtils.compose(diff1.desc, diff2.desc, String::equals);
    composed.signature = ListDiffUtils.compose(diff1.signature, diff2.signature, String::equals);
    composed.value = ListDiffUtils.compose(diff1.value, diff2.value, Object::equals);
    composed.visibleAnnotations = ListDiffUtils.compose(diff1.visibleAnnotations, diff2.visibleAnnotations, AnnotationNodeHelper::equals);
    composed.invisibleAnnotations = ListDiffUtils.compose(diff1.invisibleAnnotations, diff2.invisibleAnnotations, AnnotationNodeHelper::equals);
    composed.visibleTypeAnnotations = ListDiffUtils.compose(diff1.visibleTypeAnnotations, diff2.visibleTypeAnnotations, AnnotationNodeHelper::equals);
    composed.invisibleTypeAnnotations = ListDiffUtils.compose(diff1.invisibleTypeAnnotations, diff2.invisibleTypeAnnotations, AnnotationNodeHelper::equals);
    return composed;
  }

  public static Pair<FieldDiff, FieldDiff> commute(FieldDiff diff1, FieldDiff diff2) throws ConflictException {
    if (diff1 == null || diff2 == null) {
      return Pair.of(diff2, diff1);
    }

    FieldDiff diff2Prime = new FieldDiff();
    FieldDiff diff1Prime = new FieldDiff();

    Pair<ListDiff<Integer>, ListDiff<Integer>> access = ListDiffUtils.commute(diff1.access, diff2.access, Integer::equals);
    diff2Prime.access = access.first;
    diff1Prime.access = access.second;

    Pair<ListDiff<String>, ListDiff<String>> name = ListDiffUtils.commute(diff1.name, diff2.name, String::equals);
    diff2Prime.name = name.first;
    diff1Prime.name = name.second;

    Pair<ListDiff<String>, ListDiff<String>> desc = ListDiffUtils.commute(diff1.desc, diff2.desc, String::equals);
    diff2Prime.desc = desc.first;
    diff1Prime.desc = desc.second;

    Pair<ListDiff<String>, ListDiff<String>> signature = ListDiffUtils.commute(diff1.signature, diff2.signature, String::equals);
    diff2Prime.signature = signature.first;
    diff1Prime.signature = signature.second;

    Pair<ListDiff<Object>, ListDiff<Object>> value = ListDiffUtils.commute(diff1.value, diff2.value, Object::equals);
    diff2Prime.value = value.first;
    diff1Prime.value = value.second;

    Pair<ListDiff<AnnotationNode>, ListDiff<AnnotationNode>> visibleAnnotations = ListDiffUtils.commute(diff1.visibleAnnotations, diff2.visibleAnnotations, AnnotationNodeHelper::equals);
    diff2Prime.visibleAnnotations = visibleAnnotations.first;
    diff1Prime.visibleAnnotations = visibleAnnotations.second;

    Pair<ListDiff<AnnotationNode>, ListDiff<AnnotationNode>> invisibleAnnotations = ListDiffUtils.commute(diff1.invisibleAnnotations, diff2.invisibleAnnotations, AnnotationNodeHelper::equals);
    diff2Prime.invisibleAnnotations = invisibleAnnotations.first;
    diff1Prime.invisibleAnnotations = invisibleAnnotations.second;

    Pair<ListDiff<TypeAnnotationNode>, ListDiff<TypeAnnotationNode>> visibleTypeAnnotations = ListDiffUtils.commute(diff1.visibleTypeAnnotations, diff2.visibleTypeAnnotations, AnnotationNodeHelper::equals);
    diff2Prime.visibleTypeAnnotations = visibleTypeAnnotations.first;
    diff1Prime.visibleTypeAnnotations = visibleTypeAnnotations.second;

    Pair<ListDiff<TypeAnnotationNode>, ListDiff<TypeAnnotationNode>> invisibleTypeAnnotations = ListDiffUtils.commute(diff1.invisibleTypeAnnotations, diff2.invisibleTypeAnnotations, AnnotationNodeHelper::equals);
    diff2Prime.invisibleTypeAnnotations = invisibleTypeAnnotations.first;
    diff1Prime.invisibleTypeAnnotations = invisibleTypeAnnotations.second;

    return Pair.of(diff2Prime, diff1Prime);
  }

  public static void write(FieldDiff diff, CustomDataOutput out) throws IOException {
    out.writeBoolean(diff == null);

    if (diff == null) {
      return;
    }

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

  public static FieldDiff read(CustomDataInput in) throws IOException {
    if (in.readBoolean()) {
      return null;
    }

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

  public static int distance(FieldDiff diff) {
    if (diff == null) {
      return 0;
    }

    return ListDiffUtils.distance(diff.access)
            + ListDiffUtils.distance(diff.name)
            + ListDiffUtils.distance(diff.desc)
            + ListDiffUtils.distance(diff.signature)
            + ListDiffUtils.distance(diff.value)
            + ListDiffUtils.distance(diff.visibleAnnotations)
            + ListDiffUtils.distance(diff.invisibleAnnotations)
            + ListDiffUtils.distance(diff.visibleTypeAnnotations)
            + ListDiffUtils.distance(diff.invisibleTypeAnnotations);
  }
}
