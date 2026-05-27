package aspa.core;

import java.io.File;
import java.util.HashMap;

/**
 * Registry that handles the coupling of file associations with corresponding
 * (top-level) symbol classes.
 * 
 * @author Eduardo Marques
 * 
 */
public final class Registry {

  /**
   * Associate a file extension to a class.
   * 
   * @param extension
   *          File extension.
   * @param clazz
   *          Symbol class.
   */
  public static void associate(String extension, Class<? extends Symbol> clazz) {
    synchronized (registry) {
      registry.put(extension, clazz);
    }
  }

  /**
   * Get symbol class associated to a file.
   * 
   * @param extension
   *          File extension.
   * @return The associated class if one is defined, or <code>null</code>
   *         otherwise.
   */
  public static Class<? extends Symbol> association(String extension) {
    synchronized (registry) {
      return registry.get(extension);
    }
  }

  public static Class<? extends Symbol> association(File file) {
    String fileName = file.getName();

    int index = fileName.lastIndexOf('.');

    if (index == -1)
      return null;

    return association(fileName.substring(index + 1));
  }

  /**
   * Internal registry representation.
   */
  private static HashMap<String, Class<? extends Symbol>> registry = new HashMap<String, Class<? extends Symbol>>();

  static {
    // Setup built-in associations.
    associate("class", aspa.jvm.ClassFile.class);

  }
}
