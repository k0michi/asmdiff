package com.koyomiji.asmpatch;

import java.util.Objects;

public class ValueDiffer<T> implements IDiffer<T, ValuePatch<T>> {
  @Override
  public int distance(T oldValue, T newValue) {
    return Objects.equals(oldValue, newValue) ? 0 : 1;
  }

  public ValuePatch<T> diff(T oldValue, T newValue) {
    if (Objects.equals(oldValue, newValue)) {
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
