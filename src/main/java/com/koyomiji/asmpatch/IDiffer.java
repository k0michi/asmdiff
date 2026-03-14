package com.koyomiji.asmpatch;

public interface IDiffer<T, U extends IPatch<T>> {
  U diff(T oldValue, T newValue);
  int distance(T oldValue, T newValue);
  boolean canMatch(T oldValue, T newValue);
}
