package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.InnerClassNode;

public class InnerClassPatch implements IPatch<InnerClassNode>  {
  public ValuePatch<String> name;
  public ValuePatch<String> outerName;
  public ValuePatch<String> innerName;
  public ValuePatch<Integer> access;

  @Override
  public boolean isSame() {
    return name.isSame()
        && outerName.isSame()
        && innerName.isSame()
        && access.isSame();
  }
}
