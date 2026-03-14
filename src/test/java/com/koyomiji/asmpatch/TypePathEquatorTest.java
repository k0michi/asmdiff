package com.koyomiji.asmpatch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.TypePath;

class TypePathEquatorTest {
  @Test
  void testEquals() {
    {
      TypePath a = TypePath.fromString("[.*0;");
      TypePath b = TypePath.fromString("[.*0;");
      var equator = new TypePathEquator();
      Assertions.assertTrue(equator.equals(a, b));
    }
  }

  @Test
  void testNotEquals() {
    {
      TypePath a = TypePath.fromString("[");
      TypePath b = TypePath.fromString(".");
      var equator = new TypePathEquator();
      Assertions.assertFalse(equator.equals(a, b));
    }

    {
      TypePath a = TypePath.fromString("[");
      TypePath b = TypePath.fromString("*");
      var equator = new TypePathEquator();
      Assertions.assertFalse(equator.equals(a, b));
    }

    {
      TypePath a = TypePath.fromString("[");
      TypePath b = TypePath.fromString("0;");
      var equator = new TypePathEquator();
      Assertions.assertFalse(equator.equals(a, b));
    }

    {
      TypePath a = TypePath.fromString("0;");
      TypePath b = TypePath.fromString("1;");
      var equator = new TypePathEquator();
      Assertions.assertFalse(equator.equals(a, b));
    }
  }
}
