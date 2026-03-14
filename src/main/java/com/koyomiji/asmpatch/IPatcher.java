package com.koyomiji.asmpatch;

public interface IPatcher<T, U> {
  T patch(T oldValue, U patch);
  boolean canPatch(T oldValue, U patch);
}
