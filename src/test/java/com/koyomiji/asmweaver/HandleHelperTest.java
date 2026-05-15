package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Handle;

import java.io.*;

class HandleHelperTest {
  @Test
  void test_readWrite() throws IOException {
    Handle handle = new Handle(1, "owner", "name", "desc", false);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    HandleHelper.write(handle, dos);

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    DataInputStream dis = new DataInputStream(bais);
    Handle read = HandleHelper.read(dis);

    Assertions.assertEquals(handle, read);
  }
}
