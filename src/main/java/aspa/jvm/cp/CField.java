package aspa.jvm.cp;

import aspa.util.SPair;

/**
 * Field constant.
 * 
 * A field constant is defined by a class and a signature (a name-type pair).
 * 
 * @author Eduardo Marques
 * 
 */
public final class CField extends SPair<CClass, CNameAndType> implements
    CValue {
  /**
   * Constructor.
   * 
   * @param clazz
   *          field class
   * @param signature
   *          field signature
   */
  public CField(CClass clazz, CNameAndType signature) {
    super(clazz, signature);
  }

  /**
   * Get associated constant pool tag (always TAG_Field).
   * 
   * @return TAG_FIELD
   */
  @Override
  public int tag() {
    return TAG_Field;
  }
}
