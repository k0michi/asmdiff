package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.TypeAnnotationNode;

public class RecordComponentDiff implements IDiff {
  public ListDiff<String> name;
  public ListDiff<String> descriptor;
  public ListDiff<String> signature;
  public ListDiff<AnnotationNode> visibleAnnotations;
  public ListDiff<AnnotationNode> invisibleAnnotations;
  public ListDiff<TypeAnnotationNode> visibleTypeAnnotations;
  public ListDiff<TypeAnnotationNode> invisibleTypeAnnotations;
  // attrs

  @Override
  public boolean isEmpty() {
    return name.isEmpty()
            && descriptor.isEmpty()
            && signature.isEmpty()
            && visibleAnnotations.isEmpty()
            && invisibleAnnotations.isEmpty()
            && visibleTypeAnnotations.isEmpty()
            && invisibleTypeAnnotations.isEmpty();
  }

  @Override
  public int distance() {
    return name.distance()
            + descriptor.distance()
            + signature.distance()
            + visibleAnnotations.distance()
            + invisibleAnnotations.distance()
            + visibleTypeAnnotations.distance()
            + invisibleTypeAnnotations.distance();
  }
}
