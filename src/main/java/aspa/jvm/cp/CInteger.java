package aspa.jvm.cp;

import aspa.util.SInt;

/**
 * Integer constant.
 * 
 * @author Eduardo Marques
 * 
 */
public class CInteger extends SInt implements CValue {
  public CInteger(int v) {
    super(v);
  }

  /**
   * Get associated constant pool tag (TAG_INTEGER).
   * 
   * @return TAG_INTEGER
   */
  @Override
  public int tag() {
    return TAG_Integer;
  }
}
