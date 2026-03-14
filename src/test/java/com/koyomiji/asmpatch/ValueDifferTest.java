package com.koyomiji.asmpatch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

public class ValueDifferTest {
  @Test
  void testUnchanged() {
    var a = "1";
    var b = 1;
    var differ = new ValueDiffer<>(new IEquator<Object>() {
      @Override
      public boolean equals(Object a, Object b) {
        return Objects.equals(a, Integer.toString((Integer) b));
      }
    });

    var result = differ.diff(a, b);
    Assertions.assertEquals(false, result.changed);
  }

  @Test
  void testChanged() {
    var a = "1";
    var b = 2;
    var differ = new ValueDiffer<>(new IEquator<Object>() {
      @Override
      public boolean equals(Object a, Object b) {
        return Objects.equals(a, Integer.toString((Integer) b));
      }
    });

    var result = differ.diff(a, b);
    Assertions.assertEquals(true, result.changed);
  }
}
