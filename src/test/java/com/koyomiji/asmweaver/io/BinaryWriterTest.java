package com.koyomiji.asmweaver.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

class BinaryWriterTest {
  @Test
  void test_1() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BinaryWriter bw = new BinaryWriter(baos);
    bw.beginList("list");
    bw.writeBoolean(true);
    bw.writeByte(1);
    bw.writeShort(1);
    bw.writeChar('a');
    bw.writeInt(1);
    bw.writeLong(1);
    bw.writeFloat(1.0f);
    bw.writeDouble(1.0);
    bw.writeUTF("abc");
    bw.endList();

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    BinaryReader br = new BinaryReader(bais);
    br.consumeBeginList("list");
    Assertions.assertEquals(true, br.readBoolean());
    Assertions.assertEquals(1, br.readByte());
    Assertions.assertEquals(1, br.readShort());
    Assertions.assertEquals('a', br.readChar());
    Assertions.assertEquals(1, br.readInt());
    Assertions.assertEquals(1, br.readLong());
    Assertions.assertEquals(1.0f, br.readFloat());
    Assertions.assertEquals(1.0, br.readDouble());
    Assertions.assertEquals("abc", br.readUTF());
    br.consumeEndList();
  }
}
