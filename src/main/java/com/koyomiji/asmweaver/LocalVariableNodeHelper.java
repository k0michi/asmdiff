package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.BiFunctionHelper;
import com.koyomiji.asmweaver.util.tuple.Pair;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class LocalVariableNodeHelper {
  public static boolean equals(LocalVariableNode a, LocalVariableNode b, BiPredicate<LabelNode, LabelNode> labelEquals, BiPredicate<Pair<LabelNode, Integer>, Pair<LabelNode, Integer>> localEquals) {
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
            && labelEquals.test(a.start, b.start)
            && labelEquals.test(a.end, b.end)
            && localEquals.test(Pair.of(a.start, a.index), Pair.of(b.start, b.index));
  }

  public static boolean equals(LocalVariableNode a, LocalVariableNode b) {
    return equals(a, b, Objects::equals, Objects::equals);
  }
}
