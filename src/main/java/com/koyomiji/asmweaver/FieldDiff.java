package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.TypeAnnotationNode;

public class FieldDiff implements IDiff {
  public ListDiff<Integer> access;
  public ListDiff<String> name;
  public ListDiff<String> desc;
  public ListDiff<String> signature;
  public ListDiff<Object> value;
  public ListDiff<AnnotationNode> visibleAnnotations;
  public ListDiff<AnnotationNode> invisibleAnnotations;
  public ListDiff<TypeAnnotationNode> visibleTypeAnnotations;
  public ListDiff<TypeAnnotationNode> invisibleTypeAnnotations;
  // attributes

  @Override
  public boolean isEmpty() {
    return access.isEmpty()
            && name.isEmpty()
            && desc.isEmpty()
            && signature.isEmpty()
            && value.isEmpty()
            && visibleAnnotations.isEmpty()
            && invisibleAnnotations.isEmpty()
            && visibleTypeAnnotations.isEmpty()
            && invisibleTypeAnnotations.isEmpty();
  }
}
