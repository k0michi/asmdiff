package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.InnerClassNode;

public class InnerClassPatch {
  public ValuePatch<String> name;
  public ValuePatch<String> outerName;
  public ValuePatch<String> innerName;
  public ValuePatch<Integer> access;
}
