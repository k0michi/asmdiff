package com.koyomiji.asmweaver.util;

public class AutoIncrementBiHashMap<K> extends BiHashMap<K, Integer> {
  private int nextId = 0;

  public AutoIncrementBiHashMap() {
    super();
  }

  public AutoIncrementBiHashMap(AutoIncrementBiHashMap<K> other) {
    super(other);
    this.nextId = other.nextId;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Integer get(Object key) {
    if (super.containsKey(key)) {
      return super.get(key);
    }

    K genericKey = (K) key;
    Integer newValue = nextId++;

    super.put(genericKey, newValue);
    return newValue;
  }

  @Override
  public Integer put(K key, Integer value) {
    Integer oldValue = super.put(key, value);

    if (value != null && value >= nextId) {
      nextId = value + 1;
    }

    return oldValue;
  }
}
