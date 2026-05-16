package com.koyomiji.asmweaver.heuristic;

import com.koyomiji.asmweaver.util.BiPersistentHashMap;
import org.objectweb.asm.tree.LabelNode;

public abstract class Heuristic {
  public abstract int calculate(int indexA, int indexB, BiPersistentHashMap<LabelNode, LabelNode> labelMap);
}
