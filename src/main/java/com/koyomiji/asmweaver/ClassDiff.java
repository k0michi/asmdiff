package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.*;

public class ClassDiff implements IDiff {
  public ListDiff<Integer> version;
  public ListDiff<Integer> access;
  public ListDiff<String> name;
  public ListDiff<String> signature;
  public ListDiff<String> superName;
  public ListDiff<String> interfaces;
  public ListDiff<String> sourceFile;
  public ListDiff<String> sourceDebug;
  // module
  public ListDiff<String> outerClass;
  public ListDiff<String> outerMethod;
  public ListDiff<String> outerMethodDesc;
  public ListDiff<AnnotationNode> visibleAnnotations;
  public ListDiff<AnnotationNode> invisibleAnnotations;
  public ListDiff<TypeAnnotationNode> visibleTypeAnnotations;
  public ListDiff<TypeAnnotationNode> invisibleTypeAnnotations;
  // attributes
  public ListDiff<InnerClassNode> innerClasses;
  public ListDiff<String> nestHostClass;
  public ListDiff<String> nestMembers;
  public ListDiff<String> permittedSubclasses;
  public KeyedListDiff<MemberKey, RecordComponentNode, RecordComponentDiff> recordComponents;
  public KeyedListDiff<MemberKey, FieldNode, FieldDiff> fields;
  public KeyedListDiff<MemberKey, MethodNode, MethodDiff> methods;

  @Override
  public boolean isEmpty() {
    return version.isEmpty()
            && access.isEmpty()
            && name.isEmpty()
            && signature.isEmpty()
            && superName.isEmpty()
            && interfaces.isEmpty()
            && sourceFile.isEmpty()
            && sourceDebug.isEmpty()
            && outerClass.isEmpty()
            && outerMethod.isEmpty()
            && outerMethodDesc.isEmpty()
            && visibleAnnotations.isEmpty()
            && invisibleAnnotations.isEmpty()
            && visibleTypeAnnotations.isEmpty()
            && invisibleTypeAnnotations.isEmpty()
            && innerClasses.isEmpty()
            && nestHostClass.isEmpty()
            && nestMembers.isEmpty()
            && permittedSubclasses.isEmpty()
            && recordComponents.isEmpty()
            && fields.isEmpty()
            && methods.isEmpty();
  }
}
