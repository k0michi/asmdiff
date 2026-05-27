package aspa.jvm;

import java.util.TreeSet;

import aspa.core.ComparableSymbol;

public class JVMSymbolSet<S extends ComparableSymbol> extends
    JVMSymbolCollection<S> {
  public JVMSymbolSet(Class<S> clazz) {
    super(clazz, new TreeSet<S>());
  }
}