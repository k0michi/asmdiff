package com.koyomiji.asmpatch;

import org.objectweb.asm.TypePath;

public class TypePathDiffer implements IDiffer<TypePath, ValuePatch<TypePath>> {
  private ValueDiffer<TypePath> differ = new ValueDiffer<>(new TypePathEquator());

  @Override
  public ValuePatch<TypePath> diff(TypePath oldValue, TypePath newValue) {
    return differ.diff(oldValue, newValue);
  }

  @Override
  public int distance(TypePath oldValue, TypePath newValue) {
    return differ.distance(oldValue, newValue);
  }

  @Override
  public boolean canMatch(TypePath oldValue, TypePath newValue) {
    return differ.canMatch(oldValue, newValue);
  }
}
