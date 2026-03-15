package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.ModuleRequireNode;

import java.util.Objects;

public class ModuleRequireDiffer implements IDiffer<ModuleRequireNode, ModuleRequirePatch> {
  @Override
  public ModuleRequirePatch diff(ModuleRequireNode oldValue, ModuleRequireNode newValue) {
    var patch = new ModuleRequirePatch();
    patch.module = new ValueDiffer<String>().diff(
            oldValue.module,
            newValue.module
    );
    patch.access = new ValueDiffer<Integer>().diff(
            oldValue.access,
            newValue.access
    );
    patch.version = new ValueDiffer<String>().diff(
            oldValue.version,
            newValue.version
    );
    return patch;
  }

  @Override
  public int distance(ModuleRequireNode oldValue, ModuleRequireNode newValue) {
    int distance = 0;
    distance += new ValueDiffer<String>().distance(
            oldValue.module,
            newValue.module
    );
    distance += new ValueDiffer<Integer>().distance(
            oldValue.access,
            newValue.access
    );
    distance += new ValueDiffer<String>().distance(
            oldValue.version,
            newValue.version
    );
    return distance;
  }

  @Override
  public boolean canMatch(ModuleRequireNode oldValue, ModuleRequireNode newValue) {
    return Objects.equals(oldValue.module, newValue.module);
  }
}
