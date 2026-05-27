package aspa.util;

import java.io.IOException;

import aspa.core.Stream;

public class SPair<A extends Scalar<?>, B extends Scalar<?>> extends
    Scalar<Pair<A, B>> {
  public SPair(A a, B b) {
    super(new Pair<A, B>(a, b));
  }

  public void read(Stream in) throws IOException {
    Pair<A, B> pair = get();
    pair.first.read(in);
    pair.second.read(in);
  }

  public void write(Stream out) throws IOException {
    Pair<A, B> pair = get();
    pair.first.write(out);
    pair.second.write(out);
  }
}