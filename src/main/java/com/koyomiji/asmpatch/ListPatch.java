package com.koyomiji.asmpatch;

import java.util.List;

public class ListPatch<T, U> {
  public enum EntryType {
    MATCH, ADD, REMOVE
  }

  public static class Entry<T, U> {
    public EntryType type;
    public T newValue;
    public U patch;

    public Entry(EntryType type, T newValue, U patch) {
      this.type = type;
      this.newValue = newValue;
      this.patch = patch;
    }

    public static <T, U> Entry<T, U> match(U patch) {
      return new Entry<>(EntryType.MATCH, null, patch);
    }

    public static <T, U> Entry<T, U> add(T value) {
      return new Entry<>(EntryType.ADD, value, null);
    }

    public static <T, U> Entry<T, U> remove() {
      return new Entry<>(EntryType.REMOVE, null, null);
    }
  }

  public List<Entry<T, U>> entries;

  public ListPatch(List<Entry<T, U>> entries) {
    this.entries = entries;
  }
}
