package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

class MapHelperTest {
  @Test
  void test_putIfAbsentAndTest_0() {
    HashMap<String, String> map = new HashMap<>();

    Assertions.assertTrue(MapHelper.putIfAbsentAndTest(map, "key1", "value1"));
    Assertions.assertTrue(MapHelper.putIfAbsentAndTest(map, "key1", "value1"));
    Assertions.assertFalse(MapHelper.putIfAbsentAndTest(map, "key1", "value2"));
  }
}
