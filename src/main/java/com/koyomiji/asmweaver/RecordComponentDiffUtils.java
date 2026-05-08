package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.RecordComponentNode;

public class RecordComponentDiffUtils {
  public static RecordComponentDiff diff(RecordComponentNode node1, RecordComponentNode node2) {
    RecordComponentDiff diff = new RecordComponentDiff();
    diff.name = ListDiffUtils.diff(ListHelper.ofNullable(node1.name), ListHelper.ofNullable(node2.name), String::equals);
    diff.descriptor = ListDiffUtils.diff(ListHelper.ofNullable(node1.descriptor), ListHelper.ofNullable(node2.descriptor), String::equals);
    diff.signature = ListDiffUtils.diff(ListHelper.ofNullable(node1.signature), ListHelper.ofNullable(node2.signature), String::equals);
    diff.visibleAnnotations = ListDiffUtils.diff(node1.visibleAnnotations, node2.visibleAnnotations, AnnotationNodeHelper::equals);
    diff.invisibleAnnotations = ListDiffUtils.diff(node1.invisibleAnnotations, node2.invisibleAnnotations, AnnotationNodeHelper::equals);
    diff.visibleTypeAnnotations = ListDiffUtils.diff(node1.visibleTypeAnnotations, node2.visibleTypeAnnotations, TypeAnnotationNodeHelper::equals);
    diff.invisibleTypeAnnotations = ListDiffUtils.diff(node1.invisibleTypeAnnotations, node2.invisibleTypeAnnotations, TypeAnnotationNodeHelper::equals);
    // attrs
    return diff;
  }
}
