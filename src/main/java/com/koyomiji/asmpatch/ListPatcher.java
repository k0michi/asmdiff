package com.koyomiji.asmpatch;

import java.util.ArrayList;
import java.util.List;

public class ListPatcher<T, U, V> implements IPatcher<List<T>, ListPatch<T, U, V>> {
  private IPatcher<T, U> patcher;

  public ListPatcher(IPatcher<T, U> patcher) {
    this.patcher = patcher;
  }

  @Override
  public List<T> patch(List<T> oldValue, ListPatch<T, U, V> patch) {
    if (!canPatch(oldValue, patch)) {
      throw new IllegalStateException("Cannot apply patch: " + patch);
    }

    var newValue = new ArrayList<T>();
    int i = 0;

    for (ListPatch.Entry<T, U, V> entry : patch.entries) {
      switch (entry.type) {
        case MATCH:
          newValue.add(patcher.patch(oldValue.get(i), entry.patch));
          i++;
          break;
        case ADD:
          newValue.add(entry.newValue);
          break;
        case REMOVE:
          i++;
          break;
      }
    }

    return newValue;
  }

  @Override
  public boolean canPatch(List<T> oldValue, ListPatch<T, U, V> patch) {
    int i = 0;

    for (ListPatch.Entry<T, U, V> entry : patch.entries) {
      switch (entry.type) {
        case MATCH, REMOVE:
          if (i >= oldValue.size()
                  || !patcher.canPatch(oldValue.get(i), entry.patch)) {
            return false;
          }

          i++;
          break;
        case ADD:
          break;
      }
    }

    return i == oldValue.size();
  }
}
