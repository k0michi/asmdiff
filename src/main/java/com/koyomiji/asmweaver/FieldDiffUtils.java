package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldNode;

public class FieldDiffUtils {
  public static FieldDiff diff(FieldNode node1, FieldNode node2) {
    FieldDiff diff = new FieldDiff();
//    diff.access = ListDiffUtils.diff(ListHelper.ofNullable(node1.access), ListHelper.ofNullable(node2.access), Integer::equals);
//    diff.name = ListDiffUtils.diff(ListHelper.ofNullable(node1.name), ListHelper.ofNullable(node2.name), String::equals);
//    diff.desc = ListDiffUtils.diff(ListHelper.ofNullable(node1.desc), ListHelper.ofNullable(node2.desc), String::equals);
//    diff.signature = ListDiffUtils.diff(ListHelper.ofNullable(node1.signature), ListHelper.ofNullable(node2.signature), String::equals);
//    diff.value = ListDiffUtils.diff(ListHelper.ofNullable(node1.value), ListHelper.ofNullable(node2.value), Object::equals);
    diff.access =ListDiffUtils.diff(
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
            ListHelper.orEmpty(node1.visibleAnnotations),
            ListHelper.orEmpty(node2.visibleAnnotations),
            AnnotationNodeHelper::equals
    );
    diff.invisibleAnnotations = ListDiffUtils.diff(
            ListHelper.orEmpty(node1.invisibleAnnotations),
            ListHelper.orEmpty(node2.invisibleAnnotations),
            AnnotationNodeHelper::equals
    );
    diff.visibleTypeAnnotations = ListDiffUtils.diff(
            ListHelper.orEmpty(node1.visibleTypeAnnotations),
            ListHelper.orEmpty(node2.visibleTypeAnnotations),
            AnnotationNodeHelper::equals
    );
    diff.invisibleTypeAnnotations = ListDiffUtils.diff(
            ListHelper.orEmpty(node1.invisibleTypeAnnotations),
            ListHelper.orEmpty(node2.invisibleTypeAnnotations),
            AnnotationNodeHelper::equals
    );
    // attributes
    return diff;
  }

  public static FieldNode patch(FieldNode node, FieldDiff diff) {
    int access = ListDiffUtils.patchNonNullable(node.access, diff.access);
    String name = ListDiffUtils.patchNonNullable(node.name, diff.name);
    String desc = ListDiffUtils.patchNonNullable(node.desc, diff.desc);
    String signature = ListDiffUtils.patchNullable(node.signature, diff.signature);
    Object value = ListDiffUtils.patchNullable(node.value, diff.value);
    FieldNode patchedNode = new FieldNode(access, name, desc, signature, value);
    patchedNode.visibleAnnotations = ListDiffUtils.patch(node.visibleAnnotations, diff.visibleAnnotations);
    patchedNode.invisibleAnnotations = ListDiffUtils.patch(node.invisibleAnnotations, diff.invisibleAnnotations);
    patchedNode.visibleTypeAnnotations = ListDiffUtils.patch(node.visibleTypeAnnotations, diff.visibleTypeAnnotations);
    patchedNode.invisibleTypeAnnotations = ListDiffUtils.patch(node.invisibleTypeAnnotations, diff.invisibleTypeAnnotations);
    // attributes
    return patchedNode;
  }
}
