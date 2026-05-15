package com.koyomiji.asmweaver;

import java.util.function.Function;

public class NullableHelper {
  public static <T, U> U map(T value, Function<T, U> mapper) {
    if (value == null) {
      return null;
    }
    return mapper.apply(value);
  }
}
