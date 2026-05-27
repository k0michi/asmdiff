package aspa.jvm.cp;

import aspa.util.SString;

/**
 * String constants.
 * 
 * @author Eduardo Marques
 * 
 */
public final class CString extends SString implements CValue {
  public CString(CUtf8 v) {
    super(v.get());
  }

  /**
   * Get associated constant pool tag (TAG_STRING).
   * 
   * @return TAG_STRING
   */
  @Override
  public int tag() {
    return TAG_String;
  }
}
