package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldNode;

public class FieldDiffUtils {
  public static FieldDiff diff(FieldNode node1, FieldNode node2) {
    FieldDiff diff = new FieldDiff();
    diff.access = ListDiffUtils.diff(ListHelper.ofNullable(node1.access), ListHelper.ofNullable(node2.access), Integer::equals);
    diff.name = ListDiffUtils.diff(ListHelper.ofNullable(node1.name), ListHelper.ofNullable(node2.name), String::equals);
    diff.desc = ListDiffUtils.diff(ListHelper.ofNullable(node1.desc), ListHelper.ofNullable(node2.desc), String::equals);
    diff.signature = ListDiffUtils.diff(ListHelper.ofNullable(node1.signature), ListHelper.ofNullable(node2.signature), String::equals);
    diff.value = ListDiffUtils.diff(ListHelper.ofNullable(node1.value), ListHelper.ofNullable(node2.value), Object::equals);
    diff.visibleAnnotations = ListDiffUtils.diff(node1.visibleAnnotations, node2.visibleAnnotations, AnnotationNodeHelper::equals);
    diff.invisibleAnnotations = ListDiffUtils.diff(node1.invisibleAnnotations, node2.invisibleAnnotations, AnnotationNodeHelper::equals);
    diff.visibleTypeAnnotations = ListDiffUtils.diff(node1.visibleTypeAnnotations, node2.visibleTypeAnnotations, AnnotationNodeHelper::equals);
    diff.invisibleTypeAnnotations = ListDiffUtils.diff(node1.invisibleTypeAnnotations, node2.invisibleTypeAnnotations, AnnotationNodeHelper::equals);
    // attributes
    return diff;
  }
}
