package aspa.core;

import java.io.IOException;
import aspa.util.Mask;

public abstract class Record implements ComparableSymbol {

  public abstract Atom[] keys();

  public abstract Symbol[] values();

  public Context context() {
    return null;
  }
  
  public boolean equals(Object o) {
    return o instanceof Record && compareTo((Record) o) == 0;
  }

  // TODO: FIX
  public final int compareTo(Symbol other) {
    return compareTo((Record) other);
  }

  // TODO: FIX
  private int compareTo(Record other) {
    final Atom[] keysA = keys();
    final Atom[] keysB = other.keys();

    int cmp = 0;

    for (int i = 0; i != keysA.length && cmp == 0; ++i) {
      Atom a = keysA[i];
      Atom b = keysB[i];
      cmp = a.compareTo(b);
    }
    return cmp;
  }

  @Override
  public int diff(Symbol other, Stream out, Context ctx)
      throws IOException {
    Record rother = (Record) other;
    final Symbol[] aF = values();
    final Symbol[] bF = rother.values();
    final int n = aF.length;
    int total_d = 0;
    
    Context newCtx = context();
    
    if (newCtx != null)
      ctx = newCtx;

    assert (out.report().beginSection(this, "vs %s", other));

    Mask mask = new Mask(n);

    long back = out.getFilePointer();
    out.write(mask.bitBuffer());

    for (int i = 0; i != n; ++i) {

      assert (out.report().line("element %d of type %s", i, aF[i].getClass()
          .getSimpleName()));
      int d = aF[i].diff(bF[i], out, ctx);

      if (d == 0)
        continue;

      if (d < 0)
        d = -d;
      assert(out.report().logPatched(aF[i].getClass(), 1));
      total_d += d;
      mask.set(i);
    }

    if (total_d != 0) {
      long pos = out.getFilePointer();
      out.seek(back);
      out.write(mask.bitBuffer());
      out.seek(pos);
    } else {
      out.seek(back);
    }
    assert (out.report().endSection());
    return total_d;
  }

  public void patch(Stream in, Context ctx) throws IOException {
    assert (in.report().beginSection(this, ""));
    
    Context newCtx = context();
    
    if (newCtx != null)
      ctx = newCtx;
    
    final Symbol[] v = values();
    final int n = v.length;
    Mask mask = new Mask(n);
    in.read(mask.bitBuffer());

    for (int i = 0; i != n; ++i) {
      assert (in.report().line("element %d of type %s %s", i, v[i].getClass()
          .getSimpleName(), mask.get(i) ? "p" : "="));

      if (mask.get(i)) {
        assert (in.report().line("is %s", v[i].toString()));
        v[i].patch(in, ctx);
        assert (in.report().line("patched to %s", v[i].toString()));
      }
    }
    assert (in.report().endSection());
  }
}
