package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.ModuleRequireNode;

public class ModuleRequirePatch implements IPatch<ModuleRequireNode> {
  public ValuePatch<String> moduleName;
  public ValuePatch<Integer> access;
  public ValuePatch<String> version;
}
