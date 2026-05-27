package aspa.jvm.cp;

import java.io.IOException;

import aspa.core.Context;
import aspa.core.Atom;
import aspa.core.Stream;
import aspa.core.Symbol;

/**
 * Convenience class to represent "index gaps" in the constant pool.
 * 
 * The index gaps are defined for long and double constants, which take up two
 * logical entries rather than one for all other types of constants. This is a
 * well-known and legacy design flaw of the JVM format.
 * 
 * The "gap objects" become necessary due to ordering and comparison purposes,
 * allowing an easier implementation of several aspects of this package.
 * 
 * @author Eduardo Marques
 * 
 */
public class CGap extends Atom implements CValue {
  private final CValue dup;

  public CGap(CValue dup) {
    this.dup = dup;
  }

  @Override
  public int tag() {
    return dup.tag();
  }

  @Override
  public String toString() {
    return String.format("GAP < %s > ", dup);
  }

  public CValue actual() {
    return dup;
  }

  @Override
  public void read(Stream in, Context ctx) throws IOException {
    // TODO: FIX
    // throw new RuntimeException("this should not be called");
  }

  @Override
  public void write(Stream out, Context ctx) throws IOException {
    throw new RuntimeException("this should not be called");
  }

  @Override
  public int compareTo(Symbol arg0) {
    throw new RuntimeException("this should not be called");
  }
}
