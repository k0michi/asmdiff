package com.koyomiji.asmpatch;

import org.objectweb.asm.TypePath;

public class TypePathEquator implements Equator<TypePath> {
  @Override
  public boolean equals(TypePath a, TypePath b) {
    if (a.getLength() != b.getLength()) {
      return false;
    }

    for (int i = 0; i < a.getLength(); i++) {
      if (a.getStep(i) != b.getStep(i)) {
        return false;
      }

      if (a.getStep(i) == TypePath.TYPE_ARGUMENT) {
        if (a.getStepArgument(i) != b.getStepArgument(i)) {
          return false;
        }
      }
    }

    return true;
  }
}
