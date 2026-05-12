package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.ModuleExportNode;
import org.objectweb.asm.tree.ModuleOpenNode;
import org.objectweb.asm.tree.ModuleProvideNode;
import org.objectweb.asm.tree.ModuleRequireNode;

public class ModuleDiff implements IDiff {
  public ListDiff<String> name;
  public ListDiff<Integer> access;
  public ListDiff<String> version;
  public ListDiff<String> mainClass;
  public ListDiff<String> packages;
  public ListDiff<ModuleRequireNode> requires;
  public ListDiff<ModuleExportNode> exports;
  public ListDiff<ModuleOpenNode> opens;
  public ListDiff<String> uses;
  public ListDiff<ModuleProvideNode> provides;

  @Override
  public boolean isEmpty() {
    return name.isEmpty()
            && access.isEmpty()
            && version.isEmpty()
            && mainClass.isEmpty()
            && packages.isEmpty()
            && requires.isEmpty()
            && exports.isEmpty()
            && opens.isEmpty()
            && uses.isEmpty()
            && provides.isEmpty();
  }
}
