package aspa.jvm.cp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import aspa.core.Context;
import aspa.core.Atom;
import aspa.core.Stream;
import aspa.core.Symbol;
import aspa.core.LCS;
import aspa.core.SymbolList;
import aspa.util.Pair;
import aspa.util.SPair;
import aspa.util.SString;
import aspa.util.Scalar;

public class ConstantPool implements CTags, Context, Symbol {
  private enum State {
    invalid, read, resolved;
  }
  private static final boolean        OPTIMIZE = false;
  private static final boolean        DEBUG = false;
  private int cp_logical_len = 0;
  private State                       state = State.invalid;
  private List<CEntry>          constants = new ArrayList<CEntry>();
  private final Map<Integer, Integer> i2ri = new HashMap<Integer, Integer>();
  private final Map<Integer, Integer> ri2i = new HashMap<Integer, Integer>();
  private final Map<CValue, Integer> c2i = new TreeMap<CValue, Integer>(
                                                CEntry.VCOMPARATOR);

  public void read(Stream in, Context dummy) throws IOException {
    state = State.invalid;

    // Read the constant pool entries.
    cp_logical_len = in.readUnsignedShort();
    
    constants.clear();

    int i = 1;
    int j = 0;
    
    while (i != cp_logical_len) {
      CValue v;
      int tag;

      switch (tag = in.readByte()) {
        case TAG_Utf8:
          v = new CUtf8(in.readUTF());
          break;
        case TAG_Integer:
          v = new CInteger(in.readInt());
          break;
        case TAG_Float:
          v = new CFloat(in.readFloat());
          break;
        case TAG_Long:
          v = new CLong(in.readLong());
          break;
        case TAG_Double:
          v = new CDouble(in.readDouble());
          break;
        case TAG_Class:
        case TAG_String:
          v = new CUnresolved(tag, in.readUnsignedShort());
          break;
        case TAG_Field:
        case TAG_Method:
        case TAG_IMethod:
        case TAG_NameAndType:
          v = new CUnresolved(tag, in.readUnsignedShort(),
              in.readUnsignedShort());
          break;
        default:
          throw new ConstantPoolException("invalid tag: " + tag);
      }
      CEntry e = new CEntry(v);
      i2ri.put(i, j);
      ri2i.put(j, i);
      constants.add(e);
      j++;
      i++;
 
      if (tag == TAG_Long || tag == TAG_Double)
        i++;
    }
    state = State.read;
    resolve();
  }

  @SuppressWarnings("unused")
  public void resolve() {
    if (state == State.invalid)
      throw new ConstantPoolException("constant pool needs to be read first");

    if (state == State.resolved)
      return;

    assert (!DEBUG || dump("before resolution"));

    for (CEntry c : constants) {
      c.resolve(constants, i2ri);
    }

    assert (!DEBUG || dump("after resolution"));

    buildIndex();
    state = State.resolved;
  }

  private void buildIndex() {
    c2i.clear();

    for (int i = 0; i != constants.size(); ++i)
      c2i.put(constants.get(i).value(), ri2i.get(i));
  }

  @Override
  public int diff(Symbol s, Stream out, Context ctx) throws IOException {
    ConstantPool other = (ConstantPool) s;

    assert (out.report().beginSection(this, "vs %s", other));

    if (OPTIMIZE) {
      orderAndCompress(); 
      other.orderAndCompress();
    }
    CEntry[] a = other.constants.toArray(new CEntry[other.constants.size()]);
    CEntry[] b = constants.toArray(new CEntry[constants.size()]);    
    int d = LCS.diff(out, CEntry.class, ctx, a, b);
    
    assert (out.report().endSection());

    return d;
  }
  
  private void orderAndCompress() {
    assert(!DEBUG || dump("before optimization"));
    CValue[] usConstants = new CValue[c2i.size()];
    int i = 1;
    ri2i.clear();
    i2ri.clear();
    c2i.keySet().toArray(usConstants);
    constants.clear();
    for (int j=0; j < usConstants.length; j++) {
      i2ri.put(i, j);
      ri2i.put(j, i);
      switch (usConstants[j].tag()) {
        case TAG_Long:
        case TAG_Double:
          i++;
        default:
          i++;
      }
      constants.add(new CEntry(usConstants[j]));
    }
    cp_logical_len = i;
    
    buildIndex();
    assert(!DEBUG || dump("after optimization"));
  }

  @Override
  public String toString() {
    return String.format("CP <%d>", constants.size());
  }

  @SuppressWarnings("unused")
  public void patch(Stream in, Context ctx) throws IOException {
    assert (!DEBUG || dump("before patching"));

    CEntry[] a = constants.toArray(new CEntry[constants.size()]);  
    CEntry[] b = LCS.patch(in, ctx, a, CEntry.class);
    constants.clear();
    i2ri.clear();
    ri2i.clear();
    constants = Arrays.asList(b);
    state = State.read;

    int i = 1, j = 0;
    for (CEntry e : constants) {
      i2ri.put(i, j);
      ri2i.put(j, i);
      switch(e.tag()) {
        case TAG_Long:
        case TAG_Double:
          i++;
        default:
          i++;
          j++;
      }
    }
    cp_logical_len = i;
    assert (!DEBUG || dump("after patching"));
    resolve();
  }

  public <T extends CValue> T get(int index, Class<T> clazz) {
    return clazz.cast(get(index));
  }

  private CValue copyOf(CValue c) {
    // TODO hack ...
    Atom s = (Atom) c;
    return (CValue) s.copy();
  }

  public CValue get(int index) {
    if (index <= 0 || index >= cp_logical_len)
      throw new ConstantPoolException("Invalid constant pool index: " + index + " (entries: "+ cp_logical_len +")");
    
    return copyOf(constants.get(i2ri.get(index)).value());
  }

  public int get(CValue value) {
    // System.out.printf("%s %s %s -> %s\n", TAGNAMES[value.tag()], value, value, c2i.get(value));
    return c2i.get(value);
  }

  public void write(Stream out, Context ctx) throws IOException {
    assert (!DEBUG || dump("before writing"));

    out.writeShort(cp_logical_len);

    for (CEntry c : constants) {
      CValue v = c.value();
      int tag = v.tag();

      out.writeByte(tag);

      switch (tag) {
        case TAG_Long:
        case TAG_Double:
        case TAG_Utf8:
        case TAG_Integer:
        case TAG_Float:
          ((Scalar<?>) v).write(out);
          break;
        case TAG_Class:
        case TAG_String: {
          SString str = (SString) v;
          out.writeShort(get(new CUtf8(str.get())));
          break;
        }
        case TAG_Field:
        case TAG_Method:
        case TAG_IMethod:
        case TAG_NameAndType: {
          @SuppressWarnings("unchecked")
          Pair<? extends CValue, ? extends CValue> p = ((SPair<? extends CValue, ? extends CValue>) v)
              .get();
          out.writeShort(get(p.first));
          out.writeShort(get(p.second));
          break;
        }
        default:
          throw new RuntimeException("unexpected tag: " + tag);
      }
    }
  }

  private boolean dump(String info) {
    System.out.println("CP dump: " + info);

    for (int i = 0; i != constants.size(); ++i)
      System.out.printf("%4d %4d - %s\n", i, ri2i.get(i), constants.get(i));

    return true;
  }
}
