package com.koyomiji.asmweaver.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class DataStreamHelperTest {
  @Test
  void test_readUTFNullable() throws java.io.IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

    DataStreamHelper.writeUTFNullable(dataOutputStream, "Hello, World!");
    DataStreamHelper.writeUTFNullable(dataOutputStream, null);

    ByteArrayInputStream byteArrayInputStream = new java.io.ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    BinaryReader dataInputStream = new BinaryReader(byteArrayInputStream);

    String value1 = DataStreamHelper.readUTFNullable(dataInputStream);
    String value2 = DataStreamHelper.readUTFNullable(dataInputStream);

    Assertions.assertEquals("Hello, World!", value1);
    Assertions.assertNull(value2);
  }
}
