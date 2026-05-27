package aspa.util;

public class Pair<A extends Comparable<? super A>, B extends Comparable<? super B>>
    implements Comparable<Pair<A, B>> {
  public A first;
  public B second;

  public Pair() {

  }

  public Pair(A a, B b) {
    this.first = a;
    this.second = b;
  }

  @SuppressWarnings("unchecked")
  public boolean equals(Object o) {
    return compareTo((Pair<A, B>) o) == 0;
  }

  public int compareTo(Pair<A, B> other) {

    int c = first.compareTo(other.first);

    if (c == 0)
      c = second.compareTo(other.second);

    return c;
  }

  public String toString() {
    return String.format("(%s,%s)", first, second);
  }
}
