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
}
