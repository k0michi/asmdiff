package com.koyomiji.asmpatch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ClassDifferTest {
  @Test
  void testDiffUnchanged() {
    var differ = new ClassDiffer();
    var oldClassNode = TestUtil.readClassNode("/C1.class");
    var newClassNode = TestUtil.readClassNode("/C1.class");
    var diff = differ.diff(oldClassNode, newClassNode);
    Assertions.assertEquals(false, diff.version.changed);
    Assertions.assertEquals(false, diff.access.changed);
    Assertions.assertEquals(false, diff.name.changed);
    Assertions.assertEquals(false, diff.signature.changed);
    Assertions.assertEquals(false, diff.superName.changed);
    Assertions.assertEquals(0, diff.interfaces.entries.size());
  }

  @Test
  void testDiffName() {
    var differ = new ClassDiffer();
    var oldClassNode = TestUtil.readClassNode("/C1.class");
    var newClassNode = TestUtil.readClassNode("/C2.class");
    var diff = differ.diff(oldClassNode, newClassNode);
    Assertions.assertEquals(true, diff.name.changed);
    Assertions.assertEquals("C1", diff.name.oldValue);
    Assertions.assertEquals("C2", diff.name.newValue);
  }

  @Test
  void testAddAnnotation() {
    var differ = new ClassDiffer();
    var oldClassNode = TestUtil.readClassNode("/C1.class");
    var newClassNode = TestUtil.readClassNode("/C3.class");
    var diff = differ.diff(oldClassNode, newClassNode);
    Assertions.assertEquals(1, diff.invisibleAnnotations.entries.size());
    var annotationEntry = diff.invisibleAnnotations.entries.get(0);
    Assertions.assertSame(ListPatch.EntryType.ADD, annotationEntry.type);
    Assertions.assertEquals("LA1;", annotationEntry.newValue.desc);
  }

  @Test
  void testRemoveAnnotation() {
    var differ = new ClassDiffer();
    var oldClassNode = TestUtil.readClassNode("/C3.class");
    var newClassNode = TestUtil.readClassNode("/C1.class");
    var diff = differ.diff(oldClassNode, newClassNode);
    Assertions.assertEquals(1, diff.invisibleAnnotations.entries.size());
    var annotationEntry = diff.invisibleAnnotations.entries.get(0);
    Assertions.assertSame(ListPatch.EntryType.REMOVE, annotationEntry.type);
  }

  @Test
  void testChangeAnnotation() {
    var differ = new ClassDiffer();
    var oldClassNode = TestUtil.readClassNode("/C3.class");
    var newClassNode = TestUtil.readClassNode("/C4.class");
    var diff = differ.diff(oldClassNode, newClassNode);
    Assertions.assertEquals(1, diff.invisibleAnnotations.entries.size());
    var annotationEntry = diff.invisibleAnnotations.entries.get(0);
    Assertions.assertSame(ListPatch.EntryType.MATCH, annotationEntry.type);
    Assertions.assertEquals("LA2;", annotationEntry.patch.desc.newValue);
  }
}
