package aspa.jvm.attr;

import java.io.IOException;
import java.util.HashMap;

import aspa.core.Context;
import aspa.core.Atom;
import aspa.core.Stream;
import aspa.core.Record;
import aspa.core.Symbol;
import aspa.jvm.bytecode.Code;
import aspa.jvm.cp.CUtf8;
import aspa.jvm.cp.ConstantPool;
import aspa.util.Misc;

public class Attribute extends Record {
  private static final String                                           CODE_ATTR                      = "Code";
  private static final String                                           CONSTANT_VALUE_ATTR            = "ConstantValue";
  private static final String                                           DEPRECATED_ATTR                = "Deprecated";
  private static final String                                           EXCEPTIONS_ATTR                = "Exceptions";
  private static final String                                           INNER_CLASSES_ATTR             = "InnerClasses";
  private static final String                                           LINE_NUMBER_TABLE_ATTR         = "LineNumberTable";
  private static final String                                           LOCAL_VARIABLE_TABLE_ATTR      = "LocalVariableTable";
  private static final String                                           LOCAL_VARIABLE_TYPE_TABLE_ATTR = "LocalVariableTypeTable";
  private static final String                                           SOURCE_FILE_ATTR               = "SourceFile";
  private static final String                                           STACK_MAP_TABLE_ATTR           = "StackMapTable";
  private static final String                                           SYNTHETIC_ATTR                 = "Synthetic";
  private static final String                                           SIGNATURE_ATTR                 = "Signature";
  private static final HashMap<String, Class<? extends AttributeValue>> clazzes;

  static {
    clazzes = new HashMap<String, Class<? extends AttributeValue>>();
    clazzes.put(CODE_ATTR, Code.class);
    clazzes.put(CONSTANT_VALUE_ATTR, ConstantValue.class);
    clazzes.put(DEPRECATED_ATTR, ZeroLengthAttribute.class);
    clazzes.put(EXCEPTIONS_ATTR, Exceptions.class);
    clazzes.put(INNER_CLASSES_ATTR, InnerClasses.class);
    clazzes.put(LINE_NUMBER_TABLE_ATTR, LineNumberTable.class);
    clazzes.put(LOCAL_VARIABLE_TABLE_ATTR, LocalVariableTable.class);
    clazzes.put(LOCAL_VARIABLE_TYPE_TABLE_ATTR, UnparsedAttribute.class); // TODO
    clazzes.put(STACK_MAP_TABLE_ATTR, UnparsedAttribute.class); // TODO
    clazzes.put(SOURCE_FILE_ATTR, SourceFile.class);
    clazzes.put(SYNTHETIC_ATTR, ZeroLengthAttribute.class);
    clazzes.put(SIGNATURE_ATTR, UnparsedAttribute.class); // TODO
  }

  private CUtf8  name = new CUtf8();
  private Symbol symb = null;

  @Override
  public Atom[] keys() {
    return new Atom[] { name };
  }

  @Override
  public Symbol[] values() {
    return new Symbol[] { symb };
  }

  @Override
  public void read(Stream in, Context ctx) throws IOException {
    ConstantPool cp = (ConstantPool) ctx;

    name = cp.get(in.readUnsignedShort(), CUtf8.class);
    // assert(in.report().line("reading %s", name));
    
    Class<? extends AttributeValue> clazz = clazzes.get(name.get());
    symb = clazz != null ? Misc.create(clazz) : new UnparsedAttribute();

    symb.read(in, cp);
  }

  @Override 
  public int diff(Symbol other, Stream out, Context ctx) throws IOException {
    int d = super.diff(other, out, ctx);
    assert (d == 0 || out.report().logPatched(getClass().getName() + "#" + name.toString(), 1));
    return d;
  }
  
  @Override
  public void write(Stream out, Context ctx) throws IOException {
    ConstantPool cp = (ConstantPool) ctx;
    out.writeShort(cp.get(name));
    symb.write(out, cp);
  }

  @Override
  public String toString() {
    return "ATTR(" + name + "," + symb + ")";
  }
}
