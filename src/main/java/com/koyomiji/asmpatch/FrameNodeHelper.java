package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.Objects;

public class FrameNodeHelper {
  public static boolean equals(FrameNode a, FrameNode b) {
    if (!AbstractInsnNodeHelper.equals(a, b)) {
      return false;
    }

    if (a.type != b.type) {
      return false;
    }

    if (!ListHelper.equals(a.local, b.local, FrameNodeHelper::equalsElement)) {
      return false;
    }

    if (!ListHelper.equals(a.stack, b.stack, FrameNodeHelper::equalsElement)) {
      return false;
    }

    return true;
  }

  private static boolean equalsElement(Object a, Object b) {
    if (a instanceof LabelNode && b instanceof LabelNode) {
      return LabelNodeHelper.equals((LabelNode) a, (LabelNode) b);
    }

    return Objects.equals(a, b);
  }
}
