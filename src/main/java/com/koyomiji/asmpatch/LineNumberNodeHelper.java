package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.LineNumberNode;

public class LineNumberNodeHelper {
  public static boolean equals(LineNumberNode a, LineNumberNode b) {
    if (!AbstractInsnNodeHelper.equals(a, b)) {
      return false;
    }

    if (a.line != b.line) {
      return false;
    }

    if (!LabelNodeHelper.equals(a.start, b.start)) {
      return false;
    }

    return true;
  }
}
