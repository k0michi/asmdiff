package aspa.jvm.cp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import aspa.core.ComparableSymbol;
import aspa.core.Context;
import aspa.core.Atom;
import aspa.core.Stream;
import aspa.core.Symbol;
import aspa.util.Pair;
import aspa.util.SPair;
import aspa.util.SString;
import aspa.util.Scalar;

public final class CEntry extends Atom implements CTags {
  public static final Comparator<CValue> VCOMPARATOR 
  = new Comparator<CValue>() {
    public int compare(CValue a, CValue b) {
      int cmp = a.tag() - b.tag();

      if (cmp != 0)
        return cmp;

      ComparableSymbol sa = (ComparableSymbol) a;
      ComparableSymbol sb = (ComparableSymbol) b;
      return sa.compareTo(sb);
    }
  };

  public boolean equals(Object o) {
    return o == this || (o instanceof CEntry && VCOMPARATOR.compare(this.value, ((CEntry) o).value) == 0);
  }
  public CEntry() {
    value = null;
  }

  public CEntry(CValue v) {
    value = v;
  }

  private CValue value;

  public int tag() {
    return value.tag();
  }

  public CValue value() {
    return value;
  }

  public int compareTo(Symbol s) {
    return VCOMPARATOR.compare(value, ((CEntry) s).value);
  }

  @Override
  public void read(Stream in, Context ctx) throws IOException {
    CValue v;
    byte tag;

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
        v = new CUnresolved(tag, in.readUnsignedShort(), in.readUnsignedShort());
        break;
      default:
        throw new RuntimeException("invalid tag: " + tag);
    }
    value = v;
  }

  boolean isResolved() {
    return !(value instanceof CUnresolved);
  }

  public CValue resolve(List<CEntry> constants, Map<Integer,Integer> i2ri) {
   
    if (!(value instanceof CUnresolved))
      return value;

    CUnresolved u = (CUnresolved) value;
    int[] indexes = u.indexes();

    int ai1 = i2ri.get(indexes[0]);
    int ai2 = -1;
    CValue v1 = constants.get(ai1).resolve(constants, i2ri);
    CValue v2 = null;

    if (indexes[1] != -1) {
      ai2 = i2ri.get(indexes[1]);
      v2 = constants.get(ai2).resolve(constants, i2ri);
    }

    switch (u.tag()) {
      case TAG_Class:
        value = new CClass((CUtf8) v1);
        break;
      case TAG_String:
        value = new CString((CUtf8) v1);
        break;
      case TAG_Field:
        value = new CField((CClass) v1, (CNameAndType) v2);
        break;
      case TAG_Method:
        value = new CMethod((CClass) v1, (CNameAndType) v2);
        break;
      case TAG_IMethod:
        value = new CIMethod((CClass) v1, (CNameAndType) v2);
        break;
      case TAG_NameAndType:
        value = new CNameAndType((CUtf8) v1, (CUtf8) v2);
        break;
      default:
        throw new ConstantPoolException("internal error (tag " + tag() + "??)");
    }
    return value;
  }

  @Override
  public void write(Stream out, Context ctx) throws IOException {
    if (value instanceof CGap)
      return;
    ConstantPool cp = (ConstantPool) ctx;
    int tag = value.tag();

    out.writeByte(tag);

    switch (tag) {
      case TAG_Utf8:
      case TAG_Integer:
      case TAG_Float:
      case TAG_Long:
      case TAG_Double:
        ((Scalar<?>) value).write(out);
        break;
      case TAG_Class:
      case TAG_String: {
        SString str = (SString) value;
        CUtf8 c = new CUtf8(str.get());
        out.writeShort(cp.get(c));
        break;
      }
      case TAG_Field:
      case TAG_Method:
      case TAG_IMethod:
      case TAG_NameAndType: {
        @SuppressWarnings("unchecked")
        Pair<? extends CValue, ? extends CValue> p = ((SPair<? extends CValue, ? extends CValue>) value)
            .get();
        out.writeShort(cp.get(p.first));
        out.writeShort(cp.get(p.second));
        break;
      }
      default:
        throw new RuntimeException("unexpected tag: " + value.tag());
    }
  }

  @Override
  public String toString() {
    return String.format("%s [ %s ]", TAGNAMES[value.tag()], value);
  }
}
