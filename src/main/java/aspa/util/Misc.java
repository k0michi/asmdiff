package aspa.util;

import java.lang.reflect.Array;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * Class providing miscellaneous utility methods.
 * 
 * @author Eduardo Marques
 */
public final class Misc {
  /**
   * Create an instance of a given type. This is a wrapper for
   * Class.newInstance() with a different contract w.r.t. exception behavior. It
   * converts checked exceptions thrown by the latter into unchecked exceptions,
   * avoiding typically unnecessary try/catch blocks.
   * 
   * @param clazz
   *          class of objects
   * @return created object
   */
  public static <T> T create(Class<T> clazz) {
    try {
      return clazz.newInstance();
    } catch (InstantiationException e) {
      throw new RuntimeException(clazz.getName(), e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(clazz.getName(), e);
    }
  }

  /**
   * Create an array of a given type. This is purely a wrapper to
   * Array.newIntance() in the reflection API.
   * 
   * @param clazz
   *          class of objects
   * @param length
   *          array length
   * @return created array
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] create(Class<T> clazz, int length) {
    return (T[]) Array.newInstance(clazz, length);
  }
  
  public static File createTemporaryFile() throws IOException { 
    return File.createTempFile("aspa", null); 
  }
  
  public static File saveToTemporaryFile(InputStream in) throws IOException {
    File file = createTemporaryFile();
    BufferedInputStream bin = new BufferedInputStream(in);
    BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(file));
    int b; 
    while ( (b = bin.read()) != -1 ) {
      bout.write(b);
    }
    bout.close();
    return file;
  }
}
