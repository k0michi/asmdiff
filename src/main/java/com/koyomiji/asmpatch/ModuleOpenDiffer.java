package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.ModuleOpenNode;

import java.util.Objects;

public class ModuleOpenDiffer implements IDiffer<ModuleOpenNode, ModuleOpenPatch> {
  @Override
  public ModuleOpenPatch diff(ModuleOpenNode oldValue, ModuleOpenNode newValue) {
    var patch = new ModuleOpenPatch();
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
  public int distance(ModuleOpenNode oldValue, ModuleOpenNode newValue) {
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
  public boolean canMatch(ModuleOpenNode oldValue, ModuleOpenNode newValue) {
    return Objects.equals(oldValue.packaze, newValue.packaze);
  }
}
