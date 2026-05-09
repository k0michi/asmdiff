package com.koyomiji.asmweaver.util;

import com.koyomiji.asmweaver.util.tuple.Pair;

import java.util.Map;
import java.util.function.BiFunction;

public class BiFunctionHelper {
  public static <K1, K2, V> BiFunction<K1, K2, V> fromMap(Map<Pair<K1, K2>, V> map) {
    return (k1, k2) -> map.get(new Pair<>(k1, k2));
  }

  public static <K2, V> BiFunction<V, K2, V> first() {
    return (k1, k2) -> k1;
  }

  public static <K1, V> BiFunction<K1, V, V> second() {
    return (k1, k2) -> k2;
  }
}
