package com.koyomiji.asmpatch;

import java.util.Objects;

public class ValuePatcher<T> implements IPatcher<T, ValuePatch<T>> {
  @Override
  public T patch(T oldValue, ValuePatch<T> patch) {
    if (!canPatch(oldValue, patch)) {
      throw new IllegalStateException("Cannot apply patch: " + patch);
    }

    return patch.changed ? patch.newValue : oldValue;
  }

  @Override
  public boolean canPatch(T oldValue, ValuePatch<T> patch) {
    return true;
  }
}
