package com.koyomiji.asmweaver.util.tuple;

import java.util.Objects;

public class Triplet<A, B, C> {
  public final A first;
  public final B second;
  public final C third;

  private Triplet(A first, B second, C third) {
    this.first = first;
    this.second = second;
    this.third = third;
  }

  public static <A, B, C> Triplet<A, B, C> of(A first, B second, C third) {
    return new Triplet<>(first, second, third);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Triplet<?, ?, ?> triplet = (Triplet<?, ?, ?>) o;
    return Objects.equals(first, triplet.first) && Objects.equals(second, triplet.second) && Objects.equals(third, triplet.third);
  }

  @Override
  public int hashCode() {
    return Objects.hash(first, second, third);
  }
}
