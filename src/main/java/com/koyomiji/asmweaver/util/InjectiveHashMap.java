package com.koyomiji.asmweaver.util;

import java.util.*;

public class InjectiveHashMap<K, V> {
  private final Map<K, V> forwardMap = new HashMap<>();
  private final Map<V, K> reverseMap = new HashMap<>();

  public boolean canPut(K key, V value) {
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
}
