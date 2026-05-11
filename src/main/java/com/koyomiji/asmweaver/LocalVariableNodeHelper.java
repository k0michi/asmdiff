package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.tuple.Triplet;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;

import java.util.Objects;
import java.util.function.BiPredicate;

public class LocalVariableNodeHelper {
  public static boolean equals(LocalVariableNode a, LocalVariableNode b, BiPredicate<LabelNode, LabelNode> labelEquals, BiPredicate<Triplet<LabelNode, LabelNode, Integer>, Triplet<LabelNode, LabelNode, Integer>> localEquals) {
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
            && localEquals.test(Triplet.of(a.start, a.end, a.index), Triplet.of(b.start, b.end, b.index));
  }

  public static boolean equals(LocalVariableNode a, LocalVariableNode b) {
    return equals(a, b, Objects::equals, Objects::equals);
  }

  public static int hashCode(LocalVariableNode node) {
    if (node == null) {
      return 0;
    }

    return Objects.hash(node.name, node.desc, node.signature, node.start, node.end, node.index);
  }
}
