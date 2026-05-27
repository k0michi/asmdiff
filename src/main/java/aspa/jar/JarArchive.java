package aspa.jar;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import aspa.core.Atom;
import aspa.core.Context;
import aspa.core.FileTypes;
import aspa.core.Stream;
import aspa.core.Patch2;
import aspa.core.Record;
import aspa.core.Symbol;
import aspa.core.SymbolSet;
import aspa.core.Root;
import aspa.jvm.ClassFile;
import aspa.util.Misc;
import aspa.util.SString;

// TODO: handle MANIFEST
// TODO: read classes in lazy manner for low memory usage.

/**
 * JAR archive.
 * @author Eduardo Marques, DI/FCUL, 2013
 */
public final class JarArchive  {

  /**
   * Buffer size constant.
   */
  private static final int BUFSIZE = 8192;
  
  /**
   * Jar archive item (only JVM classes or directories for now).
   */
  public static final class Item extends Record {
    private final SString name;
    private final Stream file;
    private final long position, length;
    private Symbol symbol;
    
    public void resolve() throws IOException {
      if (symbol == null) {
        file.seek(position);
        symbol = new ClassFile();
        symbol.read(file, null);
      }
    }
    
    public void clear() {
      if (symbol != null) {
        symbol = null;
      }
    }
    
    public Item() throws IOException {
      name = new SString();
      file = null;
      position = 0;
      length = 0;
    }
    public Item(String name, Stream file, long position, long length) {
      this.name = new SString(name);
      this.file = file;
      this.position  = position;
      this.length = length;
    }

    public Item(String name) {
      // Constructor to handle directories.
      this.symbol = this.name =  new SString(name);
      this.file = null;
      this.position = 0;
      this.length = 0;
    }
  
    @Override
    public int diff(Symbol other, Stream out, Context ctx) throws IOException {
      Item otherItem = (Item) other;
      resolve();
      otherItem.resolve();
      int d = super.diff(other, out, ctx);  
      clear();
      otherItem.clear();
      return d;
    }
    
    @Override
    public void patch(Stream in, Context ctx) throws IOException { 
      resolve();
      super.patch(in, ctx);
    }
   
    @Override
    public Atom[] keys() {
      return new Atom[] { name };
    }

    @Override
    public Symbol[] values() {
      return new Symbol[] { symbol };
    }

    @Override 
    public void write(Stream out, Context ctx) throws IOException {
      name.write(out);
      if (!name.get().endsWith("/")) {
        resolve();
        symbol.write(out, ctx);
        clear();
      }
    }

    @Override 
    public void read(Stream in, Context ctx) throws IOException {
      name.read(in);
      if (name.get().endsWith("/")) {
        symbol = name;
      } else {
        symbol = FileTypes.createSymbol(name.get());
        symbol.read(in, ctx);
      }
    }
  }


  private byte[] buffer;
  private final SymbolSet<Item> items;

  public JarArchive()  {
    buffer = new byte[BUFSIZE];
    items = new SymbolSet<Item>(Item.class);
  }

  public  void read(String file) throws IOException {
    JarInputStream jin = new JarInputStream(new FileInputStream(file), false);
    // assert(in.report().beginSection(this, ""));
    File tmpFile = Misc.createTemporaryFile();
    Stream tmpStream = new Stream(tmpFile, "rw");
   // tmpFile.deleteOnExit();
    try {
      JarEntry entry;
      while( (entry = jin.getNextJarEntry()) != null) {
//        if (items.size() % 1000 == 0) {
//          System.out.print('.');
//        }
        
        if (entry.isDirectory())  {
          items.add(new Item(entry.getName()));
          continue;
        }

        if (!entry.getName().endsWith(".class")) {
          // System.err.printf("Ignoring JAR entry '%s'\n", entry.getName());
          continue;
        }

        int n;
        long position = tmpStream.getFilePointer();
        while ((n = jin.read(buffer, 0, buffer.length)) > 0) {
          tmpStream.write(buffer, 0, n);
        }
        //System.out.println(entry.getName() + " "+position + " "+ (tmpStream.getFilePointer() - position));
        items.add(new Item(entry.getName(), tmpStream, position, tmpStream.getFilePointer() - position));
      }
      tmpStream.truncate();

      // System.out.println();
      jin.close();
    } 
    catch (IOException e) {
      jin.close();
      throw e;
    } finally {
      tmpFile.delete();
    }
  }

  public void write(String file) throws IOException {
    JarOutputStream jout = new JarOutputStream(new FileOutputStream(file));
    jout.setLevel(0);
    File tmpFile = Misc.createTemporaryFile();
    Stream tmpStream = new Stream(tmpFile, "rw");
   // tmpFile.deleteOnExit();
    try {
//      int i = 0;
      for (Item item : items) {
        // System.err.println(item.name.get());
        JarEntry entry = new JarEntry(item.name.get());
        
        jout.putNextEntry(entry);
        
        if (!entry.isDirectory()) {
          Stream s; 
          long length, position;
          if (item.symbol != null) {
            // patched or added symbol
            tmpStream.seek(0);
            item.symbol.write(tmpStream, null);
            length = tmpStream.getFilePointer();
            position = 0;
            s = tmpStream;
          } else {
            s = item.file;
            position = item.position;
            length = item.length;     
          }
          
          s.seek(position);
//          System.out.printf("%s %s %s %s\n", 
//              s == tmpStream ? "c" : "=", item.name, position, length);
          while (length > 0) {
            int chunk = s.read(buffer, 0, length < buffer.length ? (int) length : buffer.length);
            jout.write(buffer, 0, chunk);
            length -= chunk;
          }
        }
        jout.closeEntry();
//        if (i % 1000 == 0)
//          System.out.print('.');
//        i++;
      }
      jout.close();
//      System.out.println();
    }
    catch (IOException e) {
      jout.close();
      throw e;
    } 
    finally {
      tmpFile.delete();
    }
  }
  
  public int diff(JarArchive old, Stream out) throws IOException {
    return items.diff(old.items, out, null);
  }

  public void patch(Stream in) throws IOException {
    items.patch(in, null);
  }

  /**
   * Program entry point for JAR patching utilities.
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
            System.err.println("Arguments should be: <source JAR file> <target JAR file> <output patch file>");
            exCode = 1;
            break;
          }
          String fSrc = args[1], fTgt = args[2], fDiff = args[3];
          JarArchive cSrc = new JarArchive();
          JarArchive cTgt = new JarArchive();
          System.err.printf("Reading source version JAR -- '%s'\n", fSrc);
          cSrc.read(fSrc);
          System.err.printf("Reading target version JAR -- '%s'\n", fTgt);
          cTgt.read(fTgt);
          
          System.err.printf("Deriving patch             -- '%s'\n", fDiff);
          Stream diff = new Stream(fDiff, "rw");
          cTgt.diff(cSrc, diff);
          diff.close();  
          File fObj = new File(fDiff);
          long patchLength = fObj.length();

          if (patchLength == 0L) {
            fObj.delete();
            System.err.printf("No differences found. Patch file '%s' not generated.\n", fDiff);
          } else {
            System.err.printf("Patch '%s' generated: %s bytes.\n", fDiff, patchLength);
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
          
          // Read source JAR file.
          System.err.printf("Reading source JAR -- '%s'\n", fSrc);
          JarArchive jar = new JarArchive();
          jar.read(fSrc);
          
          // Apply patch.
          System.err.printf("Applying patch     -- '%s'\n", fDiff);
          Stream diff = new Stream(fDiff, "r");
          jar.patch(diff);
          diff.close(); 
          
          // Write target JAR file.
          System.err.printf("Writing target JAR -- '%s'\n", fTgt);
          jar.write(fTgt);
          System.err.printf("JAR file '%s' generated (%s bytes)\n", fTgt, new File(fTgt).length());
          break;
        }
        default:
          System.err.printf("Invalid command: '%s' :(\n", args[0]);
          exCode = 2;
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
