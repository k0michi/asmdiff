package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.InnerClassNode;

import java.util.Objects;

public class InnerClassDiffer implements IDiffer<InnerClassNode, InnerClassPatch> {
  @Override
  public boolean canDiff(InnerClassNode oldValue, InnerClassNode newValue) {
    return Objects.equals(oldValue.name, newValue.name);
  }

  @Override
  public InnerClassPatch diff(InnerClassNode oldValue, InnerClassNode newValue) {
    if (!canDiff(oldValue, newValue)) {
      throw new IllegalStateException("Cannot diff values with different keys: " + oldValue + " vs " + newValue);
    }

    var stringDiffer = new ValueDiffer<String>();
    var integerDiffer = new ValueDiffer<Integer>();

    InnerClassPatch patch = new InnerClassPatch();
    patch.name = ValuePatch.unchanged();
    patch.outerName = stringDiffer.diff(oldValue.outerName, newValue.outerName);
    patch.innerName = stringDiffer.diff(oldValue.innerName, newValue.innerName);
    patch.access = integerDiffer.diff(oldValue.access, newValue.access);
    return patch;
  }
}
