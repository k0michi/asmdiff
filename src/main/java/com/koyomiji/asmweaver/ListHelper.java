package com.koyomiji.asmweaver;

import com.koyomiji.asmpatch.IEquator;

import java.util.List;
import java.util.Objects;

public class ListHelper {
  public static <T> List<T> orEmpty(List<T> list) {
    return list != null ? list : List.of();
  }

  public static<T> List<T> ofNullable(T element) {
    return element != null ? List.of(element) : List.of();
  }

  public static <T> boolean equals(List<T> a, List<T> b, IEquator<T> equator) {
    if (a == b) {
      return true;
    }

    if (a == null || b == null) {
      return false;
    }

    if (a.size() != b.size()) {
      return false;
    }

    for (int i = 0; i < a.size(); i++) {
      if (!equator.equals(a.get(i), b.get(i))) {
        return false;
      }
    }

    return true;
  }

  public static <T> boolean equals(List<T> a, List<T> b) {
    return equals(a, b, Objects::equals);
  }
}
