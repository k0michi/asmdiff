package aspa.jvm.cp;

import aspa.util.SPair;

/**
 * Class method constant.
 * 
 * An interface method constant is defined by a class and a signature (name-type
 * constant).
 * 
 * @author Eduardo Marques
 * 
 */
public final class CIMethod extends SPair<CClass, CNameAndType> implements
    CValue {
  /**
   * Constructor.
   * 
   * @param clazz
   *          class
   * @param signature
   *          signature
   */
  public CIMethod(CClass clazz, CNameAndType nat) {
    super(clazz, nat);
  }

  /**
   * Get associated constant pool tag (TAG_IMETHOD).
   * 
   * @return TAG_IMETHOD
   */
  @Override
  public int tag() {
    return TAG_IMethod;
  }
}
