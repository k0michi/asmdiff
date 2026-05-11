package com.koyomiji.asmweaver;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class TestUtils {
  public static byte[] readResource(String resourcePath) {
    try (var inputStream = TestUtils.class.getResourceAsStream(resourcePath)) {
      if (inputStream == null) {
        throw new RuntimeException("Resource not found: " + resourcePath);
      }
      return inputStream.readAllBytes();
    } catch (Exception e) {
      throw new RuntimeException("Failed to read resource: " + resourcePath, e);
    }
  }

  public static ClassNode readClassNode(String resourcePath) {
    byte[] classBytes = readResource(resourcePath);
    ClassNode classNode = new ClassNode();
    var classReader = new ClassReader(classBytes);
    classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
    return classNode;
  }
}
