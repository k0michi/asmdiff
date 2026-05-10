package com.koyomiji.asmweaver.util;

import java.util.Arrays;
import java.util.Objects;

public class PersistentHashMap<K, V> {
  private final Node<K, V> root;
  private final int size;
  private final int cachedHash;

  public PersistentHashMap() {
    this.root = null;
    this.size = 0;
    this.cachedHash = 0;
  }

  private PersistentHashMap(Node<K, V> root, int size, int cachedHash) {
    this.root = root;
    this.size = size;
    this.cachedHash = cachedHash;
  }

  public int size() {
    return size;
  }

  @Override
  public int hashCode() {
    return cachedHash; // O(1) で即座に返す
  }

  public V get(K key) {
    if (root == null) return null;
    int h = Objects.hashCode(key);
    return root.get(0, h, key);
  }

  public PersistentHashMap<K, V> put(K key, V value) {
    int h = Objects.hashCode(key);

    // 増分ハッシュ計算のために古い値を取得
    V oldValue = get(key);
    Node<K, V> newRoot = (root == null)
            ? new LeafNode<>(h, key, value)
            : root.put(0, h, key, value);

    if (newRoot == root) return this;

    // エントリのハッシュ計算: (k.hash ^ v.hash)
    int entryHash = Objects.hashCode(key) ^ Objects.hashCode(value);
    int newHash = this.cachedHash;

    if (newRoot.isAdded()) {
      // 新規追加
      newHash += entryHash;
    } else {
      // 値の更新: 古いエントリ分を引き、新しい分を足す
      int oldEntryHash = Objects.hashCode(key) ^ Objects.hashCode(oldValue);
      newHash = newHash - oldEntryHash + entryHash;
    }

    return new PersistentHashMap<>(newRoot,
            root == null ? 1 : size + (newRoot.isAdded() ? 1 : 0),
            newHash);
  }

  public PersistentHashMap<K, V> remove(K key) {
    if (root == null) return this;

    V oldValue = get(key);
    if (oldValue == null) return this; // 存在しないなら何もしない

    int h = Objects.hashCode(key);
    Node<K, V> newRoot = root.remove(0, h, key);

    if (newRoot == root) return this;

    // 削除されたエントリのハッシュ分を引く
    int oldEntryHash = Objects.hashCode(key) ^ Objects.hashCode(oldValue);
    return new PersistentHashMap<>(newRoot, size - 1, cachedHash - oldEntryHash);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PersistentHashMap)) return false;
    PersistentHashMap<?, ?> that = (PersistentHashMap<?, ?>) o;
    // ハッシュ値が異なる場合は構造比較すら不要
    if (this.cachedHash != that.cachedHash || this.size != that.size) return false;
    if (this.size == 0) return true;
    return Objects.equals(this.root, that.root);
  }

  // --- Utils ---
  private static int bitpos(int hash, int shift) {
    return 1 << ((hash >>> shift) & 0x1f);
  }

  private static int index(int bitmap, int bit) {
    return Integer.bitCount(bitmap & (bit - 1));
  }

  // --- Node Interface ---
  private interface Node<K, V> {
    V get(int shift, int hash, K key);
    Node<K, V> put(int shift, int hash, K key, V value);
    Node<K, V> remove(int shift, int hash, K key);
    boolean isAdded(); // 直前のputで要素が増えたか（内部状態保持用）
  }

  // --- IndexedNode (中間ノード) ---
  private static final class IndexedNode<K, V> implements Node<K, V> {
    private final int bitmap;
    private final Node<K, V>[] nodes;
    private boolean added;

    IndexedNode(int bitmap, Node<K, V>[] nodes) {
      this.bitmap = bitmap;
      this.nodes = nodes;
    }

    @Override
    public V get(int shift, int hash, K key) {
      int bit = bitpos(hash, shift);
      if ((bitmap & bit) == 0) return null;
      return nodes[index(bitmap, bit)].get(shift + 5, hash, key);
    }

    @Override
    public Node<K, V> put(int shift, int hash, K key, V value) {
      int bit = bitpos(hash, shift);
      int idx = index(bitmap, bit);

      if ((bitmap & bit) != 0) {
        // 既存のパスを辿る
        Node<K, V> child = nodes[idx];
        Node<K, V> newChild = child.put(shift + 5, hash, key, value);
        if (child == newChild) return this;

        Node<K, V>[] newNodes = nodes.clone();
        newNodes[idx] = newChild;
        IndexedNode<K, V> newNode = new IndexedNode<>(bitmap, newNodes);
        newNode.added = newChild.isAdded();
        return newNode;
      } else {
        // 新しい枝を作成
        Node<K, V> leaf = new LeafNode<>(hash, key, value);
        Node<K, V>[] newNodes = (Node<K, V>[]) new Node[nodes.length + 1];
        System.arraycopy(nodes, 0, newNodes, 0, idx);
        newNodes[idx] = leaf;
        System.arraycopy(nodes, idx, newNodes, idx + 1, nodes.length - idx);

        IndexedNode<K, V> newNode = new IndexedNode<>(bitmap | bit, newNodes);
        newNode.added = true;
        return newNode;
      }
    }

    @Override
    public Node<K, V> remove(int shift, int hash, K key) {
      int bit = bitpos(hash, shift);
      if ((bitmap & bit) == 0) return this;

      int idx = index(bitmap, bit);
      Node<K, V> child = nodes[idx];
      Node<K, V> newChild = child.remove(shift + 5, hash, key);

      if (child == newChild) return this;
      if (newChild == null) {
        // 子が空になった場合
        int newBitmap = bitmap ^ bit;
        if (newBitmap == 0) return null;

        Node<K, V>[] newNodes = (Node<K, V>[]) new Node[nodes.length - 1];
        System.arraycopy(nodes, 0, newNodes, 0, idx);
        System.arraycopy(nodes, idx + 1, newNodes, idx, nodes.length - idx - 1);

        // カノニカル形式：子が1つで、それがLeafNodeなら縮退させる
        if (newNodes.length == 1 && newNodes[0] instanceof LeafNode) {
          return newNodes[0];
        }
        return new IndexedNode<>(newBitmap, newNodes);
      }

      Node<K, V>[] newNodes = nodes.clone();
      newNodes[idx] = newChild;
      return new IndexedNode<>(bitmap, newNodes);
    }

    @Override public boolean isAdded() { return added; }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof IndexedNode)) return false;
      IndexedNode<?, ?> that = (IndexedNode<?, ?>) o;
      // ビットマップが同じで、かつ子ノード配列が全て等価かチェック
      return bitmap == that.bitmap && Arrays.equals(nodes, that.nodes);
    }

    @Override
    public int hashCode() {
      int result = Objects.hash(bitmap);
      result = 31 * result + Arrays.hashCode(nodes);
      return result;
    }
  }

  // --- LeafNode (末端ノード) ---
  private static final class LeafNode<K, V> implements Node<K, V> {
    private final int hash;
    private final K key;
    private final V value;
    private boolean added;

    LeafNode(int hash, K key, V value) {
      this.hash = hash;
      this.key = key;
      this.value = value;
    }

    @Override
    public V get(int shift, int hash, K key) {
      return Objects.equals(this.key, key) ? value : null;
    }

    @Override
    public Node<K, V> put(int shift, int hash, K key, V value) {
      if (Objects.equals(this.key, key)) {
        if (Objects.equals(this.value, value)) return this;
        return new LeafNode<>(hash, key, value);
      }

      // ハッシュ衝突または深層への移動
      Node<K, V> newLeaf = new LeafNode<>(hash, key, value);
      if (this.hash == hash) {
        return new CollisionNode<>(hash, (Node<K, V>[]) new Node[]{this, newLeaf});
      }

      // IndexedNodeを作成して両方のリーフを格納
      Node<K, V> empty = new IndexedNode<>(0, (Node<K, V>[]) new Node[0]);
      return empty.put(shift, this.hash, this.key, this.value)
              .put(shift, hash, key, value);
    }

    @Override
    public Node<K, V> remove(int shift, int hash, K key) {
      return Objects.equals(this.key, key) ? null : this;
    }

    @Override public boolean isAdded() { return added; }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof LeafNode)) return false;
      LeafNode<?, ?> that = (LeafNode<?, ?>) o;
      return hash == that.hash &&
              Objects.equals(key, that.key) &&
              Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(hash, key, value);
    }
  }

  // --- CollisionNode (ハッシュ衝突用) ---
  private static final class CollisionNode<K, V> implements Node<K, V> {
    private final int hash;
    private final Node<K, V>[] entries; // LeafNode[]
    private boolean added;

    CollisionNode(int hash, Node<K, V>[] entries) {
      this.hash = hash;
      this.entries = entries;
    }

    @Override
    public V get(int shift, int hash, K key) {
      for (Node<K, V> node : entries) {
        V val = node.get(shift, hash, key);
        if (val != null) return val;
      }
      return null;
    }

    @Override
    public Node<K, V> put(int shift, int hash, K key, V value) {
      for (int i = 0; i < entries.length; i++) {
        if (Objects.equals(((LeafNode<K, V>)entries[i]).key, key)) {
          if (Objects.equals(((LeafNode<K, V>)entries[i]).value, value)) return this;
          Node<K, V>[] newEntries = entries.clone();
          newEntries[i] = new LeafNode<>(hash, key, value);
          return new CollisionNode<>(hash, newEntries);
        }
      }
      Node<K, V>[] newEntries = Arrays.copyOf(entries, entries.length + 1);
      newEntries[entries.length] = new LeafNode<>(hash, key, value);
      CollisionNode<K, V> newNode = new CollisionNode<>(hash, newEntries);
      newNode.added = true;
      return newNode;
    }

    @Override
    public Node<K, V> remove(int shift, int hash, K key) {
      for (int i = 0; i < entries.length; i++) {
        if (Objects.equals(((LeafNode<K, V>)entries[i]).key, key)) {
          if (entries.length == 2) {
            return entries[1 - i]; // 残った方をLeafNodeとして返す
          }
          Node<K, V>[] newEntries = (Node<K, V>[]) new Node[entries.length - 1];
          System.arraycopy(entries, 0, newEntries, 0, i);
          System.arraycopy(entries, i + 1, newEntries, i, entries.length - i - 1);
          return new CollisionNode<>(hash, newEntries);
        }
      }
      return this;
    }

    @Override public boolean isAdded() { return added; }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof CollisionNode)) return false;
      CollisionNode<?, ?> that = (CollisionNode<?, ?>) o;
      // ハッシュ値が同じで、衝突している全エントリが一致するか
      return hash == that.hash && Arrays.equals(entries, that.entries);
    }

    @Override
    public int hashCode() {
      int result = Objects.hash(hash);
      result = 31 * result + Arrays.hashCode(entries);
      return result;
    }
  }
}
