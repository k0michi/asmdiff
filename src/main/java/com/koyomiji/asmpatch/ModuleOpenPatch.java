package com.koyomiji.asmpatch;

public class ModuleOpenPatch implements IPatch<ModuleOpenPatch> {
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
