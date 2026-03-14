package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.AnnotationNode;

public class AnnotationPatch implements IPatch<AnnotationNode> {
  public ValuePatch<String> desc;
  public ListPatch<Object, AnnotationValuePatch, Object> values;
}
