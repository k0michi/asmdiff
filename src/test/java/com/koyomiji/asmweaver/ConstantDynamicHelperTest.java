package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;

import java.io.*;

class ConstantDynamicHelperTest {
  @Test
  void test_readWrite() throws IOException {
    ConstantDynamic node = new ConstantDynamic("name", "desc", new Handle(1, "owner", "name", "desc", false), 1, "str");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    ConstantDynamicHelper.write(node, dos);

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    DataInputStream dis = new DataInputStream(bais);
    ConstantDynamic read = ConstantDynamicHelper.read(dis);

    Assertions.assertEquals(node, read);
  }
}
