package com.koyomiji.asmweaver;

import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

public class ListHelper {
  public static <T> List<T> orEmpty(List<T> list) {
    return list != null ? list : List.of();
  }

  public static <T> List<T> ofNullable(T element) {
    return element != null ? List.of(element) : List.of();
  }

  public static <T> List<T> ofNullableArray(T[] array) {
    return array != null ? List.of(array) : List.of();
  }

  public static <T> boolean equals(List<T> a, List<T> b, BiPredicate<T, T> compare) {
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
      if (!compare.test(a.get(i), b.get(i))) {
        return false;
      }
    }

    return true;
  }

  public static <T> boolean equals(List<T> a, List<T> b) {
    return equals(a, b, Objects::equals);
  }

  public static <T> T getOrNull(List<T> list, int index) {
    if (index < 0 || index >= list.size()) {
      return null;
    }

    return list.get(index);
  }
}
