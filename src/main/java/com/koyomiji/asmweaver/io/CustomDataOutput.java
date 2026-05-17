package com.koyomiji.asmweaver.io;

import com.koyomiji.asmweaver.ListHelper;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;

public interface CustomDataOutput extends DataOutput {
  void beginList(String name) throws IOException;

  void endList() throws IOException;

  <T> void writeList(String name, Collection<T> collection, ListHelper.ElementWriter<T> writer) throws IOException;

  <T> void writeNullable(T element, ListHelper.ElementWriter<T> writer) throws IOException;

  <T> void writeVariant(String name, int id, T element, ListHelper.ElementWriter<T> writer) throws IOException;
}
