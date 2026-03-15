package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.FieldNode;

import java.util.Objects;

public class FieldDiffer implements IDiffer<FieldNode, FieldPatch> {
  @Override
  public FieldPatch diff(FieldNode oldValue, FieldNode newValue) {
    oldValue = oldValue == null ? new FieldNode(0, null, null, null, null) : oldValue;
    newValue = newValue == null ? new FieldNode(0, null, null, null, null) : newValue;

    var integerDiffer = new ValueDiffer<Integer>();
    var stringDiffer = new ValueDiffer<String>();
    var objectDiffer = new ValueDiffer<Object>();
    var annotationsDiffer = new ListDiffer<>(new AnnotationDiffer());
    var typeAnnotationsDiffer = new ListDiffer<>(new TypeAnnotationDiffer());

    FieldPatch patch = new FieldPatch();
    patch.access = integerDiffer.diff(oldValue.access, newValue.access);
    patch.name = stringDiffer.diff(oldValue.name, newValue.name);
    patch.desc = stringDiffer.diff(oldValue.desc, newValue.desc);
    patch.signature = stringDiffer.diff(oldValue.signature, newValue.signature);
    patch.value = objectDiffer.diff(oldValue.value, newValue.value);
    patch.visibleAnnotations = annotationsDiffer.diff(
            ListHelper.orEmpty(oldValue.visibleAnnotations),
            ListHelper.orEmpty(newValue.visibleAnnotations)
    );
    patch.invisibleAnnotations = annotationsDiffer.diff(
            ListHelper.orEmpty(oldValue.invisibleAnnotations),
            ListHelper.orEmpty(newValue.invisibleAnnotations)
    );
    patch.visibleTypeAnnotations = typeAnnotationsDiffer.diff(
            ListHelper.orEmpty(oldValue.visibleTypeAnnotations),
            ListHelper.orEmpty(newValue.visibleTypeAnnotations)
    );
    patch.invisibleTypeAnnotations = typeAnnotationsDiffer.diff(
            ListHelper.orEmpty(oldValue.invisibleTypeAnnotations),
            ListHelper.orEmpty(newValue.invisibleTypeAnnotations)
    );
    // TODO: attrs
    return patch;
  }

  @Override
  public int distance(FieldNode oldValue, FieldNode newValue) {
    oldValue = oldValue == null ? new FieldNode(0, null, null, null, null) : oldValue;
    newValue = newValue == null ? new FieldNode(0, null, null, null, null) : newValue;

    var integerDiffer = new ValueDiffer<Integer>();
    var stringDiffer = new ValueDiffer<String>();
    var objectDiffer = new ValueDiffer<Object>();
    var annotationsDiffer = new ListDiffer<>(new AnnotationDiffer());
    var typeAnnotationsDiffer = new ListDiffer<>(new TypeAnnotationDiffer());

    int distance = 0;
    distance += integerDiffer.distance(oldValue.access, newValue.access);
    distance += stringDiffer.distance(oldValue.name, newValue.name);
    distance += stringDiffer.distance(oldValue.desc, newValue.desc);
    distance += stringDiffer.distance(oldValue.signature, newValue.signature);
    distance += objectDiffer.distance(oldValue.value, newValue.value);
    distance += annotationsDiffer.distance(
            ListHelper.orEmpty(oldValue.visibleAnnotations),
            ListHelper.orEmpty(newValue.visibleAnnotations)
    );
    distance += annotationsDiffer.distance(
            ListHelper.orEmpty(oldValue.invisibleAnnotations),
            ListHelper.orEmpty(newValue.invisibleAnnotations)
    );
    distance += typeAnnotationsDiffer.distance(
            ListHelper.orEmpty(oldValue.visibleTypeAnnotations),
            ListHelper.orEmpty(newValue.visibleTypeAnnotations)
    );
    distance += typeAnnotationsDiffer.distance(
            ListHelper.orEmpty(oldValue.invisibleTypeAnnotations),
            ListHelper.orEmpty(newValue.invisibleTypeAnnotations)
    );
    return distance;
  }

  @Override
  public boolean canMatch(FieldNode oldValue, FieldNode newValue) {
    return Objects.equals(oldValue.name, newValue.name);
  }
}
