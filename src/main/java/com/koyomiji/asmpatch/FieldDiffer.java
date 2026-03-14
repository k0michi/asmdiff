package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.FieldNode;

import java.util.Objects;

public class FieldDiffer implements IDiffer<FieldNode, FieldPatch> {
  @Override
  public FieldPatch diff(FieldNode oldValue, FieldNode newValue) {
    var integerDiffer = new ValueDiffer<Integer>();
    var stringDiffer = new ValueDiffer<String>();
    var objectDiffer = new ValueDiffer<Object>();

    FieldPatch patch = new FieldPatch();
    patch.access = integerDiffer.diff(oldValue.access, newValue.access);
    patch.name = stringDiffer.diff(oldValue.name, newValue.name);
    patch.desc = stringDiffer.diff(oldValue.desc, newValue.desc);
    patch.signature = stringDiffer.diff(oldValue.signature, newValue.signature);
    patch.value = objectDiffer.diff(oldValue.value, newValue.value);
    // TODO: annotations
    // TODO: attrs
    return patch;
  }

  @Override
  public int distance(FieldNode oldValue, FieldNode newValue) {
    var integerDiffer = new ValueDiffer<Integer>();
    var stringDiffer = new ValueDiffer<String>();
    var objectDiffer = new ValueDiffer<Object>();

    int distance = 0;
    distance += integerDiffer.distance(oldValue.access, newValue.access);
    distance += stringDiffer.distance(oldValue.name, newValue.name);
    distance += stringDiffer.distance(oldValue.desc, newValue.desc);
    distance += stringDiffer.distance(oldValue.signature, newValue.signature);
    distance += objectDiffer.distance(oldValue.value, newValue.value);
    return distance;
  }

  @Override
  public boolean canMatch(FieldNode oldValue, FieldNode newValue) {
    return Objects.equals(oldValue.name, newValue.name);
  }
}
