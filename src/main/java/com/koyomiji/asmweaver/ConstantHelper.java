package com.koyomiji.asmweaver;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ConstantHelper {
  public static void write(Object arg, DataOutputStream out) throws IOException {
    if (arg instanceof Integer) {
      out.writeInt(0);
      out.writeInt((Integer) arg);
    } else if (arg instanceof Float) {
      out.writeInt(1);
      out.writeFloat((Float) arg);
    } else if (arg instanceof Long) {
      out.writeInt(2);
      out.writeLong((Long) arg);
    } else if (arg instanceof Double) {
      out.writeInt(3);
      out.writeDouble((Double) arg);
    } else if (arg instanceof String) {
      out.writeInt(4);
      out.writeUTF((String) arg);
    } else if (arg instanceof Type) {
      out.writeInt(5);
      out.writeUTF(((Type) arg).getDescriptor());
    } else if (arg instanceof Handle) {
      out.writeInt(6);
      HandleHelper.write((Handle) arg, out);
    } else {
      throw new IllegalArgumentException("Unsupported bootstrap argument type: " + arg.getClass());
    }
  }

  public static Object read(DataInputStream in) throws IOException {
    switch (in.readInt()) {
      case 0:
        return in.readInt();
      case 1:
        return in.readFloat();
      case 2:
        return in.readLong();
      case 3:
        return in.readDouble();
      case 4:
        return in.readUTF();
      case 5:
        return Type.getType(in.readUTF());
      case 6:
        return HandleHelper.read(in);
      default:
        throw new IllegalArgumentException("Unsupported bootstrap argument type: " + in.readInt());
    }
  }
}