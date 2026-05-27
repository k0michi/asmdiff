package aspa.core;

import java.io.IOException;
import java.util.ArrayList;

import aspa.util.Misc;

public class LCS {
  private static final boolean DEBUG = false;
  private static final int     SAME  = '=';
  private static final int     ADD   = '+';

  private static interface PatchHandler {
    int getSourceLength();

    int getTargetLength();

    boolean isFlat();
    Class<?> getSymbolClass();
  
    boolean comparePos(int sourcePos, int targetPos);

    void onAddD(Stream out, int pos, int length) throws IOException;

    void onAddP(Stream in, int pos, int length) throws IOException;

    boolean onSameD(Stream out, int sourcePos, int targetPos, int length)
        throws IOException;

    void onSameP(Stream in, int sourcePos, int targetPos, int length)
        throws IOException;
  }

  private static final class ByteArrayHandler implements PatchHandler {
    private final byte[] source;
    private final byte[] target;

    public ByteArrayHandler(byte[] source, byte[] target) {
      this.source = source;
      this.target = target;
    }
    public Class<?> getSymbolClass() {
      return Byte.class;
    }
    public int getSourceLength() {
      return source.length;
    }

    public int getTargetLength() {
      return target.length;
    }

    public boolean comparePos(int sourcePos, int targetPos) {
      return source[sourcePos] == target[targetPos];
    }

    public void onAddD(Stream out, int targetPos, int length)
        throws IOException {
      out.write(target, targetPos, length);
    }

    public boolean onSameD(Stream out, int sourcePos, int targetPos,
        int length) throws IOException {
      return true;
    }

    public void onAddP(Stream in, int targetPos, int length)
        throws IOException {
      in.read(target, targetPos, length);
    }

    public void onSameP(Stream in, int sourcePos, int targetPos, int length)
        throws IOException {
      System.arraycopy(source, sourcePos, target, targetPos, length);
    }

    public boolean isFlat() {
      return true;
    }
  }

  private static final class SymbolArrayHandler<S extends ComparableSymbol>
      implements PatchHandler {
    private final Class<? extends S> clazz;
    private final Context            ctx;
    private final S[]                source;
    private final S[]                target;
    private final boolean            isGroundSymbol;

    public Class<?> getSymbolClass() {
      return clazz;
    }
    public SymbolArrayHandler(Class<? extends S> clazz, Context ctx,
        S[] source, S[] target) {
      this.clazz = clazz;
      this.ctx = ctx;
      this.source = source;
      this.target = target;
      this.isGroundSymbol = Atom.class.isAssignableFrom(clazz);
    }

    public int getSourceLength() {
      return source.length;
    }

    public int getTargetLength() {
      return target.length;
    }

    public boolean comparePos(int sourcePos, int targetPos) {
      return source[sourcePos].equals(target[targetPos]);
    }

    public void onAddD(Stream out, int targetPos, int length)
        throws IOException {
      int endPos = targetPos + length;

      for (int pos = targetPos; pos != endPos; pos++) {
        target[pos].write(out, ctx);
        assert (out.report().line("+ " + target[pos].toString()));
      }
    }

    public boolean onSameD(Stream out, int sourcePos, int targetPos,
        int length) throws IOException {
      boolean noDifferences = true;
      if (!isGroundSymbol) {

        for (int i = 0; i != length; i++) {
          Symbol sourceSymbol = source[sourcePos + i];
          Symbol targetSymbol = target[targetPos + i];

          long rollbackPosition = out.getFilePointer();
          out.writeOffset(i + 1);

          if (targetSymbol.diff(sourceSymbol, out, ctx) == 0) {
            out.seek(rollbackPosition);
            assert (out.report().line("= %s", sourceSymbol.toString()));
            assert (out.report().logUnchanged(clazz, 1));
          } else {
            assert (out.report().line("p %s", sourceSymbol.toString()));
            assert (out.report().logPatched(clazz, 1));
            noDifferences = false;
          }
        }
        out.writeOffset(0);
      }
      else
      {
        for (int i = 0; i != length; i++) {
          assert (out.report().line("= %s", source[sourcePos + i].toString()));
        }
        assert (out.report().logUnchanged(clazz, length));
      }
      return noDifferences;
    }

    public void onAddP(Stream in, int targetPos, int length)
        throws IOException {
      int endPos = targetPos + length;

      for (int pos = targetPos; pos != endPos; pos++) {
        S symbol = Misc.create(clazz);
        symbol.read(in, ctx);
        target[pos] = symbol;
        assert (in.report().line("+ %s", symbol.toString()));
      }
    }

    public void onSameP(Stream in, int sourcePos, int targetPos, int length)
        throws IOException {

      System.arraycopy(source, sourcePos, target, targetPos, length);

      if (!isGroundSymbol) {
        int offset;

        while ((offset = in.readOffset()) != 0) {
          assert (in.report().line("OFF %d %d %d", targetPos, offset, targetPos
              + offset - 1));
          target[targetPos + offset - 1].patch(in, ctx);
        }
      }
    }

    public boolean isFlat() {
      return isGroundSymbol;
    }
  }

  private static int get(int[] fp, int d) {
    return fp[(fp.length + d) % fp.length];
  }

  private static int set(int[] fp, int d, int v) {
    return (fp[(fp.length + d) % fp.length] = v);
  }

  private static void onAddD(Stream out, PatchHandler ph, int start,
      int length) throws IOException {
    assert (out.report().line("%c %d %d", ADD, start, length));
    assert (out.report().logAdded(ph.getSymbolClass(), length));
    out.write(ADD);
    out.writeOffset(length);
    ph.onAddD(out, start, length);
  }

  private static boolean onSameD(Stream out, PatchHandler ph,
      int sourcePos, int targetPos, int length) throws IOException {
    assert (out.report().line("%c %d %d [%d]", SAME, sourcePos, length,
        targetPos));
    out.write(SAME);
    out.writeOffset(length);
    out.writeOffset(sourcePos);
    return ph.onSameD(out, sourcePos, targetPos, length);
  }

  public static int diff(Stream out, byte[] a, byte[] b)
      throws IOException {
    return diff(out, new ByteArrayHandler(a, b));
  }

  public static <S extends ComparableSymbol> int diff(Stream out,
      Class<? extends S> clazz, Context ctx, S[] a, S[] b) throws IOException {
    return diff(out, new SymbolArrayHandler<S>(clazz, ctx, a, b));
  }

  public static int diff(Stream out, PatchHandler ph) throws IOException {
    // naiveLCS (a, b);

    final int M = ph.getSourceLength();
    final int N = ph.getTargetLength();
    
    if (N == 0) {
      if (M != 0)
        out.writeOffset(0);
      return M;
    }
    
    final int Delta = N - M;
    final int absDelta = Math.abs(Delta);
    int lo, hi;

    boolean specialCase = false;
    if (Delta >= 0) {
      lo = 0;
      hi = Delta;
    } else {
      lo = Delta;
      hi = 0;
      specialCase = !ph.comparePos(0, 0);
    }
 
    // // System.out.printf("WuLCS M %d N %d delta %d\n", M, N, Delta);
    ArrayList<int[]> vFP = new ArrayList<int[]>();

    int P = -1;
    int[] fp0 = null;
    int[] fp = null;

    do {
      // System.out.println("P=" + P);
      ++P;

      fp0 = fp;
      fp = new int[2 * P + absDelta + 3];
      vFP.add(fp);

      set(fp, lo - P - 1, -1);
      set(fp, hi + P + 1, -1);

      if (P != 0) {
        for (int d = lo - P; d <= hi + P; d++)
          set(fp, d, get(fp0, d));
      }

      for (int d = lo - P; d != Delta; ++d) {
        snake(ph, d, fp);
      }

      for (int d = hi + P; d != Delta; --d) {
        snake(ph, d, fp);
      }
     
    } while (snake(ph, Delta, fp) != N);

    
    int SES = absDelta + 2 * P;
    int LCS = (M + N - SES)/2;
    //System.out.printf("WuLCS SES=%d LCS=%d P=%d FLAT %s\n", SES, LCS, P, ph.isFlat());

    long rollbackPosition = out.getFilePointer();
    out.writeOffset(N);

    if (SES == 0) {
      if (onSameD(out, ph, 0, 0, N)) {
        out.seek(rollbackPosition);
        return 0;
      }
      return 1;
    }

    int d = Delta, y, yAdd = N;
    int total_kept = 0;
    do {
      int yAbove, yBelow;
      fp = vFP.get(P);
      y = get(fp, d);

      if (d == Delta) {
        yAbove = get(fp, d + 1);
        yBelow = get(fp, d - 1);
      } else if (d < Delta) {
        yAbove = P != 0 ? get(vFP.get(P - 1), d + 1) : -1;
        yBelow = get(fp, d - 1);
      } else {
        yAbove = get(fp, d + 1);
        yBelow = P != 0 ? get(vFP.get(P - 1), d - 1) : -1;
      }

      ++yBelow;

      // System.out.printf("> P %d D %d Y %d YA %d YB %d ADD %d\n", P, d, y, yAbove, yBelow, yAdd);

      if (yAbove >= yBelow) {
        if (yAbove < y) {
          if (y < yAdd) {
            onAddD(out, ph, y, yAdd - y);
          }
          total_kept += y - yAbove;
          onSameD(out, ph, yAbove - d, yAbove, y - yAbove);

          yAdd = yAbove;
        }

        if (d < Delta)
          P--;

        d++;
        y = yAbove;
      } else {
        if (yBelow < y) {
          if (y < yAdd)
            onAddD(out, ph, y, yAdd - y);

          if (!specialCase || yBelow != d /* || y - yBelow > 1*/) {
            onSameD(out, ph, yBelow - d, yBelow, y - yBelow);
            total_kept += y - yBelow;
          } else {
            onAddD(out,ph,0,y-yBelow);
          }
          yAdd = yBelow;
        }

        if (d > Delta)
          P--;

        d--;
        y = yBelow - 1;
      }
      // // System.out.printf(">> P %d D %d Y %d ADD %d\n", P, d, y, yAdd);
    } while (y > 0 || y > d);
    
    assert(out.report().logRemoved(ph.getSymbolClass(), M - total_kept));
    
    if (yAdd > 0)
      onAddD(out, ph, 0, yAdd);

    return 1;
  }

  private static int snake(PatchHandler ph, int d, int[] fp) {
    final int M = ph.getSourceLength();
    final int N = ph.getTargetLength();

    final int yAbove = get(fp, d + 1);
    final int yBelow = get(fp, d - 1) + 1;

    int y = yAbove > yBelow ? yAbove : yBelow;
    int x = y - d;

    // System.out.printf("%d %d\n", x,y);
    while (x < M && y < N && ph.comparePos(x, y)) {
      x++;
      y++;
      // System.out.printf("EQ %d %d\n", x,y);
    }
    // System.out.printf("R=%d\n", y);
    return set(fp, d, y);
  }

  /**
   * Patch a plain sequence of bytes.
   * 
   * @param in
   *          patch stream
   * @param source
   *          original sequence
   * @return patched sequence
   * @throws IOException
   *           if an I/O error occurs
   */
  public static byte[] patch(Stream in, byte[] source) throws IOException {
    final int N = in.readOffset();

    final byte[] target = new byte[N];

    patch(in, new ByteArrayHandler(source, target));

    return target;
  }

  public static <S extends ComparableSymbol> S[] patch(Stream in,
      Context ctx, S[] source, Class<S> clazz) throws IOException {

    final int N = in.readOffset();
    final S[] target = Misc.create(clazz, N);

    if (N != 0)
     patch(in, new SymbolArrayHandler<S>(clazz, ctx, source, target));

    return target;
  }

  private static void patch(Stream in, PatchHandler ph) throws IOException {
    int position = ph.getTargetLength();
    assert (in.report().line("%d -> %d", ph.getSourceLength(),
        ph.getTargetLength()));
    do {
      int operation = in.read();
      int length = in.readOffset();

      position -= length;

      assert (in.report().line("%c %d %d", operation, position, length));

      switch (operation) {
        case ADD:
          ph.onAddP(in, position, length);
          break;
        case SAME:
          ph.onSameP(in, in.readOffset(), position, length);
          break;
        default:
          throw new IOException("Invalid patch format -- file position "
              + in.getFilePointer());
      }
    } while (position != 0);
  }

  public static void naiveLCS(byte[] a, byte[] b) {
    final int M = a.length;
    final int N = b.length;
    final int[][] l = new int[M + 1][N + 1];

    for (int i = 0; i < M; i++) {
      for (int j = 0; j < N; j++) {
        l[i + 1][j + 1] = a[i] == b[j] ? l[i][j] + 1 : Math.max(l[i + 1][j],
            l[i][j + 1]);
      }
    }

    for (int i = 0; i <= M; i++) {
      for (int j = 0; j <= N; j++)
        System.out.printf("%5d", l[i][j]);
      System.out.print('\n');
    }

    System.out.printf("LCS = %d\n", l[M][N]);
  }
  public static void main(String[] args) throws IOException {
    byte[] a = args[0].getBytes();
    byte[] b = args[1].getBytes();
    naiveLCS(a,b);
    Stream p = new Stream(args[2], "rw");
    diff(p,a,b);
    p.close();
  }
}
