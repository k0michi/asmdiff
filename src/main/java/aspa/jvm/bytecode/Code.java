package aspa.jvm.bytecode;

import java.io.IOException;
import java.io.PrintStream;

import aspa.core.Context;
import aspa.core.Atom;
import aspa.core.Record;
import aspa.core.Stream;
import aspa.core.Symbol;
import aspa.core.SymbolList;
import aspa.jvm.JVMSymbolSet;
import aspa.jvm.attr.Attribute;
import aspa.jvm.attr.AttributeValue;
import aspa.jvm.cp.ConstantPool;
import aspa.util.SInt;
import aspa.util.SShort;

// TODO: fix keys() and values()
public final class Code extends Record implements AttributeValue {
  private static final class MaxStack  extends SInt { }
  private static final class MaxLocals extends SInt { }
  private static final class CodeAttributes extends JVMSymbolSet<Attribute> {
    CodeAttributes() {
      super(Attribute.class);
    }
  }
  private final MaxStack                          maxStack       = new MaxStack();
  private final MaxLocals                         maxLocals      = new MaxLocals();
  private final SymbolList<Instruction>           instructions   = new SymbolList<Instruction>(
                                                                     Instruction.class);
  private final JVMSymbolSet<ExceptionTableEntry> exceptionTable = new JVMSymbolSet<ExceptionTableEntry>(
                                                                     ExceptionTableEntry.class);
  private final CodeAttributes                    attributes     = new CodeAttributes();

  @Override
  public Atom[] keys() {

    return new Atom[] {};
  }

  @Override
  public String toString() {
    return String.format("(S %d,L %d, I %d, E %d, A %d)", maxStack.get(),
        maxLocals.get(), instructions.size(), exceptionTable.size(),
        attributes.size());
  }

  @Override
  public Symbol[] values() {
    return new Symbol[] { maxStack, maxLocals, instructions, exceptionTable,
        attributes };
  }

  @Override
  public void read(Stream in, Context ctx) throws IOException {
    ConstantPool cp = (ConstantPool) ctx;
    // System.out.printf("reading from %d\n", (int) in.getFilePointer());
    in.seek(in.getFilePointer() + 4);
    maxStack.set(in.readUnsignedShort());
    maxLocals.set(in.readUnsignedShort());
    final int len = in.readInt();

    long end = in.mark(true) + len;

    // System.out.printf("reading code from %d\n", (int) in.getFilePointer());
    instructions.clear();

    while (in.getFilePointer() < end) {
      Instruction instr = new Instruction();
      instr.read(in, cp);
      instructions.add(instr);
      // System.out.println(instr);
    }

    // System.out.printf("reading ET from %d\n", (int) in.getFilePointer());
    exceptionTable.read(in, cp);
    // System.out.printf("reading ATTR from %d\n", (int) in.getFilePointer());
    attributes.read(in, cp);
  }

  public void dump(PrintStream ps) {
    System.out.printf("locals: %d, stack %d\n", maxStack, maxLocals);
    System.out.println("Code: {");
    for (Instruction i : instructions)
      System.out.printf(" %s\n", i);
    System.out.println("}");
  }

  @Override
  public void write(Stream out, Context ctx) throws IOException {
    ConstantPool cp = (ConstantPool) ctx;
    // System.out.printf("writing from %d\n", (int) out.getFilePointer());
    long totalLengthOffset = out.getFilePointer();
    out.writeInt(0); // we'll get back here
    out.writeShort(maxStack.get());
    out.writeShort(maxLocals.get());

    long codeLengthOffset = out.getFilePointer();
    out.writeInt(0); // we'll get back here
    out.mark(true);
    // System.out.printf("writing code from %d\n", (int) out.mark());
    for (Instruction instr : instructions) {
      instr.write(out, cp);
    }
    fixLengthIndicator(out, codeLengthOffset);

    // System.out.printf("writing ET from %d\n", (int) out.getFilePointer());
    exceptionTable.write(out, cp);

    // System.out.printf("writing ATTRS from %d\n", (int) out.getFilePointer());
    attributes.write(out, cp);

    fixLengthIndicator(out, totalLengthOffset);
  }

  private void fixLengthIndicator(Stream out, long offset)
      throws IOException {
    long currentPosition = out.getFilePointer();
    out.seek(offset);
    out.writeInt((int) (currentPosition - offset - 4));
    out.seek(currentPosition);
  }
}
