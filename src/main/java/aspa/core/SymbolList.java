package aspa.core;

import java.util.ArrayList;

public class SymbolList<S extends ComparableSymbol> extends SymbolCollection<S> {
  public SymbolList(Class<S> clazz) {
    super(clazz, new ArrayList<S>());
  }
}
