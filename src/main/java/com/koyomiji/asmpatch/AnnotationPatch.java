package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.AnnotationNode;

public class AnnotationPatch {
  public ValuePatch<String> desc;
  public ListPatch<Object, AnnotationValuePatch> values;
}
