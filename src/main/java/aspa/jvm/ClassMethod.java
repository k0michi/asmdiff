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

public final class ClassMethod extends Record {
  
  private static final class MModifiers extends SShort { }
  private static final class MAttributes extends JVMSymbolSet<Attribute> {
    MAttributes() {
      super(Attribute.class);
    }
  }
  private MModifiers   flags      = new MModifiers();
  private CUtf8       name       = new CUtf8();
  private CUtf8       signature  = new CUtf8();
  private MAttributes attributes             = new MAttributes();

  @Override
  public Atom[] keys() {
    return new Atom[] { name, signature };
  }

  @Override
  public Symbol[] values() {
    return new Symbol[] { flags, attributes };
  }

  @Override
  public void read(Stream in, Context ctx) throws IOException {
    ConstantPool cp = (ConstantPool) ctx;
    flags.set(in.readShort());
    name = cp.get(in.readUnsignedShort(), CUtf8.class);
    signature = cp.get(in.readUnsignedShort(), CUtf8.class);
    attributes.read(in, cp);
  }

  @Override
  public void write(Stream out, Context ctx) throws IOException {
    ConstantPool cp = (ConstantPool) ctx;
    out.writeShort(flags.get());
    out.writeShort(cp.get(name));
    out.writeShort(cp.get(signature));
    attributes.write(out, cp);
  }

  @Override
  public String toString() {
    return flags + "/" + name + "/" + signature;
  }
}
