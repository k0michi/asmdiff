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
  public ListPatch<AnnotationNode, AnnotationPatch, String> invisibleAnnotations;
  public ListPatch<AnnotationNode, AnnotationPatch, String> visibleAnnotations;
  public ListPatch<TypeAnnotationNode, TypeAnnotationPatch, String> invisibleTypeAnnotations;
  public ListPatch<TypeAnnotationNode, TypeAnnotationPatch, String> visibleTypeAnnotations;
  // TODO: attrs
}
