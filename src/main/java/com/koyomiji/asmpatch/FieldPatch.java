package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.TypeAnnotationNode;

public class FieldPatch {
  public ValuePatch<Integer> access;
  public ValuePatch<String> name;
  public ValuePatch<String> desc;
  public ValuePatch<String> signature;
  public ValuePatch<Object> value;
  public ListPatch<AnnotationNode, AnnotationPatch> invisibleAnnotations;
  public ListPatch<AnnotationNode, AnnotationPatch> visibleAnnotations;
  public ListPatch<TypeAnnotationNode, TypeAnnotationPatch> invisibleTypeAnnotations;
  public ListPatch<TypeAnnotationNode, TypeAnnotationPatch> visibleTypeAnnotations;
  // TODO: attrs
}
