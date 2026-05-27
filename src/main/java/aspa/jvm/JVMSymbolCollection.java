package aspa.jvm;

import java.io.IOException;
import java.util.Collection;

import aspa.core.ComparableSymbol;
import aspa.core.Context;
import aspa.core.Stream;
import aspa.core.SymbolCollection;
import aspa.jvm.cp.CValue;
import aspa.jvm.cp.ConstantPool;
import aspa.util.Misc;

public class JVMSymbolCollection<S extends ComparableSymbol> extends
    SymbolCollection<S> {
  public JVMSymbolCollection(Class<S> clazz, Collection<S> coll) {
    super(clazz, coll);
  }

  @Override
  public void read(Stream in, Context ctx) throws IOException {
    clear();

    ConstantPool cp = (ConstantPool) ctx;
    boolean isCPValue = CValue.class.isAssignableFrom(symbolClass());
    int n = in.readUnsignedShort();
    clear();

    while (n != 0) {
      S symbol;
      if (isCPValue) {
        symbol = symbolClass().cast(cp.get(in.readUnsignedShort()));
      } else {
        symbol = Misc.create(symbolClass());
        symbol.read(in, cp);
      }
      add(symbol);
      --n;
    }
  }

  @Override
  public void write(Stream out, Context ctx) throws IOException {
    ConstantPool cp = (ConstantPool) ctx;
    boolean isCPValue = CValue.class.isAssignableFrom(symbolClass());
    out.writeShort(size());

    for (S symbol : this) {
      if (isCPValue)
        out.writeShort(cp.get((CValue) symbol));
      else
        symbol.write(out, cp);
    }
  }
}
