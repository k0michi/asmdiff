package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.AnnotationNode;

import java.util.Objects;

public class AnnotationDiffer implements IDiffer<AnnotationNode, AnnotationPatch> {
  @Override
  public AnnotationPatch diff(AnnotationNode oldValue, AnnotationNode newValue) {
    var stringDiffer = new ValueDiffer<String>();

    AnnotationPatch patch = new AnnotationPatch();
    patch.desc = stringDiffer.diff(
            oldValue == null ? null : oldValue.desc,
            newValue == null ? null : newValue.desc
    );
    patch.values = new ListDiffer<>(new AnnotationValueDiffer()).diff(
            ListHelper.orEmpty(oldValue == null ? null : oldValue.values),
            ListHelper.orEmpty(newValue == null ? null : newValue.values)
    );
    return patch;
  }

  @Override
  public int distance(AnnotationNode oldValue, AnnotationNode newValue) {
    var stringDiffer = new ValueDiffer<String>();

    int distance = 0;
    distance += stringDiffer.distance(
            oldValue == null ? null : oldValue.desc,
            newValue == null ? null : newValue.desc
    );
    distance += new ListDiffer<>(new AnnotationValueDiffer()).distance(
            ListHelper.orEmpty(oldValue == null ? null : oldValue.values),
            ListHelper.orEmpty(newValue == null ? null : newValue.values)
    );
    return distance;
  }

  @Override
  public boolean canMatch(AnnotationNode oldValue, AnnotationNode newValue) {
    return Objects.equals(oldValue.desc, newValue.desc);
  }
}
