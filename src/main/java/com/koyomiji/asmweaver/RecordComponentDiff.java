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
}
