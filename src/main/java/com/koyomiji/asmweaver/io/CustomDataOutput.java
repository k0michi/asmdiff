package com.koyomiji.asmweaver.io;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;

public interface CustomDataOutput extends DataOutput {
  void beginList(String name) throws IOException;
  void endList() throws IOException;
  <T> void writeList(String name, Collection<T> collection, ElementWriter<T> writer) throws IOException;
  <T> void writeNullable(T element, ElementWriter<T> writer) throws IOException;
}
