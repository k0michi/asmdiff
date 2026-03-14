package com.koyomiji.asmpatch;

import java.util.Objects;

/**
 * An equator using Objects.equals.
 */
public class ObjectEquator<T> implements IEquator<T> {
  @Override
  public boolean equals(Object a, Object b) {
    return Objects.equals(a, b);
  }
}
