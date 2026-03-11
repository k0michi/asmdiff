package com.koyomiji.asmpatch;

import java.util.List;

public class ListPatch<T, U extends IPatch<T>, V> implements IPatch<List<T>> {
  @Override
  public boolean isSame() {
    return entries.stream().allMatch(entry -> entry.type == EntryType.MATCH);
  }

  public enum EntryType {
    MATCH, ADD, REMOVE
  }

  public static class Entry<T, U extends IPatch<T>, V> {
    public EntryType type;
    public V key;
    public T newValue;
    public U patch;

    public Entry(EntryType type, V key, T newValue, U patch) {
      this.type = type;
      this.key = key;
      this.newValue = newValue;
      this.patch = patch;
    }

    public static <T, U extends IPatch<T>, V> Entry<T, U, V> match(V key, U patch) {
      return new Entry<>(EntryType.MATCH, key, null, patch);
    }

    public static <T, U extends IPatch<T>, V> Entry<T, U, V> add(T value) {
      return new Entry<>(EntryType.ADD, null, value, null);
    }

    public static <T, U extends IPatch<T>, V> Entry<T, U, V> remove(V key) {
      return new Entry<>(EntryType.REMOVE, key, null, null);
    }
  }

  public List<Entry<T, U, V>> entries;

  public ListPatch(List<Entry<T, U, V>> entries) {
    this.entries = entries;
  }
}
