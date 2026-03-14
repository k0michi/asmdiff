package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.TypeAnnotationNode;

import java.util.Objects;

public class TypeAnnotationDiffer implements IDiffer<TypeAnnotationNode, TypeAnnotationPatch> {
  @Override
  public TypeAnnotationPatch diff(TypeAnnotationNode oldValue, TypeAnnotationNode newValue) {
    var stringDiffer = new ValueDiffer<String>();
    var integerDiffer = new ValueDiffer<Integer>();
    var typePathDiffer = new TypePathDiffer();

    TypeAnnotationPatch patch = new TypeAnnotationPatch();
    patch.desc = stringDiffer.diff(
            oldValue == null ? null : oldValue.desc,
            newValue == null ? null : newValue.desc
    );
    patch.values = new ListDiffer<>(new AnnotationValueDiffer()).diff(
            ListHelper.orEmpty(oldValue == null ? null : oldValue.values),
            ListHelper.orEmpty(newValue == null ? null : newValue.values)
    );
    patch.typeRef = integerDiffer.diff(
            oldValue == null ? null : oldValue.typeRef,
            newValue == null ? null : newValue.typeRef
    );
    patch.typePath = typePathDiffer.diff(
            oldValue == null ? null : oldValue.typePath,
            newValue == null ? null : newValue.typePath
    );

    return patch;
  }

  @Override
  public int distance(TypeAnnotationNode oldValue, TypeAnnotationNode newValue) {
    int distance = 0;

    var stringDiffer = new ValueDiffer<String>();
    var integerDiffer = new ValueDiffer<Integer>();
    var typePathDiffer = new TypePathDiffer();

    TypeAnnotationPatch patch = new TypeAnnotationPatch();
    distance += stringDiffer.distance(
            oldValue == null ? null : oldValue.desc,
            newValue == null ? null : newValue.desc
    );
    distance += new ListDiffer<>(new AnnotationValueDiffer()).distance(
            ListHelper.orEmpty(oldValue == null ? null : oldValue.values),
            ListHelper.orEmpty(newValue == null ? null : newValue.values)
    );
    distance += integerDiffer.distance(
            oldValue == null ? null : oldValue.typeRef,
            newValue == null ? null : newValue.typeRef
    );
    distance += typePathDiffer.distance(
            oldValue == null ? null : oldValue.typePath,
            newValue == null ? null : newValue.typePath
    );

    return distance;
  }

  @Override
  public boolean canMatch(TypeAnnotationNode oldValue, TypeAnnotationNode newValue) {
    return Objects.equals(oldValue.desc, newValue.desc);
  }
}
