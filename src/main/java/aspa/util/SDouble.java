package aspa.util;

import java.io.IOException;

import aspa.core.Stream;

public class SDouble extends Scalar<Double> {
  public SDouble() {
    super(0.0);
  }

  public SDouble(double v) {
    super(v);
  }

  public void read(Stream in) throws IOException {
    set(in.readDouble());
  }

  public void write(Stream out) throws IOException {
    out.writeDouble(get());
  }
}