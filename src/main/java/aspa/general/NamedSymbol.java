package aspa.general;

import java.io.IOException;

import aspa.core.Atom;
import aspa.core.Context;
import aspa.core.Stream;
import aspa.core.Record;
import aspa.core.Symbol;
import aspa.util.SString;

public class NamedSymbol<T extends Symbol> extends Record {

  private final SString name = new SString();
  private final T  symbol;

  public NamedSymbol(String name, T symbol) {
    this.name.set(name);
    this.symbol = symbol;
  }

  public final T content() {
    return symbol;
  }
  
  @Override
  public void read(Stream in, Context ctx) throws IOException {
    name.read(in);
    symbol.read(in, ctx);
  }

  @Override
  public void write(Stream out, Context ctx) throws IOException {
    name.write(out);
    symbol.write(out, ctx);
  }

  @Override
  public final Atom[] keys() {
    return new Atom[] { name };
  }

  @Override
  public Symbol[] values() {
    return new Symbol[] { symbol };
  }
}
