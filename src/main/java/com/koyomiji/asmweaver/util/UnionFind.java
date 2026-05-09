package com.koyomiji.asmweaver.util;

import java.util.*;

/**
 * 汎用的な Union-Find (素集合データ構造)
 * @param <T> ノードの型 (equals() と hashCode() が正しく実装されていること)
 */
public class UnionFind<T> {
  // 各ノードの親を保持するマップ
  private final Map<T, T> parent = new HashMap<>();
  // 各木の高さを保持するマップ（最適化用）
  private final Map<T, Integer> rank = new HashMap<>();

  /**
   * ノードを新たに登録します。既に存在する場合は何もしません。
   */
  public void addNode(T node) {
    if (!parent.containsKey(node)) {
      parent.put(node, node); // 初期状態では自分が親
      rank.put(node, 0);
    }
  }

  /**
   * ノードが属するグループの根（代表元）を返します。
   * 探索と同時に「経路圧縮」を行い、次回以降の探索を高速化します。
   */
  public T find(T node) {
    if (!parent.containsKey(node)) {
      throw new IllegalArgumentException("Node not found: " + node);
    }

    T p = parent.get(node);
    if (!p.equals(node)) {
      // 再帰的に根を探し、親を直接根に繋ぎ直す（経路圧縮）
      T root = find(p);
      parent.put(node, root);
    }
    return parent.get(node);
  }

  /**
   * 2つのノードが属するグループを結合します。
   */
  public void union(T node1, T node2) {
    T root1 = find(node1);
    T root2 = find(node2);

    if (root1.equals(root2)) {
      return; // 既に同じグループ
    }

    int rank1 = rank.get(root1);
    int rank2 = rank.get(root2);

    // ランク（木の高さ）が低い方を、高い方に繋ぐ（Union by Rank）
    if (rank1 > rank2) {
      parent.put(root2, root1);
    } else if (rank1 < rank2) {
      parent.put(root1, root2);
    } else {
      parent.put(root2, root1);
      rank.put(root1, rank1 + 1); // 高さが同じ場合は+1
    }
  }

  /**
   * 現在のすべてのグループ（連結成分）を抽出して返します。
   * 今回の目的である「論理変数のスコープ一覧」を取得するためのメソッドです。
   */
  public List<Set<T>> getGroups() {
    Map<T, Set<T>> groups = new HashMap<>();

    for (T node : parent.keySet()) {
      T root = find(node); // 確実に最新の根を取得
      groups.computeIfAbsent(root, k -> new HashSet<>()).add(node);
    }

    return new ArrayList<>(groups.values());
  }
}