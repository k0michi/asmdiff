package aspa.jvm;

import java.io.File;
import java.io.IOException;

import aspa.core.Context;
import aspa.core.Atom;
import aspa.core.FileTypes;
import aspa.core.Record;
import aspa.core.Stream;
import aspa.core.Symbol;
import aspa.core.Root;
import aspa.jvm.attr.Attribute;
import aspa.jvm.cp.CClass;
import aspa.jvm.cp.ConstantPool;
import aspa.util.SInt;
import aspa.util.SShort;

public final class ClassFile extends Record implements Root {
  private static final int          MAGIC       = 0xCAFEBABE;

  private static final class Version extends SInt { }
  private static final class Modifiers extends SShort { }
  private static final class CAttributes extends JVMSymbolSet<Attribute> {
    CAttributes() {
      super(Attribute.class);
    }
  }
   
  private final Version             version     = new Version();
  private final Modifiers           flags       = new Modifiers();
  private ConstantPool              pool        = new ConstantPool();
  private CClass                    clazz       = new CClass();
  private CClass                    super_clazz = new CClass();
  private JVMSymbolSet<CClass>      interfaces  = new JVMSymbolSet<CClass>(
                                                    CClass.class);
  private JVMSymbolSet<ClassField>  fields      = new JVMSymbolSet<ClassField>(
                                                    ClassField.class);
  private JVMSymbolSet<ClassMethod> methods     = new JVMSymbolSet<ClassMethod>(
                                                    ClassMethod.class);
  private CAttributes               attrs       = new CAttributes();

  public ClassFile() {

  }

  public ClassFile(String filename) throws IOException {
    this(new File(filename));
    
  }

  public ClassFile(File file) throws IOException {
    Stream in = new Stream(file, "r");
    read(in, null);
    in.close();
  }

  @Override
  public Atom[] keys() {
    return new Atom[] { clazz };
  }

  @Override
  public Symbol[] values() {
    return new Symbol[] { version, pool, flags, clazz, super_clazz, interfaces,
        fields, methods, attrs };
  }
  
  
  @Override
  public Context context() {
    return pool;
  }

  @Override
  public void read(Stream in, Context dummy) throws IOException {
    
    assert(in.report().beginSection(this, ""));
    
   
    if (in.readInt() != MAGIC)
      throw new JVMFormatException("Java class file has no magic header");

    version.read(in);
    pool.read(in, null);
    flags.read(in);
    clazz = pool.get(in.readUnsignedShort(), CClass.class);
    
    try {
      int sIdx = in.readUnsignedShort();
      if (sIdx != 0) // only 0 for java.lang.Object
        super_clazz = pool.get(sIdx, CClass.class);
      else
        super_clazz = CClass.UNDEFINED;
      
      interfaces.read(in, pool);
      fields.read(in, pool);
      methods.read(in, pool);
      attrs.read(in, pool);
      assert(in.report().endSection());
    } catch (RuntimeException e) {
      throw new JVMFormatException("error reading " + clazz, e);
    }
  }

  @Override
  public void write(Stream out, Context dummy) throws IOException {
    out.writeInt(MAGIC);
    version.write(out);
    pool.write(out, null);
    flags.write(out);

    out.writeShort(pool.get(clazz));
    if (super_clazz != CClass.UNDEFINED)
      out.writeShort(pool.get(super_clazz));
    else
      out.writeShort(0);
    interfaces.write(out, pool);
    fields.write(out, pool);
    methods.write(out, pool);
    attrs.write(out, pool);
  }

  @Override
  public String toString() {
    return clazz.toString();
  }
  
  /**
   * Get constant pool.
   * @return Constant pool for this class.
   */
  public ConstantPool getPool() {
    return pool;
  }
  
  /**
   * Program entry point for class patching utilities.
   * @param args Program arguments.
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      System.err.println("Invalid arguments :(");
    }
    
    int exCode = 0;
    try {
      switch (args[0]) {
        // generate patch
        case "diff": {
          if (args.length != 4) {
            System.err.println("Arguments should be: <source class file> <target class file> <output patch file>");
            exCode = 1;
            break;
          }
          String fSrc = args[1], fTgt = args[2], fDiff = args[3];

          ClassFile cSrc = new ClassFile(fSrc);
          ClassFile cTgt = new ClassFile(fTgt);
          Stream diff = new Stream(fDiff, "rw");
          cTgt.diff(cSrc, diff, cTgt.getPool());
          diff.close();  

          File fObj = new File(fDiff);
          long patchLength = fObj.length();

          if (patchLength == 0L) {
            fObj.delete();
            System.err.printf("No differences found. Patch file '%s' not generated.\n", fDiff);
          } else {
            System.err.printf("Patch '%s' generated (%s bytes).\n", fDiff, patchLength);
          }
          break;
        }
        // apply patch
        case "patch": {
          if (args.length != 4) {
            System.err.println("Arguments should be: <source class file> <patch file> <output class file>");
            System.exit(1);
          }
          String fSrc = args[1], fDiff = args[2], fTgt = args[3]; 
          ClassFile c = new ClassFile(fSrc);
          Stream diff = new Stream(fDiff, "r");
          c.patch(diff, c.getPool());
          Stream out = new Stream(fTgt,"rw");
          c.write(out, null);
          diff.close();  
          out.close();
          System.err.printf("JVM class file '%s' generated (%s bytes)\n", fTgt, new File(fTgt).length());
          break;
        }
        // class read-write test
        case "rwtest": {
          ClassFile c = new ClassFile();
          c.read(new Stream(args[1], "r"), null);
          Stream out = new Stream(args[2], "rw");
          c.write(out, null);
          out.close();
          break;
        }
        default:
          System.err.printf("Invalid command: '%s' :(\n", args[0]);
          exCode = 1;
      }
    }
    catch (Exception e) {
      System.err.println("Exception while processing command :(");
      e.printStackTrace(System.err);
      exCode = 1;
    }
    
    System.exit(exCode);
  }
}
