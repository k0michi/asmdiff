package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.ModuleExportNode;
import org.objectweb.asm.tree.ModuleOpenNode;
import org.objectweb.asm.tree.ModuleProvideNode;
import org.objectweb.asm.tree.ModuleRequireNode;

public class ModulePatch {
  public ValuePatch<String> name;
  public ValuePatch<Integer> access;
  public ValuePatch<String> version;
  public ValuePatch<String> mainClass;
  public ListPatch<String, ValuePatch<String>> packages;
  public ListPatch<ModuleRequireNode, ModuleRequirePatch> requires;
  public ListPatch<ModuleExportNode, ModuleExportPatch> exports;
  public ListPatch<ModuleOpenNode, ModuleOpenPatch> opens;
  public ListPatch<String, ValuePatch<String>> uses;
  public ListPatch<ModuleProvideNode, ModuleProvidePatch> provides;
}
