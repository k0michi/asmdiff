package com.koyomiji.asmweaver;

import java.util.Iterator;
import java.util.function.Supplier;

public class IteratorHelper {
  public static <T> T nextOrThrow(Iterator<T> iterator, Supplier<? extends RuntimeException> exceptionSupplier) {
    if (iterator.hasNext()) {
      return iterator.next();
    } else {
      throw exceptionSupplier.get();
    }
  }

  public static <T> void throwIfNext(Iterator<T> iterator, Supplier<? extends RuntimeException> exceptionSupplier) {
    if (iterator.hasNext()) {
      throw exceptionSupplier.get();
    }
  }
}
