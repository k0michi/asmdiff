package com.koyomiji.asmpatch;

public class ValuePatch<T> implements IPatch<T> {
  public boolean changed;
  public T oldValue;
  public T newValue;

  public ValuePatch(boolean changed, T oldValue, T newValue) {
    this.changed = changed;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  public static <T> ValuePatch<T> unchanged() {
    return new ValuePatch<>(false, null, null);
  }

  public static <T> ValuePatch<T> changed(T oldValue, T newValue) {
    return new ValuePatch<>(true, oldValue, newValue);
  }
}
