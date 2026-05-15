package com.koyomiji.asmweaver.util;

import java.util.*;

public class BiHashMap<K, V> extends AbstractMap<K, V> {
  private final Map<K, V> forwardMap = new HashMap<>();
  private final Map<V, K> reverseMap = new HashMap<>();

  public BiHashMap() {
  }

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

  @Override
  public V put(K key, V value) {
    if (!canPut(key, value)) {
      // 例外メッセージのために、どちらの制約に引っかかったかを判定
      if (forwardMap.containsKey(key)) {
        throw new IllegalArgumentException(
                "Key '" + key + "' is already associated with value '" + forwardMap.get(key) + "'."
        );
      } else {
        K existingKey = reverseMap.get(value);
        throw new IllegalArgumentException(
                "Value '" + value + "' is already associated with key '" + existingKey + "'."
        );
      }
    }

    // 同一ペアの put（実質何もしない）または新規ペアの追加のみがここに来る
    V oldValue = forwardMap.put(key, value);
    reverseMap.put(value, key);
    return oldValue;
  }

  @Override
  public V get(Object key) {
    return forwardMap.get(key);
  }

  public K getKey(V value) {
    return reverseMap.get(value);
  }

  @Override
  public V remove(Object key) {
    if (forwardMap.containsKey(key)) {
      V value = forwardMap.remove(key);
      reverseMap.remove(value);
      return value;
    }
    return null;
  }

  @Override
  public int size() {
    return forwardMap.size();
  }

  @Override
  public boolean containsKey(Object key) {
    return forwardMap.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return reverseMap.containsKey(value);
  }

  @Override
  public void clear() {
    forwardMap.clear();
    reverseMap.clear();
  }

  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    return new AbstractSet<>() {
      @Override
      public Iterator<Map.Entry<K, V>> iterator() {
        Iterator<Map.Entry<K, V>> it = forwardMap.entrySet().iterator();

        return new Iterator<>() {
          private Map.Entry<K, V> currentEntry;

          @Override
          public boolean hasNext() {
            return it.hasNext();
          }

          @Override
          public Map.Entry<K, V> next() {
            currentEntry = it.next();

            return new AbstractMap.SimpleEntry<>(currentEntry.getKey(), currentEntry.getValue()) {
              @Override
              public V setValue(V value) {
                K key = getKey();
                V oldValue = getValue();

                // 1. 値が変わらない場合は正常終了（古い値を返す）
                if (Objects.equals(oldValue, value)) {
                  return oldValue;
                }

                // 2. 値が変わる場合、この制約下では必ず canPut が false になるため例外を投げる
                if (!BiHashMap.this.canPut(key, value)) {
                  if (forwardMap.containsKey(key)) {
                    throw new IllegalArgumentException(
                            "Key '" + key + "' is already associated with value '" + oldValue + "'. Cannot overwrite."
                    );
                  } else {
                    K existingKey = reverseMap.get(value);
                    throw new IllegalArgumentException(
                            "Value '" + value + "' is already associated with key '" + existingKey + "'."
                    );
                  }
                }

                // 整合性チェックを通過した場合の処理（理論上、上記の canPut で弾かれるため到達しません）
                reverseMap.remove(oldValue);
                reverseMap.put(value, key);
                currentEntry.setValue(value);
                return super.setValue(value);
              }
            };
          }

          @Override
          public void remove() {
            if (currentEntry == null) {
              throw new IllegalStateException();
            }

            reverseMap.remove(currentEntry.getValue());
            it.remove();
            currentEntry = null;
          }
        };
      }

      @Override
      public int size() {
        return forwardMap.size();
      }

      @Override
      public void clear() {
        BiHashMap.this.clear();
      }
    };
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
}