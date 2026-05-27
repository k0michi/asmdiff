package aspa.core;

import java.io.IOException;

/**
 * An atom is a symbol with no inner components. A patch over a atom is defined
 * by a (full representation of) another atom of the same type.
 * 
 * @author Eduardo Marques
 */
public abstract class Atom implements ComparableSymbol, Cloneable {
  /**
   * Constructor.
   */
  public Atom() {

  }

  /**
   * Test for equality with other object.
   * 
   * @param o
   *          object to check equality with.
   */
  public boolean equals(Object o) {
    return o instanceof Atom && compareTo((Symbol) o) == 0;
  }

  @Override
  public final int diff(Symbol other, Stream out, Context ctx)
      throws IOException {

    assert(out.report().line("%s vs %s", this, other));

    if (compareTo(other) == 0)
      return 0;

    write(out, ctx);

    return 1;
  }

  @Override
  public final void patch(Stream in, Context ctx) throws IOException {
    read(in, ctx);
  }

  public Symbol copy() {
    try {
      return (Symbol) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
}
