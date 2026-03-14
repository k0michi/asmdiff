package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.ModuleExportNode;
import org.objectweb.asm.tree.ModuleRequireNode;

public class ModulePatch {
  public ValuePatch<String> name;
  public ValuePatch<Integer> access;
  public ValuePatch<String> version;
  public ValuePatch<String> mainClass;
  public ListPatch<String, ValuePatch<String>, String> packages;
  public ListPatch<ModuleRequireNode, ModuleRequirePatch, String> requires;
  public ListPatch<ModuleExportNode, ModuleExportPatch, String> exports;
  public ListPatch<String, ValuePatch<String>, String> opens;
  public ListPatch<String, ValuePatch<String>, String> uses;
  public ListPatch<String, ValuePatch<String>, String> provides;

}
