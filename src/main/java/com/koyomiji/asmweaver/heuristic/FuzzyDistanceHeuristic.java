package com.koyomiji.asmweaver.heuristic;

import com.koyomiji.asmweaver.AbstractInsnNodeHelper;
import com.koyomiji.asmweaver.InsnListDiffUtils;
import com.koyomiji.asmweaver.util.BiPersistentHashMap;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.List;

public class FuzzyDistanceHeuristic extends Heuristic {
  private int[][] table;

  public FuzzyDistanceHeuristic(List<AbstractInsnNode> insnsA, List<AbstractInsnNode> insnsB) {
    this.table = new int[insnsA.size() + 1][insnsB.size() + 1];

    for (int j = 0; j <= insnsB.size(); j++) {
      table[insnsA.size()][j] = insnsB.size() - j;
    }

    for (int i = 0; i <= insnsA.size(); i++) {
      table[i][insnsB.size()] = insnsA.size() - i;
    }

    for (int i = insnsA.size() - 1; i >= 0; i--) {
      for (int j = insnsB.size() - 1; j >= 0; j--) {
        AbstractInsnNode a = insnsA.get(i);
        AbstractInsnNode b = insnsB.get(j);

        table[i][j] = 1 + Math.min(table[i + 1][j], table[i][j + 1]);

        if (AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(a, b)) {
          table[i][j] = Math.min(table[i][j], table[i + 1][j + 1]);
        }
      }
    }
  }

  @Override
  public int calculate(int indexA, int indexB, BiPersistentHashMap<LabelNode, LabelNode> labelMap) {
    return table[indexA][indexB];
  }
}
