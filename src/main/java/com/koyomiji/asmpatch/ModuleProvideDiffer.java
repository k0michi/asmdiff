package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.ModuleProvideNode;

import java.util.Objects;

public class ModuleProvideDiffer implements IDiffer<ModuleProvideNode, ModuleProvidePatch> {
  @Override
  public ModuleProvidePatch diff(ModuleProvideNode oldValue, ModuleProvideNode newValue) {
    var patch = new ModuleProvidePatch();
    patch.service = new ValueDiffer<String>().diff(
            oldValue.service,
            newValue.service
    );
    patch.providers = new ListDiffer<String, ValuePatch<String>, String>(new ValueDiffer<String>()).diff(
            oldValue.providers,
            newValue.providers
    );
    return patch;
  }

  @Override
  public int distance(ModuleProvideNode oldValue, ModuleProvideNode newValue) {
    int distance = 0;
    distance += new ValueDiffer<String>().distance(
            oldValue.service,
            newValue.service
    );
    distance += new ListDiffer<String, ValuePatch<String>, String>(new ValueDiffer<String>()).distance(
            oldValue.providers,
            newValue.providers
    );
    return distance;
  }

  @Override
  public boolean canMatch(ModuleProvideNode oldValue, ModuleProvideNode newValue) {
    return Objects.equals(oldValue.service, newValue.service);
  }
}
