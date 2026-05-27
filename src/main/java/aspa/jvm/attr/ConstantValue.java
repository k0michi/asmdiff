package aspa.jvm.attr;

import java.io.IOException;

import aspa.core.Context;
import aspa.core.Atom;
import aspa.core.Stream;
import aspa.core.Symbol;
import aspa.jvm.cp.CValue;
import aspa.jvm.cp.ConstantPool;

public final class ConstantValue extends Atom implements AttributeValue {
  private CValue data;

  @Override
  public int compareTo(Symbol s) {
    ConstantValue other = (ConstantValue) s;
    int cmp = data.getClass().getName()
        .compareTo(other.data.getClass().getName());
    if (cmp == 0)
      cmp = data.compareTo(other.data);
    return cmp;
  }

  @Override
  public void read(Stream in, Context ctx) throws IOException {
    ConstantPool cp = (ConstantPool) ctx;

    if (in.readInt() != 2)
      throw new InvalidAttributeLengthException();

    data = cp.get(in.readUnsignedShort());
  }

  @Override
  public void write(Stream out, Context ctx) throws IOException {
    ConstantPool cp = (ConstantPool) ctx;
    out.writeInt(2);
    out.writeShort(cp.get(data));
  }

  @Override
  public String toString() {
    return data.toString();
  }
}
