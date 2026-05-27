package aspa.jvm.attr;

import java.io.IOException;

import aspa.core.ComparableSymbol;
import aspa.core.Context;
import aspa.core.Stream;
import aspa.jvm.JVMSymbolSet;

public abstract class EntrySet<S extends ComparableSymbol> 
  extends JVMSymbolSet<S> 
  implements AttributeValue {
  
  public EntrySet(Class<S> clazz) {
    super(clazz);
  }
  
  public abstract int entryLength();
  
  @Override
  public final void read(Stream in, Context ctx) throws IOException {
    int len = in.readInt();

    super.read(in, ctx);

    // System.out.printf("LEN %d %d %d\n", cpLength, len, size()*cpLength + 2);
    if (size() * entryLength() + 2 != len)
      throw new InvalidAttributeLengthException();
  }

  @Override
  public final void write(Stream out, Context ctx) throws IOException {
    out.writeInt(size() * entryLength() + 2);
    super.write(out, ctx);
  }
}
