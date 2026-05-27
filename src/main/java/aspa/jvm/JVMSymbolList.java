package aspa.jvm;

import java.util.ArrayList;

import aspa.core.ComparableSymbol;

public class JVMSymbolList<S extends ComparableSymbol> extends
    JVMSymbolCollection<S> {
  public JVMSymbolList(Class<S> clazz) {
    super(clazz, new ArrayList<S>());
  }
}
