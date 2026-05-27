package aspa.jvm.cp;

/**
 * Constant pool tags.
 * 
 * @author Eduardo Marques
 * 
 */
public interface CTags {
  /**
   * Special tag to mark the start of the constant pool.
   */
  public static final int      TAG_Zero        = 0;

  /**
   * Tag for UTF8 constants.
   */
  public static final int      TAG_Utf8        = 1;

  /**
   * Tag for integer constants.
   */
  public static final int      TAG_Integer     = 3;

  /**
   * Tag for float constants.
   */
  public static final int      TAG_Float       = 4;

  /**
   * Tag for long constants.
   */
  public static final int      TAG_Long        = 5;

  /**
   * Tag for double constants.
   */
  public static final int      TAG_Double      = 6;

  /**
   * Tag for class names.
   */
  public static final int      TAG_Class       = 7;

  /**
   * Tag for strings.
   */
  public static final int      TAG_String      = 8;

  /**
   * Tag for fields.
   */
  public static final int      TAG_Field       = 9;

  /**
   * Tag for class method constants.
   */
  public static final int      TAG_Method      = 10;

  /**
   * Tag for interface method constants.
   */
  public static final int      TAG_IMethod     = 11;

  /**
   * Tag for name and type pairs.
   */
  public static final int      TAG_NameAndType = 12;

  /**
   * Array with descriptive strings for each constant pool tag.
   */
  public static final String[] TAGNAMES        = new String[] { "ZER", "UTF",
      "UND", "INT", "FLO", "LNG", "DOU", "CLZ", "STR", "FLD", "MET", "IME",
      "NAT"                                   };
}
