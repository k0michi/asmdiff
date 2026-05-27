package aspa.util;

import java.io.IOException;

import aspa.core.Stream;

public class SShort extends Scalar<Short> {
  @Override
  public void read(Stream in) throws IOException {
    set(in.readShort());
  }

  @Override
  public void write(Stream out) throws IOException {
    out.writeShort(get());
  }
}