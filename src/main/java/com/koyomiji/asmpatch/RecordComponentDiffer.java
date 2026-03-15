package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.RecordComponentNode;

import java.util.Objects;

public class RecordComponentDiffer implements IDiffer<RecordComponentNode, RecordComponentPatch> {
  @Override
  public RecordComponentPatch diff(RecordComponentNode oldValue, RecordComponentNode newValue) {
    oldValue = oldValue == null ? new RecordComponentNode(null, null, null) : oldValue;
    newValue = newValue == null ? new RecordComponentNode(null, null, null) : newValue;

    var stringDiffer = new ValueDiffer<String>();
    var annotationsDiffer = new ListDiffer<>(new AnnotationDiffer());
    var typeAnnotationDiffer = new ListDiffer<>(new TypeAnnotationDiffer());

    var patch = new RecordComponentPatch();
    patch.name = stringDiffer.diff(oldValue.name, newValue.name);
    patch.descriptor = stringDiffer.diff(oldValue.descriptor, newValue.descriptor);
    patch.signature = stringDiffer.diff(oldValue.signature, newValue.signature);
    patch.visibleAnnotations = annotationsDiffer.diff(oldValue.visibleAnnotations, newValue.visibleAnnotations);
    patch.invisibleAnnotations = annotationsDiffer.diff(oldValue.invisibleAnnotations, newValue.invisibleAnnotations);
    patch.visibleTypeAnnotations = typeAnnotationDiffer.diff(oldValue.visibleTypeAnnotations, newValue.visibleTypeAnnotations);
    patch.invisibleTypeAnnotations = typeAnnotationDiffer.diff(oldValue.invisibleTypeAnnotations, newValue.invisibleTypeAnnotations);
    return patch;
  }

  @Override
  public int distance(RecordComponentNode oldValue, RecordComponentNode newValue) {
    oldValue = oldValue == null ? new RecordComponentNode(null, null, null) : oldValue;
    newValue = newValue == null ? new RecordComponentNode(null, null, null) : newValue;

    var stringDiffer = new ValueDiffer<String>();
    var annotationsDiffer = new ListDiffer<>(new AnnotationDiffer());
    var typeAnnotationDiffer = new ListDiffer<>(new TypeAnnotationDiffer());

    int distance = 0;
    distance += stringDiffer.distance(oldValue.name, newValue.name);
    distance += stringDiffer.distance(oldValue.descriptor, newValue.descriptor);
    distance += stringDiffer.distance(oldValue.signature, newValue.signature);
    distance += annotationsDiffer.distance(
            ListHelper.orEmpty(oldValue.visibleAnnotations),
            ListHelper.orEmpty(newValue.visibleAnnotations)
    );
    distance += annotationsDiffer.distance(
            ListHelper.orEmpty(oldValue.invisibleAnnotations),
            ListHelper.orEmpty(newValue.invisibleAnnotations)
    );
    distance += typeAnnotationDiffer.distance(
            ListHelper.orEmpty(oldValue.visibleTypeAnnotations),
            ListHelper.orEmpty(newValue.visibleTypeAnnotations)
    );
    distance += typeAnnotationDiffer.distance(
            ListHelper.orEmpty(oldValue.invisibleTypeAnnotations),
            ListHelper.orEmpty(newValue.invisibleTypeAnnotations)
    );
    return distance;
  }

  @Override
  public boolean canMatch(RecordComponentNode oldValue, RecordComponentNode newValue) {
    return Objects.equals(oldValue.name, newValue.name);
  }
}
