package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.BinaryReader;
import com.koyomiji.asmweaver.io.BinaryWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Handle;

import java.io.*;

class HandleHelperTest {
  @Test
  void test_readWrite() throws IOException {
    Handle handle = new Handle(1, "owner", "name", "desc", false);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BinaryWriter dos = new BinaryWriter(baos);
    HandleHelper.write(handle, dos);

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    BinaryReader dis = new BinaryReader(bais);
    Handle read = HandleHelper.read(dis);

    Assertions.assertEquals(handle, read);
  }
}
