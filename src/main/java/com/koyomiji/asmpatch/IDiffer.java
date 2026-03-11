package com.koyomiji.asmpatch;

public interface IDiffer<T, U extends IPatch<T>> {
  /**
   * Determine if the two values can be diffed, i.e., they are uniquely identifiable and can be compared for changes.
   * @param oldValue
   * @param newValue
   * @return
   */
  boolean canDiff(T oldValue, T newValue);
  U diff(T oldValue, T newValue);
}
