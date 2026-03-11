package com.koyomiji.asmpatch;

public interface IPatcher<T, U extends IPatch<T>> {
  T patch(T oldValue, U patch);
  boolean canPatch(T oldValue, U patch);
}
