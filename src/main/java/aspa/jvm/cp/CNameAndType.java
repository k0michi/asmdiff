package aspa.jvm.cp;

import aspa.util.SPair;

/**
 * Name-and-type (signature) constant.
 * 
 * A name-and-type constant is defined by two UTF-8 constants, one for the name
 * and another for the type.
 * 
 * @author Eduardo Marques
 * 
 */
public final class CNameAndType extends SPair<CUtf8, CUtf8> implements CValue {
  /**
   * Constructor.
   * 
   * @param name
   *          name constant
   * @param type
   *          type constant
   */
  CNameAndType(CUtf8 name, CUtf8 type) {
    super(name, type);
  }

  /**
   * Get associated constant pool tag (TAG_NAT).
   * 
   * @return TAG_NAT
   */
  @Override
  public int tag() {
    return TAG_NameAndType;
  }
}
