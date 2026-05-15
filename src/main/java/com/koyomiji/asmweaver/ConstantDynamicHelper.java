package com.koyomiji.asmweaver;

import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConstantDynamicHelper {
  public static void write(ConstantDynamic node, DataOutputStream out) throws IOException {
    out.writeUTF(node.getName());
    out.writeUTF(node.getDescriptor());
    HandleHelper.write(node.getBootstrapMethod(), out);
    out.writeInt(node.getBootstrapMethodArgumentCount());

    for (int i = 0; i < node.getBootstrapMethodArgumentCount(); i++) {
      ConstantHelper.write(node.getBootstrapMethodArgument(i), out);
    }
  }

  public static ConstantDynamic read(DataInputStream in) throws IOException {
    String name = in.readUTF();
    String descriptor = in.readUTF();
    Handle bootstrapMethod = HandleHelper.read(in);
    int bootstrapMethodArgumentCount = in.readInt();
    List<Object> bootstrapMethodArguments = new ArrayList<>();

    for (int i = 0; i < bootstrapMethodArgumentCount; i++) {
      bootstrapMethodArguments.add(ConstantHelper.read(in));
    }

    return new ConstantDynamic(name, descriptor, bootstrapMethod, bootstrapMethodArguments.toArray());
  }
}
