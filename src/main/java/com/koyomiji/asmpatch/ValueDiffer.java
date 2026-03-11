package com.koyomiji.asmpatch;

import java.util.Objects;

public class ValueDiffer<T> implements IDiffer<T, ValuePatch<T>> {
  @Override
  public boolean canDiff(T oldValue, T newValue) {
    return Objects.equals(oldValue, newValue);
  }

  public ValuePatch<T> diff(T oldValue, T newValue) {
    if (Objects.equals(oldValue, newValue)) {
      return ValuePatch.unchanged();
    } else {
      return ValuePatch.changed(oldValue, newValue);
    }
  }
}
