package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.BiFunctionHelper;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableAnnotationNode;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class LocalVariableAnnotationNodeHelper {
  public static boolean equals(LocalVariableAnnotationNode node1, LocalVariableAnnotationNode node2, Function<LabelNode, LabelNode> labelMap, BiFunction<LabelNode, Integer, Integer> localMap) {
    if (!TypeAnnotationNodeHelper.equals(node1, node2)) {
      return false;
    }

    if (node1.start.size() != node2.start.size()) {
      return false;
    }

    if (node1.end.size() != node2.end.size()) {
      return false;
    }

    if (node1.index.size() != node2.index.size()) {
      return false;
    }

    int size = Math.max(node1.start.size(), Math.max(node1.end.size(), node1.index.size()));

    for (int i = 0; i < size; i++) {
      LabelNode mappedStart1 = labelMap.apply(ListHelper.getOrNull(node1.start, i));
      LabelNode mappedEnd1 = labelMap.apply(ListHelper.getOrNull(node1.end, i));
      Integer mappedIndex1 = localMap.apply(ListHelper.getOrNull(node1.start, i), ListHelper.getOrNull(node1.index, i));
      LabelNode start2 = ListHelper.getOrNull(node2.start, i);
      LabelNode end2 = ListHelper.getOrNull(node2.end, i);
      Integer index2 = ListHelper.getOrNull(node2.index, i);

      if (!Objects.equals(mappedStart1, start2) || !Objects.equals(mappedEnd1, end2) || !Objects.equals(mappedIndex1, index2)) {
        return false;
      }
    }

    return true;
  }

  public static boolean equals(LocalVariableAnnotationNode node1, LocalVariableAnnotationNode node2) {
    return equals(node1, node2, Function.identity(), BiFunctionHelper.second());
  }
}
