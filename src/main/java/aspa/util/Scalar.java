package aspa.util;

import java.io.IOException;

import aspa.core.Context;
import aspa.core.Atom;
import aspa.core.Stream;
import aspa.core.Symbol;

public abstract class Scalar<T extends Comparable<T>> extends Atom {
  protected T value;

  public Scalar() {
    value = null;
  }

  public Scalar(T value) {
    this.value = value;
  }

  @SuppressWarnings("unchecked")
  public int compareTo(Symbol other) {
    return value.compareTo(((Scalar<T>) other).value);
  }

  public String toString() {
    return value != null ? value.toString() : null;
  }

  public final T get() {
    return value;
  }

  public final void set(T value) {
    this.value = value;
  }

  @Override
  public void read(Stream in, Context ctx) throws IOException {
    read(in);
  }

  @Override
  public void write(Stream out, Context ctx) throws IOException {
    write(out);
  }

  public void read(Stream in) throws IOException {
    throw new IOException("not implemented");
  }

  public void write(Stream out) throws IOException {
    throw new IOException("not implemented");
  }

}
