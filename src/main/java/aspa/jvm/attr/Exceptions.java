package aspa.jvm.attr;

import aspa.jvm.cp.CClass;

public final class Exceptions extends EntrySet<CClass> {
  
  public Exceptions() {
    super(CClass.class);
  }
  
  public int entryLength() {
    return 2;
  }
}
