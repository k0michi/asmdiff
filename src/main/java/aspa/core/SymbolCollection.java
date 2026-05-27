package aspa.core;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import aspa.util.Misc;

//TODO: FIX
public class SymbolCollection<S extends ComparableSymbol> implements Symbol,
    Collection<S> {
  /**
   * The actual collection object.
   */
  private final Collection<S> coll;

  /**
   * Class object for the type of contained symbols.
   */
  private final Class<S>      symbolClass;

  /**
   * Constructor.
   * 
   * @param symbolClass
   *          Symbol class
   * @param coll
   *          Collection object to use.
   */
  public SymbolCollection(Class<S> symbolClass, Collection<S> coll) {
    this.symbolClass = symbolClass;
    this.coll = coll;
  }

  @SuppressWarnings("unchecked")
  @Override
  public int diff(Symbol other, Stream out, Context ctx)
      throws IOException {

    SymbolCollection<S> oth = (SymbolCollection<S>) other;

    assert (out.report().beginSection(this, "<%s> %d -> %d ",
        symbolClass.getSimpleName(), oth.size(), size()));

    int d = LCS.diff(out, symbolClass, ctx, oth.asArray(), asArray());

    assert (out.report().endSection());

    return d;
  }

  public Class<S> symbolClass() {
    return symbolClass;
  }

  public void patch(Stream in, Context ctx) throws IOException {
    assert (in.report().beginSection(this, "<%s> %d",
        symbolClass.getSimpleName(), size()));

    S[] newS = LCS.patch(in, ctx, asArray(), symbolClass);

    clear();

    for (S symb : newS)
      coll.add(symb);

    assert (in.report().endSection());
  }

  @Override
  public void read(Stream in, Context ctx) throws IOException {
    clear();

    int len = in.readInt();

    for (int i = 0; i < len; ++i) {
      S symb = Misc.create(symbolClass);
      symb.read(in, ctx);
      add(symb);
    }
  }

  @Override
  public void write(Stream out, Context ctx) throws IOException {
    out.writeInt(size());

    for (S symb : coll)
      symb.write(out, ctx);
  }

  @Override
  public boolean add(S symb) {
    return coll.add(symb);
  }

  @Override
  public boolean addAll(Collection<? extends S> c) {
    return coll.addAll(c);
  }

  @Override
  public void clear() {
    coll.clear();
  }

  @Override
  public boolean contains(Object s) {
    return coll.contains(s);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return coll.containsAll(c);
  }

  @Override
  public boolean isEmpty() {
    return coll.isEmpty();
  }

  @Override
  public Iterator<S> iterator() {
    return coll.iterator();
  }

  @Override
  public boolean remove(Object o) {
    return coll.remove(o);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return coll.removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return coll.retainAll(c);
  }

  @Override
  public int size() {
    return coll.size();
  }

  @Override
  public Object[] toArray() {
    return coll.toArray();
  }

  @Override
  public <T> T[] toArray(T[] array) {
    return coll.toArray(array);
  }

  public S[] asArray() {
    return toArray(Misc.create(symbolClass, size()));
  }

  public String toString() {
    return String.format("%s(%s %d)", getClass().getSimpleName(),
        symbolClass.getSimpleName(), size());
  }

}
