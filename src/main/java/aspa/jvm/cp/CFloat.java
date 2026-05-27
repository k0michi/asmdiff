package aspa.jvm.cp;

import aspa.util.SFloat;

/**
 * Float constant.
 * 
 * @author Eduardo Marques
 * 
 */
public final class CFloat extends SFloat implements CValue {
  public CFloat(float v) {
    super(v);
  }

  /**
   * Get associated constant pool tag (TAG_FLOAT).
   * 
   * @return TAG_FLOAT
   */
  @Override
  public int tag() {
    return TAG_Float;
  }
}
