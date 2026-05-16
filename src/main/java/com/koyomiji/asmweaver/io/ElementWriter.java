package com.koyomiji.asmweaver.io;

import java.io.IOException;

@FunctionalInterface
public interface ElementWriter<T> {
  void write(T element) throws IOException;
}