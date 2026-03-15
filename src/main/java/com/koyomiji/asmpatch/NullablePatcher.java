package com.koyomiji.asmpatch;

import java.util.ArrayList;
import java.util.List;

public class NullablePatcher<T, U> implements IPatcher<T, NullablePatch<T, U>> {
  private final IPatcher<T, U> patcher;

  public NullablePatcher(IPatcher<T, U> patcher) {
    this.patcher = patcher;
  }

  @Override
  public T patch(T oldValue, NullablePatch<T, U> patch) {
    if (!canPatch(oldValue, patch)) {
      throw new IllegalStateException("Cannot apply patch: " + patch);
    }

    List<T> oldList = oldValue == null ? List.<T>of() : List.of(oldValue);
    List<T> newList = new ArrayList<>();
    int i = 0;

    for (NullablePatch.Entry<T, U> entry : patch.entries) {
      switch (entry.type) {
        case MATCH:
          newList.add(patcher.patch(oldList.get(i), entry.patch));
          i++;
          break;
        case ADD:
          newList.add(entry.newValue);
          break;
        case REMOVE:
          i++;
          break;
      }
    }

    return newList.isEmpty() ? null : newList.get(0);
  }

  @Override
  public boolean canPatch(T oldValue, NullablePatch<T, U> patch) {
    int i = 0;
    var oldList = oldValue == null ? List.<T>of() : List.of(oldValue);

    for (NullablePatch.Entry<T, U> entry : patch.entries) {
      switch (entry.type) {
        case MATCH, REMOVE:
          if (i >= oldList.size()
                  || !patcher.canPatch(oldList.get(i), entry.patch)) {
            return false;
          }
          i++;
          break;
        case ADD:
          break;
      }
    }

    return i == oldList.size();
  }
}
