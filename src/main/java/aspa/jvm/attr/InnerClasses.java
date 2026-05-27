package aspa.jvm.attr;

import java.io.IOException;

import aspa.core.Atom;
import aspa.core.Context;
import aspa.core.Stream;
import aspa.core.Record;
import aspa.core.Symbol;
import aspa.jvm.cp.CClass;
import aspa.jvm.cp.CUtf8;
import aspa.jvm.cp.ConstantPool;
import aspa.util.SShort;

public final class InnerClasses 
extends EntrySet<InnerClasses.Entry> implements AttributeValue {
  
  public InnerClasses() {
    super(Entry.class);
  }
    
  @Override
  public int entryLength() {
    return Entry.LENGTH;
  }
  
  public static class Entry extends Record {
    public static final int LENGTH  = 8;

    private CClass          innerClass = CClass.UNDEFINED;
    private CClass          outerClass = CClass.UNDEFINED;
    private CUtf8           name       = CUtf8.UNDEFINED;
    private SShort          flags      = new SShort();

    @Override
    public Atom[] keys() {
      return new Atom[] { innerClass, outerClass, name, flags };
    }

    @Override
    public Symbol[] values() {
      return new Symbol[] {};
    }

    @Override
    public void read(Stream in, Context ctx) throws IOException {
      int idx;
      ConstantPool cp = (ConstantPool) ctx;
      
      idx = in.readUnsignedShort();
      if (idx != 0)
        innerClass = cp.get(idx, CClass.class);
 
      idx = in.readUnsignedShort();
      if (idx != 0)
        outerClass = cp.get(idx, CClass.class);
      
      idx = in.readUnsignedShort();
      if (idx != 0)
        name = cp.get(idx, CUtf8.class);

      flags.read(in);
    }

    @Override
    public void write(Stream out, Context ctx) throws IOException {
      ConstantPool cp = (ConstantPool) ctx;
      out.writeShort(innerClass != CClass.UNDEFINED ? cp.get(innerClass) : 0);
      out.writeShort(outerClass != CClass.UNDEFINED ? cp.get(outerClass) : 0);
      out.writeShort(name != CUtf8.UNDEFINED ? cp.get(name) : 0);
      flags.write(out);
    }
  }

}
