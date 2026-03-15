package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.ModuleExportNode;

import java.util.Objects;

public class ModuleExportDiffer implements IDiffer<ModuleExportNode, ModuleExportPatch> {
  @Override
  public ModuleExportPatch diff(ModuleExportNode oldValue, ModuleExportNode newValue) {
    var patch = new ModuleExportPatch();
    patch.packaze = new ValueDiffer<String>().diff(
            oldValue.packaze,
            newValue.packaze
    );
    patch.access = new ValueDiffer<Integer>().diff(
            oldValue.access,
            newValue.access
    );
    patch.modules = new ListDiffer<>(new ValueDiffer<String>()).diff(
            oldValue.modules,
            newValue.modules
    );
    return patch;
  }

  @Override
  public int distance(ModuleExportNode oldValue, ModuleExportNode newValue) {
    int distance = 0;
    distance += new ValueDiffer<String>().distance(
            oldValue.packaze,
            newValue.packaze
    );
    distance += new ValueDiffer<Integer>().distance(
            oldValue.access,
            newValue.access
    );
    distance += new ListDiffer<>(new ValueDiffer<String>()).distance(
            oldValue.modules,
            newValue.modules
    );
    return distance;
  }

  @Override
  public boolean canMatch(ModuleExportNode oldValue, ModuleExportNode newValue) {
    return Objects.equals(oldValue.packaze, newValue.packaze);
  }
}
