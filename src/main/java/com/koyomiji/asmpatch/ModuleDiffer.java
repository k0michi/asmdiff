package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.*;

import java.util.Objects;

public class ModuleDiffer implements IDiffer<ModuleNode, ModulePatch> {
  @Override
  public ModulePatch diff(ModuleNode oldValue, ModuleNode newValue) {
    var stringDiffer = new ValueDiffer<String>();
    var stringsDiffer = new ListDiffer<>(stringDiffer);
    var integerDiffer = new ValueDiffer<Integer>();
    var patch = new ModulePatch();

    patch.name = stringDiffer.diff(oldValue.name, newValue.name);
    patch.access = integerDiffer.diff(oldValue.access, newValue.access);
    patch.version = stringDiffer.diff(oldValue.version, newValue.version);
    patch.mainClass = stringDiffer.diff(oldValue.version, newValue.mainClass);
    patch.packages = stringsDiffer.diff(
            ListHelper.orEmpty(oldValue.packages),
            ListHelper.orEmpty(newValue.packages)
    );
    patch.requires = new ListDiffer<>(new ModuleRequireDiffer()).diff(
            ListHelper.orEmpty(oldValue.requires),
            ListHelper.orEmpty(newValue.requires)
    );
    patch.exports = new ListDiffer<>(new ModuleExportDiffer()).diff(
            ListHelper.orEmpty(oldValue.exports),
            ListHelper.orEmpty(newValue.exports)
    );
    patch.opens = new ListDiffer<>(new ModuleOpenDiffer()).diff(
            ListHelper.orEmpty(oldValue.opens),
            ListHelper.orEmpty(newValue.opens)
    );
    patch.uses = stringsDiffer.diff(
            ListHelper.orEmpty(oldValue.uses),
            ListHelper.orEmpty(newValue.uses)
    );
    patch.provides = new ListDiffer<>(new ModuleProvideDiffer()).diff(
            ListHelper.orEmpty(oldValue.provides),
            ListHelper.orEmpty(newValue.provides)
    );
    return patch;
  }

  @Override
  public int distance(ModuleNode oldValue, ModuleNode newValue) {
    int distance = 0;
    var stringDiffer = new ValueDiffer<String>();
    var stringsDiffer = new ListDiffer<>(stringDiffer);
    var integerDiffer = new ValueDiffer<Integer>();

    distance += stringDiffer.distance(oldValue.name, newValue.name);
    distance += integerDiffer.distance(oldValue.access, newValue.access);
    distance += stringDiffer.distance(oldValue.version, newValue.version);
    distance += stringDiffer.distance(oldValue.version, newValue.version);
    distance += stringsDiffer.distance(
            ListHelper.orEmpty(oldValue.packages),
            ListHelper.orEmpty(newValue.packages)
    );
    distance += new ListDiffer<>(new ModuleRequireDiffer()).distance(
            ListHelper.orEmpty(oldValue.requires),
            ListHelper.orEmpty(newValue.requires)
    );
    distance += new ListDiffer<>(new ModuleExportDiffer()).distance(
            ListHelper.orEmpty(oldValue.exports),
            ListHelper.orEmpty(newValue.exports)
    );
    distance += new ListDiffer<>(new ModuleOpenDiffer()).distance(
            ListHelper.orEmpty(oldValue.opens),
            ListHelper.orEmpty(newValue.opens)
    );
    distance += stringsDiffer.distance(
            ListHelper.orEmpty(oldValue.uses),
            ListHelper.orEmpty(newValue.uses)
    );
    distance += new ListDiffer<>(new ModuleProvideDiffer()).distance(
            ListHelper.orEmpty(oldValue.provides),
            ListHelper.orEmpty(newValue.provides)
    );
    return distance;
  }

  @Override
  public boolean canMatch(ModuleNode oldValue, ModuleNode newValue) {
    return Objects.equals(oldValue.name, newValue.name);
  }
}
