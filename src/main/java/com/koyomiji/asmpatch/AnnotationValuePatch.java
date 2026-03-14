package com.koyomiji.asmpatch;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

public class AnnotationValuePatch implements IPatch<Object> {
  public ValuePatch<Object> objectValue;
  public AnnotationPatch annotationValue;
  public ListPatch<Object, AnnotationValuePatch, Object> annotationArrayValue;
}
