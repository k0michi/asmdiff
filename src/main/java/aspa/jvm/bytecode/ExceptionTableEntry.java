package aspa.jvm.bytecode;

import java.io.IOException;

import aspa.core.Context;
import aspa.core.Atom;
import aspa.core.Record;
import aspa.core.Stream;
import aspa.core.Symbol;
import aspa.jvm.cp.CClass;
import aspa.jvm.cp.CUtf8;
import aspa.jvm.cp.ConstantPool;
import aspa.util.SInt;

public final class ExceptionTableEntry extends Record {
  private final SInt startPC   = new SInt();
  private final SInt endPC     = new SInt();
  private final SInt handlerPC = new SInt();
  private CClass     clazz     = new CClass();

  @Override
  public Atom[] keys() {
    return new Atom[] { clazz };
  }

  @Override
  public Symbol[] values() {
    return new Symbol[] { startPC, endPC, handlerPC };
  }

  private final String UNDEFINED_VALUE = "__undefined__";
  
  @Override
  public void read(Stream in, Context ctx) throws IOException {
    ConstantPool cp = (ConstantPool) ctx;
    startPC.set(in.readUnsignedShort());
    endPC.set(in.readUnsignedShort());
    handlerPC.set(in.readUnsignedShort());
    int idx = in.readUnsignedShort();
    if (idx != 0)
      clazz = cp.get(idx, CClass.class);
    else
      clazz = new CClass(new CUtf8(UNDEFINED_VALUE));
  }

  @Override
  public void write(Stream out, Context ctx) throws IOException {
    ConstantPool cp = (ConstantPool) ctx;
    out.writeShort(startPC.get());
    out.writeShort(endPC.get());
    out.writeShort(handlerPC.get());
    if (!clazz.get().equals(UNDEFINED_VALUE))
      out.writeShort(cp.get(clazz));
    else 
      out.writeShort(0);
  }
}
