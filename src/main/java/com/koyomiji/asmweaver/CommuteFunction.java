package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.tuple.Pair;

@FunctionalInterface
public interface CommuteFunction<Diff> {
  Pair<Diff, Diff> commute(Diff diff1, Diff diff2) throws ConflictException;
}
