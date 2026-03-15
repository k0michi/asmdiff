package com.koyomiji.asmpatch;

public interface IDiffer<T, U> {
  /**
   * Calculate the patch to transform oldValue into newValue.
   * @param oldValue
   * @param newValue
   * @return
   */
  U diff(T oldValue, T newValue);

  /**
   * Calculate the distance between two values.
   * @param oldValue
   * @param newValue
   * @return
   */
  int distance(T oldValue, T newValue);

  /**
   * Determine whether the two values can be matched. If this method returns false, the diffing process will treat them as completely different and won't attempt to compute a patch.
   * @param oldValue
   * @param newValue
   * @return
   */
  boolean canMatch(T oldValue, T newValue);
}
