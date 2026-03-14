package com.koyomiji.asmpatch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;

class HandleEquatorTest {
  @Test
  void testEquals() {
    {
      var a = new Handle(Opcodes.H_GETFIELD, "a", "b", "c", false);
      var b = new Handle(Opcodes.H_GETFIELD, "a", "b", "c", false);
      var equator = new HandleEquator();
      Assertions.assertTrue(equator.equals(a, b));
    }
  }

  @Test
  void testNotEquals() {
    {
      var a = new Handle(Opcodes.H_GETSTATIC, "a", "b", "c", false);
      var b = new Handle(Opcodes.H_GETFIELD, "a", "b", "c", false);
      var equator = new HandleEquator();
      Assertions.assertFalse(equator.equals(a, b));
    }

    {
      var a = new Handle(Opcodes.H_GETSTATIC, "a", "b", "c", false);
      var b = new Handle(Opcodes.H_GETSTATIC, "b", "b", "c", false);
      var equator = new HandleEquator();
      Assertions.assertFalse(equator.equals(a, b));
    }

    {
      var a = new Handle(Opcodes.H_GETSTATIC, "a", "b", "c", false);
      var b = new Handle(Opcodes.H_GETSTATIC, "a", "c", "c", false);
      var equator = new HandleEquator();
      Assertions.assertFalse(equator.equals(a, b));
    }

    {
      var a = new Handle(Opcodes.H_GETSTATIC, "a", "b", "c", false);
      var b = new Handle(Opcodes.H_GETSTATIC, "a", "b", "d", false);
      var equator = new HandleEquator();
      Assertions.assertFalse(equator.equals(a, b));
    }

    {
      var a = new Handle(Opcodes.H_GETSTATIC, "a", "b", "c", false);
      var b = new Handle(Opcodes.H_GETSTATIC, "a", "b", "c", true);
      var equator = new HandleEquator();
      Assertions.assertFalse(equator.equals(a, b));
    }
  }
}
