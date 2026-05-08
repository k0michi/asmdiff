package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.BiFunctionHelper;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class LocalVariableNodeHelper {
  public static boolean equals(LocalVariableNode a, LocalVariableNode b, Function<LabelNode, LabelNode> labelMap, BiFunction<LabelNode, Integer, Integer> localMap) {
    if (a == b) {
      return true;
    }

    if (a == null || b == null) {
      return false;
    }

    if (a.getClass() != b.getClass()) {
      return false;
    }

    return Objects.equals(a.name, b.name)
            && Objects.equals(a.desc, b.desc)
            && Objects.equals(a.signature, b.signature)
            && labelMap.apply(a.start) == b.start
            && labelMap.apply(a.end) == b.end
            && localMap.apply(a.start, a.index) == b.index;
  }

  public static boolean equals(LocalVariableNode a, LocalVariableNode b) {
    return equals(a, b, Function.identity(), BiFunctionHelper.second());
  }
}
