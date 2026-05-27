package aspa.jvm.cp;

import java.io.IOException;

import aspa.core.Context;
import aspa.core.Atom;
import aspa.core.Stream;
import aspa.core.Symbol;

/**
 * Unresolved constant.
 * 
 * @author Eduardo Marques
 * 
 */
public final class CUnresolved extends Atom implements CValue {
  private final int   tag;
  private final int[] index = new int[2];

  public CUnresolved(int tag, int idx) {
    this.tag = tag;
    index[0] = idx;
    index[1] = -1;
  }

  public CUnresolved(int tag, int idx1, int idx2) {
    this.tag = tag;
    index[0] = idx1;
    index[1] = idx2;
  }

  public int[] indexes() {
    return index;
  }

  @Override
  public int tag() {
    return tag;
  }

  @Override
  public String toString() {
    return String.format("U %d %d", index[0], index[1]);
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
  public int compareTo(Symbol arg0) {
    throw new RuntimeException("this should not be called");
  }

}
