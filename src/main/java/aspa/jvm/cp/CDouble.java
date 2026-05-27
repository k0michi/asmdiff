package aspa.jvm.cp;

import aspa.util.SDouble;

/**
 * Double constant.
 * 
 * @author Eduardo Marques
 * 
 */
public final class CDouble extends SDouble implements CValue {
  public CDouble(double v) {
    super(v);
  }

  /**
   * Get associated constant pool tag (TAG_DOUBLE).
   * 
   * @return TAG_DOUBLE
   */
  @Override
  public int tag() {
    return TAG_Double;
  }
}
