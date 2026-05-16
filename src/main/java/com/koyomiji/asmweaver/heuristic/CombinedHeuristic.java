package com.koyomiji.asmweaver.heuristic;

import com.koyomiji.asmweaver.InsnListDiffUtils;
import com.koyomiji.asmweaver.util.BiPersistentHashMap;
import org.objectweb.asm.tree.LabelNode;

import java.util.List;

public class CombinedHeuristic extends Heuristic {
  private List<Heuristic> heuristics;

  public CombinedHeuristic(List<Heuristic> heuristics) {
    this.heuristics = heuristics;
  }

  @Override
  public int calculate(int indexA, int indexB, BiPersistentHashMap<LabelNode, LabelNode> labelMap) {
    int max = 0;
    for (Heuristic h : heuristics) {
      max = Math.max(max, h.calculate(indexA, indexB, labelMap));
    }
    return max;
  }
}
