package com.koyomiji.asmweaver.json;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JsonWriterTest {
  @Test
  void test_0() {
    StringBuilder sb = new StringBuilder();
    JsonWriter writer = new JsonWriter(sb);
    JsonVisitor obj = writer.visitObject(null);
    obj.visitEnd();
    writer.visitEnd();

    Assertions.assertEquals("{}", sb.toString());
  }

  @Test
  void test_1() {
    StringBuilder sb = new StringBuilder();
    JsonWriter writer = new JsonWriter(sb);
    JsonVisitor obj = writer.visitObject(null);
    obj.visitString("key1", "value");
    obj.visitNumber("key2", 123);
    obj.visitBoolean("key3", true);
    obj.visitNull("key4");
    obj.visitArray("key5").visitEnd();
    obj.visitObject("key6").visitEnd();
    obj.visitEnd();
    writer.visitEnd();

    Assertions.assertEquals("{\"key1\":\"value\",\"key2\":123,\"key3\":true,\"key4\":null,\"key5\":[],\"key6\":{}}", sb.toString());
  }

  @Test
  void test_2() {
    StringBuilder sb = new StringBuilder();
    JsonWriter writer = new JsonWriter(sb);
    JsonVisitor arr = writer.visitArray(null);
    arr.visitString(null, "value");
    arr.visitNumber(null, 123);
    arr.visitBoolean(null, true);
    arr.visitNull(null);
    arr.visitArray(null).visitEnd();
    arr.visitObject(null).visitEnd();
    arr.visitEnd();
    writer.visitEnd();

    Assertions.assertEquals("[\"value\",123,true,null,[],{}]", sb.toString());
  }

  @Test
  void test_multiple_roots() {
    StringBuilder sb = new StringBuilder();
    JsonWriter writer = new JsonWriter(sb);
    writer.visitString("key1", "value");

    Assertions.assertThrows(IllegalStateException.class, () -> {
      writer.visitNumber("key2", 123);
    });
  }

  @Test
  void test_no_root() {
    StringBuilder sb = new StringBuilder();
    JsonWriter writer = new JsonWriter(sb);

    Assertions.assertThrows(IllegalStateException.class, () -> {
      writer.visitEnd();
    });
  }
}
