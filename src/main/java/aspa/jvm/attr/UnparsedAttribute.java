package aspa.jvm.attr;

import java.io.IOException;

import aspa.core.Context;
import aspa.core.Stream;
import aspa.core.SymbolList;
import aspa.util.SByte;

public final class UnparsedAttribute 
extends SymbolList<SByte> implements AttributeValue {
 
  public UnparsedAttribute() {
    super(SByte.class);
  }

  @Override
  public void read(Stream in, Context ctx) throws IOException {
    clear();

    int len = in.readInt();

    if (len < 0) {
      throw new InvalidAttributeLengthException();
    }
    
    while (len != 0) {
      add(new SByte(in.readByte()));
      --len;
    }
  }

  @Override
  public void write(Stream out, Context ctx) throws IOException {
    out.writeInt(size());

    for (SByte b : this) {
      out.writeByte(b.get());
    }
  }
}
