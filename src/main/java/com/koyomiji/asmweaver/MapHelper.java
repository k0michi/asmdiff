package com.koyomiji.asmweaver;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;

public class MapHelper {
  public static <T, U> boolean putIfAbsentAndTest(Map<T, U> map, T key, U value) {
    return putIfAbsentAndTest(map, key, value, Objects::equals);
  }

  public static <T, U> boolean putIfAbsentAndTest(Map<T, U> map, T key, U value, BiPredicate<U, U> valueEquals) {
    U oldValue = map.putIfAbsent(key, value);
    return oldValue == null || valueEquals.test(value, oldValue);
  }
}
