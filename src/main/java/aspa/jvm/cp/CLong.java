package aspa.jvm.cp;

import aspa.util.SLong;

/**
 * Long constant.
 * 
 * @author Eduardo Marques
 * 
 */
public class CLong extends SLong implements CValue {
  public CLong(long v) {
    super(v);
  }

  /**
   * Get associated constant pool tag (TAG_LONG).
   * 
   * @return TAG_LONG
   */
  @Override
  public int tag() {
    return TAG_Long;
  }
}
