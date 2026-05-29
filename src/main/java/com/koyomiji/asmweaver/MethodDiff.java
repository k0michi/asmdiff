package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.*;

import java.util.List;

public class MethodDiff implements IDiff {
  public boolean isEmpty;

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
  public ListDiff<LineNumberNode> lineNumbers;
  public ListDiff<TryCatchBlockNode> tryCatchBlocks;
  public ListDiff<Integer> maxStack;
  public ListDiff<Integer> maxLocals;
  public ListDiff<LocalVariableNode> localVariables;
  public ListDiff<LocalVariableAnnotationNode> visibleLocalVariableAnnotations;
  public ListDiff<LocalVariableAnnotationNode> invisibleLocalVariableAnnotations;

  @Override
  public boolean isEmpty() {
    if (isEmpty) {
      return true;
    }

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
            && instructions.isEmpty()
            && lineNumbers.isEmpty()
            && tryCatchBlocks.isEmpty()
            && maxStack.isEmpty()
            && maxLocals.isEmpty()
            && localVariables.isEmpty()
            && visibleLocalVariableAnnotations.isEmpty()
            && invisibleLocalVariableAnnotations.isEmpty();
  }

  @Override
  public int distance() {
    if (isEmpty) {
      return 0;
    }

    return access.distance()
            + name.distance()
            + desc.distance()
            + signature.distance()
            + exceptions.distance()
            + parameters.distance()
            + visibleAnnotations.distance()
            + invisibleAnnotations.distance()
            + visibleTypeAnnotations.distance()
            + invisibleTypeAnnotations.distance()
            + annotationDefault.distance()
            + visibleAnnotableParameterCount.distance()
            + visibleParameterAnnotations.distance()
            + invisibleAnnotableParameterCount.distance()
            + invisibleParameterAnnotations.distance()
            + instructions.distance()
            + lineNumbers.distance()
            + tryCatchBlocks.distance()
            + maxStack.distance()
            + maxLocals.distance()
            + localVariables.distance()
            + visibleLocalVariableAnnotations.distance()
            + invisibleLocalVariableAnnotations.distance();
  }
}
