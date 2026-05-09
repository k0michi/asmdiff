package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.HashCodeBuilder;
import org.objectweb.asm.TypePath;

public class TypePathHelper {
  public static boolean equals(TypePath a, TypePath b) {
    if (a == b) {
      return true;
    }

    if (a == null || b == null) {
      return false;
    }

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

  public static int hashCode(TypePath typePath) {
    if (typePath == null) {
      return 0;
    }

    HashCodeBuilder builder = new HashCodeBuilder();

    for (int i = 0; i < typePath.getLength(); i++) {
      builder.append(typePath.getStep(i));

      if (typePath.getStep(i) == TypePath.TYPE_ARGUMENT) {
        builder.append(typePath.getStepArgument(i));
      }
    }

    return builder.build();
  }
}
