package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.*;

import java.util.List;

public class MethodDiff {
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
}
