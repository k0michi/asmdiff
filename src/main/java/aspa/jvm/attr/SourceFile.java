package aspa.jvm.attr;

import java.io.IOException;

import aspa.core.Context;
import aspa.core.Atom;
import aspa.core.Stream;
import aspa.core.Symbol;
import aspa.jvm.cp.CUtf8;
import aspa.jvm.cp.ConstantPool;

public final class SourceFile extends Atom implements AttributeValue {

  private CUtf8 file = new CUtf8();

  @Override
  public void read(Stream in, Context ctx) throws IOException {

    if (in.readInt() != 2)
      throw new InvalidAttributeLengthException();

    ConstantPool cp = (ConstantPool) ctx;
    file = cp.get(in.readUnsignedShort(), CUtf8.class);
  }

  @Override
  public void write(Stream out, Context ctx) throws IOException {
    out.writeInt(2);
    ConstantPool cp = (ConstantPool) ctx;
    out.writeShort(cp.get(file));
  }

  @Override
  public int compareTo(Symbol s) {
    SourceFile other = (SourceFile) s;
    return file.compareTo(other.file);
  }
}
