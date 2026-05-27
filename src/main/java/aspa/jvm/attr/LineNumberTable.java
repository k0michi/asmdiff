package aspa.jvm.attr;

import java.io.IOException;

import aspa.core.Context;
import aspa.core.Stream;
import aspa.jvm.JVMSymbolList;
import aspa.util.SInt;

public final class LineNumberTable 
  extends JVMSymbolList<SInt> 
  implements AttributeValue {

  public LineNumberTable() {
    super(SInt.class);
  }

  @Override
  public void read(Stream in, Context ctx) throws IOException {
    int len = in.readInt();
    super.read(in, ctx);

    if (len != size() * 4 + 2)
      throw new IOException("invalid LineNumberTable attribute");

  }

  @Override
  public void write(Stream out, Context ctx) throws IOException {
    out.writeInt(size() * 4 + 2);
    super.write(out, ctx);
  }
}
