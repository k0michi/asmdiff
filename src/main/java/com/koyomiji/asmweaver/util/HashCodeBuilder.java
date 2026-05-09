package com.koyomiji.asmweaver.util;

import java.util.Objects;
import java.util.function.ToIntFunction;

public class HashCodeBuilder {
  private int hash = 1;
  private static final int multiplier = 31;

  private <T> int hashCode(T value, ToIntFunction<T> hashFunction) {
    return value != null ? hashFunction.applyAsInt(value) : 0;
  }

  public <T> HashCodeBuilder append(T value, ToIntFunction<T> hashFunction) {
    hash = multiplier * hash + hashCode(value, hashFunction);
    return this;
  }

  public <T> HashCodeBuilder append(T value) {
    hash = multiplier * hash + hashCode(value, Objects::hashCode);
    return this;
  }

  public int build() {
    return hash;
  }
}
