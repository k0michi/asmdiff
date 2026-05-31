package com.koyomiji.asmweaver.exp.aspa;

import aspa.core.Stream;

import java.io.File;
import java.io.IOException;

public class ByteArrayStream extends Stream {
  public ByteArrayStream(File file, String mode) throws IOException {
    super(file, mode);
  }
}
