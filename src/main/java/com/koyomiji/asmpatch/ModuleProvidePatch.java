package com.koyomiji.asmpatch;

public class ModuleProvidePatch {
  public ValuePatch<String> service;
  public ListPatch<String, ValuePatch<String>, String> providers;
}
