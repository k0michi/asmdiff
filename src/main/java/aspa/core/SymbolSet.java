package aspa.core;

import java.util.TreeSet;

public class SymbolSet<S extends ComparableSymbol> extends SymbolCollection<S> {
  public SymbolSet(Class<S> clazz) {
    super(clazz, new TreeSet<S>());
  }
}
