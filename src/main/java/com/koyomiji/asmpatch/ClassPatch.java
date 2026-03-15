package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.*;

public class ClassPatch {
  public ValuePatch<Integer> version;
  public ValuePatch<Integer> access;
  public ValuePatch<String> name;
  public ValuePatch<String> signature;
  public ValuePatch<String> superName;
  public ListPatch<String, ValuePatch<String>> interfaces;
  public ValuePatch<String> sourceFile;
  public ValuePatch<String> sourceDebug;
  public NullablePatch<ModuleNode, ModulePatch> module;
  public ValuePatch<String> outerClass;
  public ValuePatch<String> outerMethod;
  public ValuePatch<String> outerMethodDesc;
  public ListPatch<AnnotationNode, AnnotationPatch> visibleAnnotations;
  public ListPatch<AnnotationNode, AnnotationPatch> invisibleAnnotations;
  public ListPatch<TypeAnnotationNode, TypeAnnotationPatch> visibleTypeAnnotations;
  public ListPatch<TypeAnnotationNode, TypeAnnotationPatch> invisibleTypeAnnotations;
  // TODO: attributes
  public ListPatch<InnerClassNode, InnerClassPatch> innerClasses;
  public ValuePatch<String> nestHostClass;
  public ListPatch<String, ValuePatch<String>> nestMembers;
  public ListPatch<String, ValuePatch<String>> permittedSubclasses;
  // TODO: record components
  // TODO: fields
  // TODO: methods
}
