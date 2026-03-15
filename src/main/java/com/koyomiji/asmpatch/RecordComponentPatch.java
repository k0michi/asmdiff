package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.TypeAnnotationNode;

public class RecordComponentPatch {
  public ValuePatch<String> name;
  public ValuePatch<String> descriptor;
  public ValuePatch<String> signature;
  public ListPatch<AnnotationNode, AnnotationPatch> visibleAnnotations;
  public ListPatch<AnnotationNode, AnnotationPatch> invisibleAnnotations;
  public ListPatch<TypeAnnotationNode, TypeAnnotationPatch> visibleTypeAnnotations;
  public ListPatch<TypeAnnotationNode, TypeAnnotationPatch> invisibleTypeAnnotations;
  // TODO: attrs
}
