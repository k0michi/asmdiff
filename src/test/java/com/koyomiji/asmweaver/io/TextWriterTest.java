package com.koyomiji.asmweaver.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

class TextWriterTest {
  @Test
  void test_0() throws IOException {
    StringBuilder sb = new StringBuilder();
    TextWriter tw = new TextWriter(sb);
    tw.beginList("list");
    tw.endList();
    Assertions.assertEquals("(list)", sb.toString());
  }

  @Test
  void test_1() throws IOException {
    StringBuilder sb = new StringBuilder();
    TextWriter tw = new TextWriter(sb);
    tw.beginList("list");
    tw.writeBoolean(true);
    tw.writeByte(1);
    tw.writeShort(1);
    tw.writeChar('a');
    tw.writeInt(1);
    tw.writeLong(1);
    tw.writeFloat(1.0f);
    tw.writeDouble(1.0);
    tw.writeUTF("abc");
    tw.endList();

    StringReader sr = new StringReader(sb.toString());
    TextReader tr = new TextReader(sr);
    tr.consumeBeginList("list");
    Assertions.assertEquals(true, tr.readBoolean());
    Assertions.assertEquals(1, tr.readByte());
    Assertions.assertEquals(1, tr.readShort());
    Assertions.assertEquals('a', tr.readChar());
    Assertions.assertEquals(1, tr.readInt());
    Assertions.assertEquals(1, tr.readLong());
    Assertions.assertEquals(1.0f, tr.readFloat());
    Assertions.assertEquals(1.0, tr.readDouble());
    Assertions.assertEquals("abc", tr.readUTF());
    tr.consumeEndList();
  }
}
