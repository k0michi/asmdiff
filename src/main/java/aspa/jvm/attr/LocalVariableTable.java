package aspa.jvm.attr;

import java.io.IOException;

import aspa.core.Atom;
import aspa.core.Context;
import aspa.core.Stream;
import aspa.core.Record;
import aspa.core.Symbol;
import aspa.jvm.cp.CUtf8;
import aspa.jvm.cp.ConstantPool;
import aspa.util.SInt;

public final class LocalVariableTable 
extends EntrySet<LocalVariableTable.Entry> implements AttributeValue {
  
  public LocalVariableTable() {
    super(Entry.class);
  }
  
  @Override
  public int entryLength() {
    return Entry.LENGTH;
  }
  
  public static class Entry extends Record {
    public static final int LENGTH = 10;

    private SInt            pc     = new SInt();
    private SInt            length = new SInt();
    private CUtf8           name   = new CUtf8();
    private CUtf8           type   = new CUtf8();
    private SInt            var    = new SInt();

    @Override
    public Atom[] keys() {
      return new Atom[] { pc, length, var };
    }

    @Override
    public Symbol[] values() {
      return new Symbol[] { name, type };
    }

    @Override
    public void read(Stream in, Context ctx) throws IOException {
      ConstantPool cp = (ConstantPool) ctx;
      pc.set(in.readUnsignedShort());
      length.set(in.readUnsignedShort());
      name = cp.get(in.readUnsignedShort(), CUtf8.class);
      type = cp.get(in.readUnsignedShort(), CUtf8.class);
      var.set(in.readUnsignedShort());
    }

    @Override
    public void write(Stream out, Context ctx) throws IOException {
      ConstantPool cp = (ConstantPool) ctx;
      out.writeShort(pc.get());
      out.writeShort(length.get());
      out.writeShort(cp.get(name));
      out.writeShort(cp.get(type));
      out.writeShort(var.get());
    }
  }

}
