package com.koyomiji.asmpatch;

import java.util.Objects;

public class ValueDiffer<T> implements IDiffer<T, ValuePatch<T>> {
  private IEquator<T> equator;

  public ValueDiffer() {
    this(new ObjectEquator<>());
  }

  public ValueDiffer(IEquator<T> equator) {
    this.equator = equator;
  }

  @Override
  public int distance(T oldValue, T newValue) {
    return equator.equals(oldValue, newValue) ? 0 : 1;
  }

  public ValuePatch<T> diff(T oldValue, T newValue) {
    if (equator.equals(oldValue, newValue)) {
      return ValuePatch.unchanged();
    } else {
      return ValuePatch.changed(oldValue, newValue);
    }
  }

  @Override
  public boolean canMatch(T oldValue, T newValue) {
    return true;
  }
}
