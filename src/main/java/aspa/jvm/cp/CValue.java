package aspa.jvm.cp;

import aspa.core.ComparableSymbol;

/**
 * Interface for constant pool values.
 * 
 * @author Eduardo Marques
 * 
 */
public interface CValue extends CTags, ComparableSymbol {
  /**
   * Obtain constant pool value tag. The possible values are defined in the
   * CPTags interfae.
   * 
   * @return the tag associated to a constant pool value
   */
  int tag();
}
