package aspa.core;

import java.util.TreeMap;

import aspa.general.BinaryData;
import aspa.jar.JarArchive;
import aspa.jvm.ClassFile;

public class FileTypes {

  static { 
    singleton = new FileTypes();
    defineAssociation(".class", ClassFile.class);
  }
  
  private static final FileTypes singleton; 
  
  public static void defineAssociation(String ext, Class<? extends Root> clazz) {
    singleton.associate(ext, clazz);
  }
  
  public static Root createSymbol(String filename) {
    return singleton.create(filename);
  }
  
  private final TreeMap<String, Class<? extends Root>> assoc;


  private FileTypes() {
    assoc = new TreeMap<>();
  }
  
  private synchronized void associate(String ext, Class<? extends Root> clazz) {
    assoc.put(ext, clazz);
  }
  
  private synchronized Root create(String filename) {
    int index = filename.lastIndexOf('.');
    Class<? extends Root> clazz = BinaryData.class;
    if (index != -1) {
      String ext = filename.substring(index);
      Class<? extends Root> aclazz = assoc.get(ext);
      if (aclazz != null)
        clazz = aclazz;
    } 
    try {
      return clazz.newInstance();   
    }
    catch (IllegalAccessException | InstantiationException e) {
      throw new FileTypeAssociationException(clazz.getName(), e);
    }
  }
}
