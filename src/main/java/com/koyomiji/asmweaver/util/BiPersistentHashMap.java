package com.koyomiji.asmweaver.util;

import java.util.Objects;

public class BiPersistentHashMap<K, V> {
  private final PersistentHashMap<K, V> forwardMap;
  private final PersistentHashMap<V, K> reverseMap;

  public BiPersistentHashMap() {
    this.forwardMap = new PersistentHashMap<>();
    this.reverseMap = new PersistentHashMap<>();
  }

  public BiPersistentHashMap(BiPersistentHashMap<K, V> other) {
    this.forwardMap = other.forwardMap;
    this.reverseMap = other.reverseMap;
  }

  private BiPersistentHashMap(PersistentHashMap<K, V> forwardMap, PersistentHashMap<V, K> reverseMap) {
    this.forwardMap = forwardMap;
    this.reverseMap = reverseMap;
  }

  public boolean canPut(K key, V value) {
    if (forwardMap.containsKey(key)) {
      V existingValue = forwardMap.get(key);
      return Objects.equals(existingValue, value);
    }

    if (reverseMap.containsKey(value)) {
      K existingKey = reverseMap.get(value);
      return Objects.equals(existingKey, key);
    }

    return true;
  }

  public BiPersistentHashMap<K, V> put(K key, V value) {
    if (!canPut(key, value)) {
      K existingKey = reverseMap.get(value);
      throw new IllegalArgumentException(
              "Value '" + value + "' is already associated with key '" + existingKey + "'."
      );
    }

    PersistentHashMap<K, V> newForwardMap = forwardMap.put(key, value);
    PersistentHashMap<V, K> newReverseMap = reverseMap.put(value, key);
    return new BiPersistentHashMap<>(newForwardMap, newReverseMap);
  }

  public V get(K key) {
    return forwardMap.get(key);
  }

  public K getKey(V value) {
    return reverseMap.get(value);
  }

  public boolean containsKey(K key) {
    return forwardMap.containsKey(key);
  }

  public boolean containsValue(V value) {
    return reverseMap.containsKey(value);
  }

  public BiPersistentHashMap<K, V> remove(K key) {
    if (!forwardMap.containsKey(key)) {
      return this; // キーが存在しない場合は変更なし
    }

    V value = forwardMap.get(key);
    PersistentHashMap<K, V> newForwardMap = forwardMap.remove(key);
    PersistentHashMap<V, K> newReverseMap = reverseMap.remove(value);
    return new BiPersistentHashMap<>(newForwardMap, newReverseMap);
  }

  public int size() {
    return forwardMap.size();
  }

  public PersistentHashMap<K, V> forwardMap() {
    return forwardMap;
  }

  public PersistentHashMap<V, K> reverseMap() {
    return reverseMap;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BiPersistentHashMap<?, ?> that = (BiPersistentHashMap<?, ?>) o;
    return Objects.equals(forwardMap, that.forwardMap);
  }

  @Override
  public int hashCode() {
    return forwardMap.hashCode();
  }
}
