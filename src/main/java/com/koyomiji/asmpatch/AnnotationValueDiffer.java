package com.koyomiji.asmpatch;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.List;
import java.util.Objects;

public class AnnotationValueDiffer implements IDiffer<Object, AnnotationValuePatch> {
  @Override
  public AnnotationValuePatch diff(Object oldValue, Object newValue) {
    if (oldValue instanceof List<?> && newValue instanceof List<?>) {
      var listDiffer = new ListDiffer<>(new AnnotationValueDiffer());
      AnnotationValuePatch patch = new AnnotationValuePatch();
      patch.annotationArrayValue = listDiffer.diff((List<Object>) oldValue, (List<Object>) newValue);
      return patch;
    } else if (oldValue instanceof AnnotationNode && newValue instanceof AnnotationNode) {
      var annotationDiffer = new AnnotationDiffer();
      AnnotationValuePatch patch = new AnnotationValuePatch();
      patch.annotationValue = annotationDiffer.diff((AnnotationNode) oldValue, (AnnotationNode) newValue);
      return patch;
    } else {
      var objectDiffer = new ValueDiffer<Object>();
      AnnotationValuePatch patch = new AnnotationValuePatch();
      patch.objectValue = objectDiffer.diff(oldValue, newValue);
      return patch;
    }
  }

  @Override
  public int distance(Object oldValue, Object newValue) {
    if (oldValue instanceof List<?> && newValue instanceof List<?>) {
      var listDiffer = new ListDiffer<>(new AnnotationValueDiffer());
      return listDiffer.distance((List<Object>) oldValue, (List<Object>) newValue);
    } else if (oldValue instanceof AnnotationNode && newValue instanceof AnnotationNode) {
      var annotationDiffer = new AnnotationDiffer();
      return annotationDiffer.distance((AnnotationNode) oldValue, (AnnotationNode) newValue);
    } else {
      var objectDiffer = new ValueDiffer<Object>();
      return objectDiffer.distance(oldValue, newValue);
    }
  }

  @Override
  public boolean canMatch(Object oldValue, Object newValue) {
    return true;
  }
}
