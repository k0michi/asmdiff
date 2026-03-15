package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.ParameterNode;

import java.util.Objects;

public class ParameterDiffer implements IDiffer<ParameterNode, ParameterPatch> {
  @Override
  public ParameterPatch diff(ParameterNode oldValue, ParameterNode newValue) {
    var stringDiffer = new ValueDiffer<String>();
    var integerDiffer = new ValueDiffer<Integer>();

    ParameterPatch patch = new ParameterPatch();
    patch.name = stringDiffer.diff(
            oldValue.name,
            newValue.name
    );
    patch.access = integerDiffer.diff(
            oldValue.access,
            newValue.access
    );
    return patch;
  }

  @Override
  public int distance(ParameterNode oldValue, ParameterNode newValue) {
    var stringDiffer = new ValueDiffer<String>();
    var integerDiffer = new ValueDiffer<Integer>();

    int distance = 0;
    distance += stringDiffer.distance(
            oldValue.name,
            newValue.name
    );
    distance += integerDiffer.distance(
            oldValue.access,
            newValue.access
    );
    return distance;
  }

  @Override
  public boolean canMatch(ParameterNode oldValue, ParameterNode newValue) {
    return Objects.equals(oldValue.name, newValue.name);
  }
}
