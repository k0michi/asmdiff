package com.koyomiji.asmpatch;

import org.objectweb.asm.TypePath;

public class TypeAnnotationPatch extends AnnotationPatch {
  public ValuePatch<Integer> typeRef;
  public ValuePatch<TypePath> typePath;
}
