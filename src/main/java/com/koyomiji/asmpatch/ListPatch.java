package com.koyomiji.asmpatch;

import java.util.List;

public class ListPatch<T, U, V> {
  public enum EntryType {
    MATCH, ADD, REMOVE
  }

  public static class Entry<T, U, V> {
    public EntryType type;
    public T newValue;
    public U patch;

    public Entry(EntryType type, T newValue, U patch) {
      this.type = type;
      this.newValue = newValue;
      this.patch = patch;
    }

    public static <T, U, V> Entry<T, U, V> match(U patch) {
      return new Entry<>(EntryType.MATCH, null, patch);
    }

    public static <T, U, V> Entry<T, U, V> add(T value) {
      return new Entry<>(EntryType.ADD, value, null);
    }

    public static <T, U, V> Entry<T, U, V> remove() {
      return new Entry<>(EntryType.REMOVE, null, null);
    }
  }

  public List<Entry<T, U, V>> entries;

  public ListPatch(List<Entry<T, U, V>> entries) {
    this.entries = entries;
  }
}
