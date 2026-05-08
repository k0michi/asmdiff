package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.TypePath;

class TypePathHelperTest {
  @Test
  void test_equals_0() {
    TypePath path1 = TypePath.fromString("0;1;2;[.*");
    TypePath path2 = TypePath.fromString("0;1;2;[.*");
    Assertions.assertTrue(TypePathHelper.equals(path1, path2));
  }

  @Test
  void test_equals_1() {
    TypePath path1 = TypePath.fromString("0;1;2;");
    TypePath path2 = TypePath.fromString("0;1;3;");
    Assertions.assertFalse(TypePathHelper.equals(path1, path2));
  }

  @Test
  void test_equals_2() {
    TypePath path1 = TypePath.fromString("0;1;2;");
    TypePath path2 = TypePath.fromString("0;1;");
    Assertions.assertFalse(TypePathHelper.equals(path1, path2));
  }

  @Test
  void test_equals_3() {
    TypePath path1 = TypePath.fromString("0;1;2;");
    Assertions.assertFalse(TypePathHelper.equals(path1, null));
  }

  @Test
  void test_equals_4() {
    TypePath path2 = TypePath.fromString("0;1;2;");
    Assertions.assertFalse(TypePathHelper.equals(null, path2));
  }

  @Test
  void test_equals_5() {
    Assertions.assertTrue(TypePathHelper.equals(null, null));
  }
}
