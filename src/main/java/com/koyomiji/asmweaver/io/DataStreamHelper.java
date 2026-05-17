package com.koyomiji.asmweaver.io;

import java.io.DataOutputStream;
import java.io.IOException;

public class DataStreamHelper {
  public static String readUTFNullable(CustomDataInput stream) throws java.io.IOException {
    boolean isNotNull = stream.readBoolean();

    if (isNotNull) {
      return stream.readUTF();
    } else {
      return null;
    }
  }

  public static void writeUTFNullable(DataOutputStream stream, String value) throws IOException {
    if (value == null) {
      stream.writeBoolean(false);
    } else {
      stream.writeBoolean(true);
      stream.writeUTF(value);
    }
  }
}
