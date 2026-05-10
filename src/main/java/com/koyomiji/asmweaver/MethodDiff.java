package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.*;

import java.util.List;

public class MethodDiff implements IDiff {
  public ListDiff<Integer> access;
  public ListDiff<String> name;
  public ListDiff<String> desc;
  public ListDiff<String> signature;
  public ListDiff<String> exceptions;
  public ListDiff<ParameterNode> parameters;
  public ListDiff<AnnotationNode> visibleAnnotations;
  public ListDiff<AnnotationNode> invisibleAnnotations;
  public ListDiff<TypeAnnotationNode> visibleTypeAnnotations;
  public ListDiff<TypeAnnotationNode> invisibleTypeAnnotations;
  // attrs
  public ListDiff<Object> annotationDefault;
  public ListDiff<Integer> visibleAnnotableParameterCount;
  public KeyedListDiff<Integer, List<AnnotationNode>, ListDiff<AnnotationNode>> visibleParameterAnnotations;
  public ListDiff<Integer> invisibleAnnotableParameterCount;
  public KeyedListDiff<Integer, List<AnnotationNode>, ListDiff<AnnotationNode>> invisibleParameterAnnotations;
  public InsnListDiff instructions;
  public ListDiff<TryCatchBlockNode> tryCatchBlocks;
  public ListDiff<LocalVariableNode> localVariables;
  public ListDiff<LocalVariableAnnotationNode> visibleLocalVariableAnnotations;
  public ListDiff<LocalVariableAnnotationNode> invisibleLocalVariableAnnotations;

  @Override
  public boolean isEmpty() {
    return access.isEmpty()
            && name.isEmpty()
            && desc.isEmpty()
            && signature.isEmpty()
            && exceptions.isEmpty()
            && parameters.isEmpty()
            && visibleAnnotations.isEmpty()
            && invisibleAnnotations.isEmpty()
            && visibleTypeAnnotations.isEmpty()
            && invisibleTypeAnnotations.isEmpty()
            && annotationDefault.isEmpty()
            && visibleAnnotableParameterCount.isEmpty()
            && visibleParameterAnnotations.isEmpty()
            && invisibleAnnotableParameterCount.isEmpty()
            && invisibleParameterAnnotations.isEmpty()
            && instructions.operations.isEmpty()
            && tryCatchBlocks.isEmpty()
            && localVariables.isEmpty()
            && visibleLocalVariableAnnotations.isEmpty()
            && invisibleLocalVariableAnnotations.isEmpty();
  }
}
