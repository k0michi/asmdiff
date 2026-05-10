package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.BiPersistentHashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BiPersistentHashMapTest {
  @Test
  void test_put_get_0() {
    BiPersistentHashMap<String, Integer> map1 = new BiPersistentHashMap<>();
    BiPersistentHashMap<String, Integer> map2 = map1.put("a", 1);
    Assertions.assertNull(map1.get("a"));
    Assertions.assertNull(map1.getKey(1));
    Assertions.assertEquals(1, map2.get("a"));
    Assertions.assertEquals("a", map2.getKey(1));
  }

  @Test
  void test_canPut_0() {
    BiPersistentHashMap<String, Integer> map1 = new BiPersistentHashMap<>();
    BiPersistentHashMap<String, Integer> map2 = map1.put("a", 1);
    BiPersistentHashMap<String, Integer> map3 = map2.put("b", 2);
    Assertions.assertTrue(map1.canPut("a", 1));
    Assertions.assertTrue(map1.canPut("a", 2));
    Assertions.assertTrue(map1.canPut("b", 2));

    Assertions.assertTrue(map2.canPut("a", 1));
    Assertions.assertFalse(map2.canPut("a", 2));
    Assertions.assertTrue(map2.canPut("b", 2));

    Assertions.assertTrue(map3.canPut("a", 1));
    Assertions.assertFalse(map3.canPut("a", 2));
    Assertions.assertTrue(map3.canPut("b", 2));
  }

  @Test
  void test_put_duplicateValue() {
    BiPersistentHashMap<String, Integer> map1 = new BiPersistentHashMap<>();
    BiPersistentHashMap<String, Integer> map2 = map1.put("a", 1);
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      map2.put("b", 1);
    });
  }
}
