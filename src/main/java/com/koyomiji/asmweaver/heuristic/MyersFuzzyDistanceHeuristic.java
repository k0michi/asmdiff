package com.koyomiji.asmweaver.heuristic;

import com.koyomiji.asmweaver.AbstractInsnNodeHelper;
import com.koyomiji.asmweaver.InsnListDiffUtils;
import com.koyomiji.asmweaver.util.BiPersistentHashMap;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyersFuzzyDistanceHeuristic extends Heuristic {
  private final List<AbstractInsnNode> src;
  private final List<AbstractInsnNode> dst;
  private final int n;
  private final int m;

  // 対角線ごとの到達履歴を保持する: key = k, value = DiagonalHistory
  private final Map<Integer, DiagonalHistory> historyMap = new HashMap<>();

  // Myersの状態管理
  private final Map<Integer, Integer> v = new HashMap<>();
  private int currentD = -1;

  public MyersFuzzyDistanceHeuristic(List<AbstractInsnNode> src, List<AbstractInsnNode> dst) {
    this.src = src;
    this.dst = dst;
    this.n = src.size();
    this.m = dst.size();
    this.v.put(1, 0); // 初期値
  }

  /**
   * 終端から逆向きに計算した際の到達履歴を管理するクラス
   */
  private static class DiagonalHistory {
    int[] us = new int[4];
    int[] ds = new int[4];
    int size = 0;

    void add(int u, int d) {
      if (size > 0 && us[size - 1] >= u) {
        return;
      }
      if (size == us.length) {
        us = Arrays.copyOf(us, size * 2);
        ds = Arrays.copyOf(ds, size * 2);
      }
      us[size] = u;
      ds[size] = d;
      size++;
    }

    int getD(int targetU) {
      int left = 0;
      int right = size - 1;
      int ans = -1;
      while (left <= right) {
        int mid = (left + right) >>> 1;
        if (us[mid] >= targetU) {
          ans = ds[mid];
          right = mid - 1;
        } else {
          left = mid + 1;
        }
      }
      return ans;
    }
  }

  @Override
  public int calculate(int targetX, int targetY, BiPersistentHashMap<LabelNode, LabelNode> labelMap) {
    if (targetX < 0 || targetY < 0 || targetX > n || targetY > m) {
      throw new IndexOutOfBoundsException();
    }

    // 元のFuzzyDistanceHeuristicに合わせるため、終端 (n, m) を始点 (0, 0) とみなす座標変換
    int targetU = n - targetX;
    int targetW = m - targetY;
    int targetK = targetU - targetW;

    DiagonalHistory hist = historyMap.get(targetK);
    if (hist != null) {
      int d = hist.getD(targetU);
      if (d != -1) {
        return d;
      }
    }

    while (currentD < n + m) {
      currentD++;
      boolean targetReachedInCurrentD = false;

      for (int k = -currentD; k <= currentD; k += 2) {
        int u; // 逆向きのX座標（終端からの距離）
        int vPrev = v.getOrDefault(k - 1, -1);
        int vNext = v.getOrDefault(k + 1, -1);

        if (k == -currentD || (k != currentD && vPrev < vNext)) {
          u = vNext;
        } else {
          u = vPrev + 1;
        }

        int w = u - k; // 逆向きのY座標

        // スネーク（一致移動）を処理
        // 配列の後ろ（n-1, m-1）から前へ向かって比較する
        while (u < n && w < m && AbstractInsnNodeHelper.equalsIgnoreLabelsExactLocals(src.get(n - 1 - u), dst.get(m - 1 - w))) {
          u++;
          w++;
        }
        v.put(k, u);

        int maxUForK = Math.min(n, m + k);
        int recordU = Math.min(u, maxUForK);
        if (recordU >= 0) {
          DiagonalHistory h = historyMap.computeIfAbsent(k, key -> new DiagonalHistory());
          h.add(recordU, currentD);
        }

        if (k == targetK && recordU >= targetU) {
          targetReachedInCurrentD = true;
        }
      }

      if (targetReachedInCurrentD) {
        return currentD;
      }
    }
    return -1;
  }
}
