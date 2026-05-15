package com.koyomiji.asmweaver;

import org.objectweb.asm.Handle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class HandleHelper {
  public static void write(Handle handle, DataOutputStream out) throws IOException {
    out.writeInt(handle.getTag());
    out.writeUTF(handle.getOwner());
    out.writeUTF(handle.getName());
    out.writeUTF(handle.getDesc());
    out.writeBoolean(handle.isInterface());
  }

  public static Handle read(DataInputStream in) throws IOException {
    int tag = in.readInt();
    String owner = in.readUTF();
    String name = in.readUTF();
    String desc = in.readUTF();
    boolean isInterface = in.readBoolean();
    return new Handle(tag, owner, name, desc, isInterface);
  }
}
