package aspa.util;

import java.io.IOException;

import aspa.core.Stream;

public class SFloat extends Scalar<Float> {
  public SFloat() {
    super(0.0f);
  }

  public SFloat(float f) {
    super(f);
  }

  public void read(Stream in) throws IOException {
    set(in.readFloat());
  }

  public void write(Stream out) throws IOException {
    out.writeFloat(get());
  }
}