package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.AutoIncrementBiHashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AutoIncrementBiHashMapTest {
  @Test
  void test_0() {
    AutoIncrementBiHashMap<String> map = new AutoIncrementBiHashMap<>();
    Assertions.assertEquals(0, map.get("a"));
    Assertions.assertEquals(1, map.get("b"));

    map.put("c", 5);
    Assertions.assertEquals(5, map.get("c"));

    map.put("d", 6);
    Assertions.assertEquals(7, map.get("e"));
  }

  @Test
  void test_1() {
    AutoIncrementBiHashMap<String> map = new AutoIncrementBiHashMap<>();
    map.get("a");
    AutoIncrementBiHashMap<String> map2 = new AutoIncrementBiHashMap<>(map);
    Assertions.assertEquals(0, map2.get("a"));
    Assertions.assertEquals(1, map2.get("b"));
  }
}
