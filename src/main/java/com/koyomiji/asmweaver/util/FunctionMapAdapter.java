package com.koyomiji.asmweaver.util;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class FunctionMapAdapter<K, V> implements Map<K, V> {
  private final Function<K, V> function;

  public FunctionMapAdapter(Function<K, V> function) {
    this.function = Objects.requireNonNull(function, "Function cannot be null");
  }

  @SuppressWarnings("unchecked")
  @Override
  public V get(Object key) {
    // キーの型が合わない場合は ClassCastException が発生します
    return function.apply((K) key);
  }

  @SuppressWarnings("unchecked")
  @Override
  public V getOrDefault(Object key, V defaultValue) {
    // 独自の getOrDefault 実装。
    // インターフェースのデフォルト実装は containsKey() を呼ぶため、オーバーライドが必須です。
    try {
      V value = function.apply((K) key);
      return (value != null) ? value : defaultValue;
    } catch (ClassCastException e) {
      return defaultValue;
    }
  }

  // --- 以下、サポートしない操作（すべて例外をスロー） ---

  @Override
  public int size() {
    throw new UnsupportedOperationException("size() is not supported.");
  }

  @Override
  public boolean isEmpty() {
    throw new UnsupportedOperationException("isEmpty() is not supported.");
  }

  @Override
  public boolean containsKey(Object key) {
    throw new UnsupportedOperationException("containsKey() is not supported.");
  }

  @Override
  public boolean containsValue(Object value) {
    throw new UnsupportedOperationException("containsValue() is not supported.");
  }

  @Override
  public V put(K key, V value) {
    throw new UnsupportedOperationException("put() is not supported.");
  }

  @Override
  public V remove(Object key) {
    throw new UnsupportedOperationException("remove() is not supported.");
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    throw new UnsupportedOperationException("putAll() is not supported.");
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException("clear() is not supported.");
  }

  @Override
  public Set<K> keySet() {
    throw new UnsupportedOperationException("keySet() is not supported.");
  }

  @Override
  public Collection<V> values() {
    throw new UnsupportedOperationException("values() is not supported.");
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    throw new UnsupportedOperationException("entrySet() is not supported.");
  }
}