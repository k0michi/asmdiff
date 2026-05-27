package aspa.util;

import java.io.IOException;

import aspa.core.Stream;

public class SString extends Scalar<String> {
  public SString() {
    super("");
  }

  public SString(String v) {
    super(v);
  }

  public void read(Stream in) throws IOException {
    set(in.readUTF());
  }

  public void write(Stream out) throws IOException {
    out.writeUTF(get());
  }
}