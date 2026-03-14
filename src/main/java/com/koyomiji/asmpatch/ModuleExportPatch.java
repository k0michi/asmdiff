package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.ModuleExportNode;

public class ModuleExportPatch {
  public ValuePatch<String> packaze;
  public ValuePatch<Integer> access;
  public ListPatch<String, ValuePatch<String>, String> modules;
}
