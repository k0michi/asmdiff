package com.koyomiji.asmpatch;

import java.util.List;

public class NullablePatch<T, U> {
  public enum EntryType {
    MATCH, ADD, REMOVE
  }

  public static class Entry<T, U> {
    public NullablePatch.EntryType type;
    public T newValue;
    public U patch;

    public Entry(NullablePatch.EntryType type, T newValue, U patch) {
      this.type = type;
      this.newValue = newValue;
      this.patch = patch;
    }

    public static <T, U> NullablePatch.Entry<T, U> match(U patch) {
      return new NullablePatch.Entry<>(NullablePatch.EntryType.MATCH, null, patch);
    }

    public static <T, U> NullablePatch.Entry<T, U> add(T value) {
      return new NullablePatch.Entry<>(NullablePatch.EntryType.ADD, value, null);
    }

    public static <T, U> NullablePatch.Entry<T, U> remove() {
      return new NullablePatch.Entry<>(NullablePatch.EntryType.REMOVE, null, null);
    }
  }

  public List<NullablePatch.Entry<T, U>> entries;

  public NullablePatch(List<NullablePatch.Entry<T, U>> entries) {
    this.entries = entries;
  }
}
