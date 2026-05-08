package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.TypeAnnotationNode;

import java.util.Objects;

public class TypeAnnotationNodeHelper {
  public static boolean equals(TypeAnnotationNode a, TypeAnnotationNode b) {
    if (!AnnotationNodeHelper.equals(a, b)) {
      return false;
    }

    if (!Objects.equals(a.typeRef, b.typeRef)) {
      return false;
    }

    if (!TypePathHelper.equals(a.typePath, b.typePath)) {
      return false;
    }

    return true;
  }
}
