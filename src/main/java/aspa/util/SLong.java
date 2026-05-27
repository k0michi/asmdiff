package aspa.util;

import java.io.IOException;

import aspa.core.Stream;

public class SLong extends Scalar<Long> {
  public SLong() {
    super(0L);
  }

  public SLong(long v) {
    super(v);
  }

  public void read(Stream in) throws IOException {
    set(in.readLong());
  }

  public void write(Stream out) throws IOException {
    out.writeLong(get());
  }
}