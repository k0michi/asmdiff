package aspa.util;

import java.io.IOException;

import aspa.core.Stream;

public class SByte extends Scalar<Byte> {
  public SByte() {

  }

  public SByte(byte b) {
    super(b);
  }

  public void read(Stream in) throws IOException {
    set(in.readByte());
  }

  public void write(Stream out) throws IOException {
    out.writeByte(get());
  }
}