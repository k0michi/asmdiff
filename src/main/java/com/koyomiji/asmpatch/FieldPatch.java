package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.FieldNode;

public class FieldPatch implements IPatch<FieldNode> {
  public ValuePatch<Integer> access;
  public ValuePatch<String> name;
  public ValuePatch<String> desc;
  public ValuePatch<String> signature;
  public ValuePatch<Object> value;
  // TODO: annotations
  // TODO: attrs
}
