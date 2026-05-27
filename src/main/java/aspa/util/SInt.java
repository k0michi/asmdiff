package aspa.util;

import java.io.IOException;

import aspa.core.Stream;

public class SInt extends Scalar<Integer> {
  public SInt() {
    super(0);
  }

  public SInt(int v) {
    super(v);
  }

  public void read(Stream in) throws IOException {
    set(in.readInt());
  }

  public void write(Stream out) throws IOException {
    out.writeInt(get());
  }
}