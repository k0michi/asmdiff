package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

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

  public static <T> void verifyEquals(Supplier<List<T>> nodesSupplier, BiPredicate<T, T> equals) {
    Assertions.assertTrue(equals.test(null, null), "null compared with null should be true");

    List<T> nodes1 = nodesSupplier.get();
    List<T> nodes2 = nodesSupplier.get();

    int size = nodes1.size();

    for (int i = 0; i < size; i++) {
      T node1 = nodes1.get(i);
      T node2 = nodes2.get(i);

      Assertions.assertTrue(equals.test(node1, node2), "Index " + i + " should equal its counterpart instance");
      Assertions.assertFalse(equals.test(node1, null), "Index " + i + " compared with null should be false");
      Assertions.assertFalse(equals.test(null, node2), "null compared with index " + i + " should be false");

      for (int j = 0; j < size; j++) {
        if (i != j) {
          Assertions.assertFalse(equals.test(node1, nodes1.get(j)),
                  String.format("Index %d and %d should not be equal", i, j));
        }
      }
    }
  }

  public static <T> void verifyHashCode(Supplier<List<T>> nodesSupplier, ToIntFunction<T> hashCode) {
    Assertions.assertEquals(
            hashCode.applyAsInt(null),
            hashCode.applyAsInt(null),
            "Hash code for null should be consistent"
    );

    List<T> nodes = nodesSupplier.get();

    for (int i = 0; i < nodes.size(); i++) {
      T node = nodes.get(i);
      int expectedHashCode = hashCode.applyAsInt(node);
      Assertions.assertEquals(expectedHashCode, hashCode.applyAsInt(node), "Hash code should be consistent for index " + i);
    }
  }

  @FunctionalInterface
  public interface Writer<T> {
    void write(T value, DataOutputStream out) throws IOException;
  }

  @FunctionalInterface
  public interface Reader<T> {
    T read(DataInputStream in) throws IOException;
  }

  public static <T> void verifyRoundTrip(Supplier<List<T>> nodesSupplier, Writer<T> writer, Reader<T> reader, BiPredicate<T, T> equals) throws IOException {
    List<T> nodes = nodesSupplier.get();

    for (int i = 0; i < nodes.size(); i++) {
      T original = nodes.get(i);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try (DataOutputStream out = new DataOutputStream(baos)) {
        writer.write(original, out);
      }

      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      T restored;
      try (DataInputStream in = new DataInputStream(bais)) {
        restored = reader.read(in);
      }

      Assertions.assertTrue(equals.test(original, restored), String.format("Round-trip should restore the original value for index %d", i));
    }
  }
}
