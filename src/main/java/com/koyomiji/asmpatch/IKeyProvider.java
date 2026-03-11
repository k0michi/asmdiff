package com.koyomiji.asmpatch;

public interface IKeyProvider<T, U> {
  U getKey(T value);

  boolean compareValues(T value1, T value2);

  boolean compareKeys(U key1, U key2);

  boolean compareValueAndKey(T value, U key);
}
