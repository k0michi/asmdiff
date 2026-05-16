package com.koyomiji.asmweaver.io;

import java.io.IOException;

@FunctionalInterface
public interface ElementReader<T> {
  T read() throws IOException;
}
