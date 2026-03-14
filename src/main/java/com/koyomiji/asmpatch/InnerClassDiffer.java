package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.InnerClassNode;

public class InnerClassDiffer implements IDiffer<InnerClassNode, InnerClassPatch> {
  @Override
  public InnerClassPatch diff(InnerClassNode oldValue, InnerClassNode newValue) {
    var stringDiffer = new ValueDiffer<String>();
    var integerDiffer = new ValueDiffer<Integer>();

    InnerClassPatch patch = new InnerClassPatch();
    patch.name = stringDiffer.diff(oldValue.name, newValue.name);
    patch.outerName = stringDiffer.diff(oldValue.outerName, newValue.outerName);
    patch.innerName = stringDiffer.diff(oldValue.innerName, newValue.innerName);
    patch.access = integerDiffer.diff(oldValue.access, newValue.access);
    return patch;
  }

  @Override
  public int distance(InnerClassNode oldValue, InnerClassNode newValue) {
    var stringDiffer = new ValueDiffer<String>();
    var integerDiffer = new ValueDiffer<Integer>();

    int distance = 0;
    distance += stringDiffer.distance(oldValue.name, newValue.name);
    distance += stringDiffer.distance(oldValue.outerName, newValue.outerName);
    distance += stringDiffer.distance(oldValue.innerName, newValue.innerName);
    distance += integerDiffer.distance(oldValue.access, newValue.access);
    return distance;
  }
}
