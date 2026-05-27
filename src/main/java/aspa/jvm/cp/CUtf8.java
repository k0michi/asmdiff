package aspa.jvm.cp;

import aspa.util.SString;

/**
 * UTF8 constant values.
 * 
 * @author Eduardo Marques
 * 
 */
public final class CUtf8 extends SString implements CValue {
  public static final CUtf8 UNDEFINED = new CUtf8("__undefined__");

  public CUtf8() {
    super();
  }

  public CUtf8(String v) {
    super(v);
  }

  /**
   * Get associated constant pool tag (TAG_UTF8).
   * 
   * @return TAG_UTF8
   */
  @Override
  public int tag() {
    return TAG_Utf8;
  }
}
