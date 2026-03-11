package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.ModuleExportNode;

public class ModuleExportPatch implements IPatch<ModuleExportNode> {
  public ValuePatch<String> packaze;
  public ValuePatch<Integer> access;
  public ListPatch<String, ValuePatch<String>, String> modules;

  @Override
  public boolean isSame() {
    return packaze.isSame()
        && access.isSame()
        && modules.isSame();
  }
}
