package com.koyomiji.asmpatch;

public class ModuleOpenPatch {
  public ValuePatch<String> packaze;
  public ValuePatch<Integer> access;
  public ListPatch<String, ValuePatch<String>, String> modules;
}
