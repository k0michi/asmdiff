package aspa.general;

import aspa.core.ComparableSymbol;
import aspa.core.Symbol;
import aspa.core.SymbolSet;

public class Archive extends NamedSymbol<SymbolSet<ComparableSymbol>> {
  public Archive(String name) {
    super(name, new SymbolSet<ComparableSymbol>(ComparableSymbol.class));
  }
}
