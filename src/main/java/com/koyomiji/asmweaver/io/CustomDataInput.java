package com.koyomiji.asmweaver.io;

import java.io.DataInput;
import java.io.IOException;
import java.util.List;

public interface CustomDataInput extends DataInput {
  void consumeBeginList(String name) throws IOException;

  void consumeEndList() throws IOException;

  <T> List<T> readList(String name, ElementReader<T> reader) throws IOException;

  <T> T readNullable(ElementReader<T> reader) throws IOException;
}
