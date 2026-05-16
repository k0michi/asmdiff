package com.koyomiji.asmweaver.heuristic;

import com.koyomiji.asmweaver.InsnListDiffUtils;
import com.koyomiji.asmweaver.util.BiPersistentHashMap;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.List;

public class DistanceHeuristic extends Heuristic {
  private int n;
  private int m;

  public DistanceHeuristic(List<AbstractInsnNode> insnsA, List<AbstractInsnNode> insnsB) {
    this.n = insnsA.size();
    this.m = insnsB.size();
  }

  @Override
  public int calculate(int indexA, int indexB, BiPersistentHashMap<LabelNode, LabelNode> labelMap) {
    return (n - indexA) + (m - indexB);
  }
}
