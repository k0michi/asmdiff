package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.BinaryReader;
import com.koyomiji.asmweaver.io.BinaryWriter;
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
    BinaryWriter dos = new BinaryWriter(baos);
    ConstantDynamicHelper.write(node, dos);

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    BinaryReader dis = new BinaryReader(bais);
    ConstantDynamic read = ConstantDynamicHelper.read(dis);

    Assertions.assertEquals(node, read);
  }
}
