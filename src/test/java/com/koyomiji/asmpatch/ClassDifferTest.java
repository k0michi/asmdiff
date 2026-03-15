package com.koyomiji.asmpatch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ClassDifferTest {
  private ClassPatch diff(String oldClassPath, String newClassPath) {
    var differ = new ClassDiffer();
    var oldClassNode = TestUtil.readClassNode(oldClassPath);
    var newClassNode = TestUtil.readClassNode(newClassPath);
    return differ.diff(oldClassNode, newClassNode);
  }

  @Test
  void testDiffUnchanged() {
    var diff = diff("/C1.class", "/C1.class");
    Assertions.assertEquals(false, diff.version.changed);
    Assertions.assertEquals(false, diff.access.changed);
    Assertions.assertEquals(false, diff.name.changed);
    Assertions.assertEquals(false, diff.signature.changed);
    Assertions.assertEquals(false, diff.superName.changed);
    Assertions.assertEquals(0, diff.interfaces.entries.size());
  }

  @Test
  void testDiffName() {
    var diff = diff("/C1.class", "/C2.class");
    Assertions.assertEquals(true, diff.name.changed);
    Assertions.assertEquals("C1", diff.name.oldValue);
    Assertions.assertEquals("C2", diff.name.newValue);
  }

  @Test
  void testAddAnnotation() {
    var diff = diff("/C1.class", "/C3.class");
    Assertions.assertEquals(1, diff.invisibleAnnotations.entries.size());
    var annotationEntry = diff.invisibleAnnotations.entries.get(0);
    Assertions.assertSame(ListPatch.EntryType.ADD, annotationEntry.type);
    Assertions.assertEquals("LA1;", annotationEntry.newValue.desc);
  }

  @Test
  void testRemoveAnnotation() {
    var diff = diff("/C3.class", "/C1.class");
    Assertions.assertEquals(1, diff.invisibleAnnotations.entries.size());
    var annotationEntry = diff.invisibleAnnotations.entries.get(0);
    Assertions.assertSame(ListPatch.EntryType.REMOVE, annotationEntry.type);
  }

  @Test
  void testChangeAnnotation() {
    var diff = diff("/C3.class", "/C4.class");
    Assertions.assertEquals(2, diff.invisibleAnnotations.entries.size());
    var annotationEntry = diff.invisibleAnnotations.entries.get(0);
    Assertions.assertSame(ListPatch.EntryType.REMOVE, annotationEntry.type);
    annotationEntry = diff.invisibleAnnotations.entries.get(1);
    Assertions.assertSame(ListPatch.EntryType.ADD, annotationEntry.type);
  }

  @Test
  void testAddField() {
    var diff = diff("/C1.class", "/C7.class");
    Assertions.assertEquals(1, diff.fields.entries.size());
    var fieldEntry = diff.fields.entries.get(0);
    Assertions.assertSame(ListPatch.EntryType.ADD, fieldEntry.type);
    Assertions.assertEquals("f", fieldEntry.newValue.name);
  }

  @Test
  void testRemoveField() {
    var diff = diff("/C7.class", "/C1.class");
    Assertions.assertEquals(1, diff.fields.entries.size());
    var fieldEntry = diff.fields.entries.get(0);
    Assertions.assertSame(ListPatch.EntryType.REMOVE, fieldEntry.type);
  }

  @Test
  void testChangeField() {
    var diff = diff("/C7.class", "/C8.class");
    Assertions.assertEquals(2, diff.fields.entries.size());
    var fieldEntry = diff.fields.entries.get(0);
    Assertions.assertSame(ListPatch.EntryType.REMOVE, fieldEntry.type);
    fieldEntry = diff.fields.entries.get(1);
    Assertions.assertSame(ListPatch.EntryType.ADD, fieldEntry.type);
  }

  @Test
  void testAddRecordComponent() {
    var diff = diff("/R1.class", "/R2.class");
    Assertions.assertEquals(1, diff.recordComponents.entries.size());
    var recordComponentEntry = diff.recordComponents.entries.get(0);
    Assertions.assertSame(ListPatch.EntryType.ADD, recordComponentEntry.type);
    Assertions.assertEquals("x", recordComponentEntry.newValue.name);
  }
}
