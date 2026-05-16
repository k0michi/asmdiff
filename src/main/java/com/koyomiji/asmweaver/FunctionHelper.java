package com.koyomiji.asmweaver;

import java.util.function.Function;

public class FunctionHelper {
  public static <T, R> Function<T, R> throwIfInvokedFunction() {
    return x -> {
      throw new AssertionError("This function should not be called");
    };
  }
}
