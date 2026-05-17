package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.CustomDataInput;
import com.koyomiji.asmweaver.io.CustomDataOutput;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.function.Function;

public class NullableHelper {
  public static <T, U> U map(T value, Function<T, U> mapper) {
    if (value == null) {
      return null;
    }
    return mapper.apply(value);
  }

  public static <T> void write(T value, CustomDataOutput out, ListHelper.ElementWriter<T> writer) throws IOException {
    if (value == null) {
      out.writeBoolean(false);
      return;
    }

    out.writeBoolean(true);
    writer.write(value, out);
  }

  public static <T> T read(CustomDataInput in, ListHelper.ElementReader<T> reader) throws IOException {
    if (in.readBoolean()) {
      return reader.read(in);
    }

    return null;
  }
}
