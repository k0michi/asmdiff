package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.InjectiveHashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class InjectiveHashMapTest {
  @Test
  void testPut() {
    var map = new InjectiveHashMap<String, Integer>();
    map.put("a", 1);
    map.put("b", 2);
    Assertions.assertEquals(1, map.get("a"));
    Assertions.assertEquals(2, map.get("b"));
  }

  @Test
  void testPutDuplicateValue() {
    var map = new InjectiveHashMap<String, Integer>();
    map.put("a", 1);
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      map.put("b", 1);
    });
  }

  @Test
  void testRemove() {
    var map = new InjectiveHashMap<String, Integer>();
    map.put("a", 1);
    map.put("b", 2);
    map.remove("a");
    Assertions.assertNull(map.get("a"));
    Assertions.assertEquals(2, map.get("b"));
  }
}
