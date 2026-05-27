package aspa.jvm.cp;

import aspa.util.SString;

/**
 * Class constant.
 * 
 * @author Eduardo Marques
 * 
 */
public final class CClass extends SString implements CValue {

  public static final CClass UNDEFINED = new CClass(CUtf8.UNDEFINED);

  public CClass() {
    super("");
  }

  public CClass(CUtf8 v) {
    super(v.get());
  }

  /**
   * Get associated constant pool tag (TAG_CLASS).
   * 
   * @return TAG_CLASS
   */
  @Override
  public int tag() {
    return TAG_Class;
  }
}
