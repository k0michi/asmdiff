package com.koyomiji.asmweaver.io;

import com.koyomiji.asmweaver.ListHelper;
import com.koyomiji.asmweaver.util.tuple.Triplet;

import java.io.DataInput;
import java.io.IOException;
import java.util.List;

public interface CustomDataInput extends DataInput {
  void consumeBeginList(String name) throws IOException;

  void consumeEndList() throws IOException;

  <T> List<T> readList(String name, ListHelper.ElementReader<T> reader) throws IOException;

  <T> T readNullable(ListHelper.ElementReader<T> reader) throws IOException;

  <T> T readVariant(Triplet<String, Integer, ListHelper.ElementReader<? extends T>>... cases) throws IOException;

  default int readVarInt() throws IOException {
    int value = 0;
    int shift = 0;
    while (shift < 32) {
      byte b = readByte();
      value |= (b & 0x7F) << shift;
      if ((b & 0x80) == 0) {
        return value;
      }
      shift += 7;
    }
    throw new IOException("Malformed VarInt: Too many bytes");
  }
}
