package aspa.jvm;

import java.io.IOException;

import aspa.core.Context;
import aspa.core.Atom;
import aspa.core.Record;
import aspa.core.Stream;
import aspa.core.Symbol;
import aspa.jvm.attr.Attribute;
import aspa.jvm.cp.CUtf8;
import aspa.jvm.cp.ConstantPool;
import aspa.util.SShort;

public final class ClassField extends Record {
  
  private static final class FModifiers extends SShort { }
  private static final class FAttributes extends JVMSymbolSet<Attribute> {
    FAttributes() {
      super(Attribute.class);
    }
  }
  private FModifiers               flags = new FModifiers();
  private CUtf8                   name  = new CUtf8();
  private CUtf8                   desc  = new CUtf8();
  private FAttributes attrs = new FAttributes();

  @Override
  public Atom[] keys() {
    return new Atom[] { name };
  }

  @Override
  public Symbol[] values() {
    return new Symbol[] { flags, desc, attrs };
  }

  @Override
  public void read(Stream in, Context ctx) throws IOException {
    ConstantPool cp = (ConstantPool) ctx;
    flags.read(in);
    name = cp.get(in.readUnsignedShort(), CUtf8.class);
    desc = cp.get(in.readUnsignedShort(), CUtf8.class);
    attrs.read(in, cp);
  }

  @Override
  public void write(Stream out, Context ctx) throws IOException {

    ConstantPool cp = (ConstantPool) ctx;
    // System.out.printf("FIELD %s %s %s\n", name, desc, cp.getClass());
    flags.write(out);
    out.writeShort(cp.get(name));
    out.writeShort(cp.get(desc));
    attrs.write(out, cp);
  }

  @Override
  public String toString() {
    return flags + "/" + name + "/" + desc;
  }
}
