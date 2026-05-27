package aspa.jvm.cp;

import aspa.util.SPair;

/**
 * Class method constant.
 * 
 * A class method constant is defined by a class and a signature (name-type
 * constant).
 * 
 * @author Eduardo Marques
 * 
 */
public final class CMethod extends SPair<CClass, CNameAndType> implements
    CValue {
  /**
   * Constructor.
   * 
   * @param clazz
   *          class
   * @param signature
   *          signature
   */
  public CMethod(CClass clazz, CNameAndType signature) {
    super(clazz, signature);
  }

  /**
   * Get associated constant pool tag (always TAG_METHOD).
   * 
   * @return TAG_METHOD
   */
  @Override
  public int tag() {
    return TAG_Method;
  }
}
