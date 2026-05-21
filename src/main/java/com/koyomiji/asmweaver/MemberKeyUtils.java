package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.CustomDataInput;
import com.koyomiji.asmweaver.io.CustomDataOutput;

import java.io.IOException;

public class MemberKeyUtils {
  public static void write(MemberKey memberKey, CustomDataOutput out) throws IOException {
    out.writeUTF(memberKey.name);
    out.writeUTF(memberKey.desc);
  }

  public static MemberKey read(CustomDataInput in) throws IOException {
    return new MemberKey(
            in.readUTF(),
            in.readUTF()
    );
  }
}
