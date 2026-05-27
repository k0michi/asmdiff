package aspa.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Patching stream.
 * 
 * @author Eduardo Marques
 * 
 */
public  class Stream extends RandomAccessFile {
  private long         marker = -1L;
  protected boolean      readOnly;
  private final String mode;
  private final File   file;
  private Report       report;

  public Stream(File file, String mode) throws IOException {
    super(file, mode);
    this.file = file;
    this.mode = mode;
    readOnly = mode.equals("r");
  }

  public Stream(String file, String mode) throws IOException {
    this(new File(file), mode);
  }

  public void truncate() throws IOException {
    if (!readOnly)
     setLength(getFilePointer()); 
  }
  
  @Override
  public void close() throws IOException {
    if (report != null) {
      report.close();
      System.out.println("report file closed");
    }
    truncate();
    super.close();
  }


  

  public long mark(boolean reset) throws IOException {
    if (reset)
      marker = getFilePointer();
    return marker;
  }

  public long mark() {
    return marker;
  }

  public void writeOffset(int off) throws IOException {
    do {
      int v = off & 0x7F;
      off = off >> 7;

      if (off != 0)
        v |= 0x80;

      write(v);
    } while (off != 0);
  }

  public int readOffset() throws IOException {
    int off = 0;
    int bits = 0;
    int v;

    do {
      v = read();
      off |= ((v & 0x7F) << bits);
      bits += 7;
    } while ((v & 0x80) != 0);
    return off;
  }

  public Report report() {
    if (report == null) {
      report = new Report(file.getPath() + ".report." + mode);
      System.out.println("report file created");
      System.out.println(file.getPath() + ".report." + mode);
    }
    return report;
  }

  
  public OutputStream asOutputStream() {
    return new OutputStream() {
      public void write(int b) throws IOException {
        Stream.this.write(b);
      }
    };
  }
  
  public InputStream asInputStream() {
    return new InputStream() {   
      @Override
      public int read() throws IOException {
        return Stream.this.read(); 
      }
    };
    
  }
  
  public static class Stats implements Cloneable {
    public int unchanged, added, removed, patched;
  }
  
  /**
   * Inner class for patch reports.
   * 
   * @author Eduardo Marques
   */
  public final class Report {
    private final PrintStream out;
    private int               indentation;
    private static final int  INDENTATION_SPACE = 2;
    private static final int  REPORT_LEVEL = 0;
   
    private TreeMap<String,Stats> stats = new TreeMap<String,Stats>();
    
    
    public Stats getStats(String id) {
      Stats s = stats.get(id);
      if (s == null) {
        s = new Stats();
        stats.put(id, s);
      }
      return s;
    }
    public Stats getStats(Class<?> clazz) {
      return getStats(clazz.getName());
    }
    
    public boolean logUnchanged(Class<?> clazz, int n) {
      getStats(clazz).unchanged += n;
      return true;
    }
    public boolean logPatched(Class<?> clazz, int n) {
      getStats(clazz).patched += n;
      return true;
    }
    public boolean logPatched(String id, int n) {
      getStats(id).patched += n;
      return true;
    }
    public boolean logRemoved(Class<?> clazz, int n) {
      getStats(clazz).removed += n;
      return true;
    }
    public boolean logAdded(Class<?> clazz, int n) {
      getStats(clazz).added += n;
      return true;
    }
    
    public Report(String file) {
      try {
        out = new PrintStream(new FileOutputStream(file));
        indentation = 0;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    private void indent() throws IOException {
      for (int i = 0; i != indentation; i++)
        out.print(' ');
    }

    public boolean line(String fmt, Object... args) throws IOException {
      if (REPORT_LEVEL > 0) {
        out.printf("%8d ", getFilePointer());
        indent();
        out.printf(fmt, args);
        out.print('\n');
        out.flush();
      }
      return true;
    }

    public boolean beginSection(Object o, String fmt, Object... args)
        throws IOException {
      if (REPORT_LEVEL > 0) {
        line("%s -- %s -- %s", o.getClass().getSimpleName(), o.toString(),
            String.format(fmt, args));
        line("{");
        indentation += INDENTATION_SPACE;
      }
      return true;
    }

    public boolean endSection() throws IOException {
      if (REPORT_LEVEL > 0) {
        indentation -= INDENTATION_SPACE;
        line("}");
      }
      return true;
    }

    public boolean close() {
      for (Map.Entry<String, Stats> entry : stats.entrySet()) {
        Stats s = entry.getValue();
        out.printf("%s|%d|%d|%d|%d\n", entry.getKey(),
                          s.unchanged, s.patched, s.added, s.removed);
      }
      out.flush();
      out.close();
      return true;
    }
  }
}
