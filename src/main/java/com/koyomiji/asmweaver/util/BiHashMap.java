package com.koyomiji.asmweaver.util;

import java.util.*;

public class BiHashMap<K, V> {
  private final Map<K, V> forwardMap = new HashMap<>();
  private final Map<V, K> reverseMap = new HashMap<>();

  public BiHashMap() {}

  public BiHashMap(BiHashMap<K, V> other) {
    this.forwardMap.putAll(other.forwardMap);
    this.reverseMap.putAll(other.reverseMap);
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

  public void put(K key, V value) {
    if (!canPut(key, value)) {
      K existingKey = reverseMap.get(value);
      throw new IllegalArgumentException(
              "Value '" + value + "' is already associated with key '" + existingKey + "'."
      );
    }

    if (forwardMap.containsKey(key)) {
      V oldValue = forwardMap.get(key);
      if (!Objects.equals(oldValue, value)) {
        reverseMap.remove(oldValue);
      }
    }

    forwardMap.put(key, value);
    reverseMap.put(value, key);
  }

  public V get(K key) {
    return forwardMap.get(key);
  }

  public K getKey(V value) {
    return reverseMap.get(value);
  }

  public void remove(K key) {
    if (forwardMap.containsKey(key)) {
      V value = forwardMap.remove(key);
      reverseMap.remove(value);
    }
  }

  @Override
  public String toString() {
    return forwardMap.toString();
  }

  public HashMap<K, V> forwardMap() {
    return new HashMap<>(forwardMap);
  }

  public HashMap<V, K> reverseMap() {
    return new HashMap<>(reverseMap);
  }

  public int size() {
    return forwardMap.size();
  }
}
