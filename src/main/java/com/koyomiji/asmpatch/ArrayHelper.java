package com.koyomiji.asmpatch;

public class ArrayHelper {
  public static <T> boolean equals(T[] a, T[] b, IEquator<T> equator) {
    if (a == b) {
      return true;
    }

    if (a == null || b == null) {
      return false;
    }

    if (a.length != b.length) {
      return false;
    }

    for (int i = 0; i < a.length; i++) {
      if (!equator.equals(a[i], b[i])) {
        return false;
      }
    }

    return true;
  }
}
