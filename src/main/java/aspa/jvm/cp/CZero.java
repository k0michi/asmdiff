package aspa.jvm.cp;

import java.io.IOException;

import aspa.core.Context;
import aspa.core.Atom;
import aspa.core.Stream;
import aspa.core.Symbol;

/**
 * Dummy class to mark the start of the constant pool. This is required to deal
 * with some technicalities in the VM format regarding the constant pool.
 * 
 * @author Eduardo Marques
 * 
 */
public final class CZero extends Atom implements CValue {
  /**
   * Constructor.
   */
  public CZero() {

  }

  /**
   * Get associated constant pool tag (the special value TAG_ZERO).
   * 
   * @return TAG_ZERO
   */
  public int tag() {
    return TAG_Zero;
  }

  @Override
  public String toString() {
    return "0";
  }

  @Override
  public void read(Stream in, Context ctx) throws IOException {
    throw new RuntimeException("this should not be called");
  }

  @Override
  public void write(Stream out, Context ctx) throws IOException {
    throw new RuntimeException("this should not be called");
  }

  @Override
  public int compareTo(Symbol s) {
    return s instanceof CZero ? 0 : -1;
  }

}
