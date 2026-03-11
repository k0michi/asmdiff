package com.koyomiji.asmpatch;

import java.util.Objects;

public class ValueKeyProvider<T> implements IKeyProvider<T, T> {
  @Override
  public T getKey(T value) {
    return value;
  }

  @Override
  public boolean compareValues(T value1, T value2) {
    return Objects.equals(value1, value2);
  }

  @Override
  public boolean compareKeys(T key1, T key2) {
    return Objects.equals(key1, key2);
  }

  @Override
  public boolean compareValueAndKey(T value, T key) {
    return Objects.equals(value, key);
  }
}
