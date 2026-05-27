package aspa.jvm.attr;

import java.io.IOException;

import aspa.core.Context;
import aspa.core.Stream;
import aspa.util.SByte;

public class ZeroLengthAttribute extends SByte implements AttributeValue {
  
  // TODO: this class should not extend SByte
  public ZeroLengthAttribute() {
    super((byte) 0);
  }
  
  @Override
  public void read(Stream in, Context ctx) throws IOException {
    if (in.readInt() != 0)
      throw new InvalidAttributeLengthException();
  }

  @Override
  public void write(Stream out, Context ctx) throws IOException {
    out.writeInt(0);
  }
}
