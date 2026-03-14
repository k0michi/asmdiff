package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.TypeAnnotationNode;

public class ClassPatch {
  public ValuePatch<Integer> version;
  public ValuePatch<Integer> access;
  public ValuePatch<String> name;
  public ValuePatch<String> signature;
  public ValuePatch<String> superName;
  public ListPatch<String, ValuePatch<String>, String> interfaces;
  public ValuePatch<String> sourceFile;
  public ValuePatch<String> sourceDebug;
  // TODO: module
  public ValuePatch<String> outerClass;
  public ValuePatch<String> outerMethod;
  public ValuePatch<String> outerMethodDesc;
  public ListPatch<AnnotationNode, AnnotationPatch, Object> visibleAnnotations;
  public ListPatch<AnnotationNode, AnnotationPatch, Object> invisibleAnnotations;
  public ListPatch<TypeAnnotationNode, TypeAnnotationPatch, Object> visibleTypeAnnotations;
  public ListPatch<TypeAnnotationNode, TypeAnnotationPatch, Object> invisibleTypeAnnotations;
  // TODO: attributes
  public ListPatch<InnerClassNode, InnerClassPatch, String> innerClasses;
  public ValuePatch<String> nestHostClass;
  public ListPatch<String, ValuePatch<String>, String> nestMembers;
  public ListPatch<String, ValuePatch<String>, String> permittedSubclasses;
  // TODO: record components
  // TODO: fields
  // TODO: methods
}
