package com.koyomiji.asmweaver.heuristic;

import com.koyomiji.asmweaver.InsnListDiffUtils;
import com.koyomiji.asmweaver.util.BiPersistentHashMap;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BigramHeuristic extends Heuristic {
  private static final int K = 32; // 追跡する頻出Bigramの数
  private final int[][] prefixA;   // [insnsA.size() + 1][K]
  private final int[][] prefixB;   // [insnsB.size() + 1][K]
  private final int totalA;
  private final int totalB;

  public BigramHeuristic(List<AbstractInsnNode> insnsA, List<AbstractInsnNode> insnsB) {
    this.totalA = insnsA.size();
    this.totalB = insnsB.size();

    // 1. 命令を抽象化されたIDに変換 (Opcode等をベースにする)
    int[] idsA = simplifyInsns(insnsA);
    int[] idsB = simplifyInsns(insnsB);

    // 2. 全Bigramをカウントし、頻出Top Kを選択
    int[] topBigrams = selectTopBigrams(idsA, idsB, K);

    // 3. 累積和テーブルの構築 O(N * K)
    // メモリ消費: 100,000 * 32 * 4 bytes = 約12.8MB (AとBで計25MB程度)
    this.prefixA = buildPrefixTable(idsA, topBigrams);
    this.prefixB = buildPrefixTable(idsB, topBigrams);
  }

  @Override
  public int calculate(int indexA, int indexB, BiPersistentHashMap<LabelNode, LabelNode> labelMap) {
    // 残りの長さの差 (Edit Distanceの絶対最小下界)
    int dist = Math.abs((totalA - indexA) - (totalB - indexB));

    // Bigram頻度差分による下界計算 (Ukkonenの定理に基づく近似)
    // 順序を考慮したより厳しい下界を算出する
    int bigramDiffSum = 0;
    for (int k = 0; k < K; k++) {
      int countA = prefixA[totalA][k] - prefixA[indexA][k];
      int countB = prefixB[totalB][k] - prefixB[indexB][k];
      bigramDiffSum += Math.abs(countA - countB);
    }

    // Bigramの差分合計 / 2 は編集距離の強力な下界となる
    return Math.max(dist, (bigramDiffSum + 1) / 2);
  }

  private int[] simplifyInsns(List<AbstractInsnNode> insns) {
    int[] ids = new int[insns.size()];
    for (int i = 0; i < insns.size(); i++) {
      AbstractInsnNode insn = insns.get(i);
      // LabelやLineNumberを無視し、Opcodeを基本とする
      // 必要に応じてフィールド参照やメソッド名のハッシュを混ぜる
      ids[i] = insn.getOpcode();
    }
    return ids;
  }

  private int[] selectTopBigrams(int[] idsA, int[] idsB, int k) {
    Map<Integer, Integer> counts = new HashMap<>();
    countBigrams(idsA, counts);
    countBigrams(idsB, counts);

    return counts.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .limit(k)
            .mapToInt(Map.Entry::getKey)
            .toArray();
  }

  private void countBigrams(int[] ids, Map<Integer, Integer> counts) {
    for (int i = 0; i < ids.length - 1; i++) {
      // 2つの命令IDを1つのintにパック (Bigram)
      int bigram = (ids[i] << 8) | (ids[i + 1] & 0xFF);
      counts.merge(bigram, 1, Integer::sum);
    }
  }

  private int[][] buildPrefixTable(int[] ids, int[] topBigrams) {
    int n = ids.length;
    int[][] table = new int[n + 1][K];

    // 命令が2つ未満ならBigramは作れないので、すべて0のまま返す
    if (n < 2) {
      return table;
    }

    for (int i = 0; i < n - 1; i++) {
      // 前の累積値を次の行にコピー（i行目からi+1行目へ）
      System.arraycopy(table[i], 0, table[i + 1], 0, K);

      int currentBigram = (ids[i] << 8) | (ids[i + 1] & 0xFF);
      for (int k = 0; k < K; k++) {
        if (topBigrams[k] == currentBigram) {
          table[i + 1][k]++;
          break;
        }
      }
    }

    // 最後の行（n行目）にも、(n-1)行目の結果をコピーして完了
    // これにより、どのインデックスから見ても「それまでの累積」が取れるようになる
    System.arraycopy(table[n - 1], 0, table[n], 0, K);

    return table;
  }
}
