package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.PersistentHashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PersistentHashMapTest {
  @Test
  void test_put_get_0() {
    PersistentHashMap<String, Integer> map1 = new PersistentHashMap<>();
    PersistentHashMap<String, Integer> map2 = map1.put("a", 1);
    Assertions.assertNull(map1.get("a"));
    Assertions.assertEquals(1, map2.get("a"));
  }

  @Test
  void test_put_get_1() {
    PersistentHashMap<String, Integer> map1 = new PersistentHashMap<>();
    PersistentHashMap<String, Integer> map2 = map1.put("a", 1);
    PersistentHashMap<String, Integer> map3 = map2.put("a", 2);
    Assertions.assertNull(map1.get("a"));
    Assertions.assertEquals(1, map2.get("a"));
    Assertions.assertEquals(2, map3.get("a"));
  }

  @Test
  void test_size_0() {
    PersistentHashMap<String, Integer> map = new PersistentHashMap<>();
    Assertions.assertEquals(0, map.size());
  }

  @Test
  void test_size_1() {
    PersistentHashMap<String, Integer> map1 = new PersistentHashMap<>();
    PersistentHashMap<String, Integer> map2 = map1.put("a", 1);
    PersistentHashMap<String, Integer> map3 = map2.put("b", 2);
    Assertions.assertEquals(0, map1.size());
    Assertions.assertEquals(1, map2.size());
    Assertions.assertEquals(2, map3.size());
  }

  @Test
  void test_size_2() {
    PersistentHashMap<String, Integer> map1 = new PersistentHashMap<>();
    PersistentHashMap<String, Integer> map2 = map1.put("a", 1);
    PersistentHashMap<String, Integer> map3 = map2.put("a", 2);
    Assertions.assertEquals(0, map1.size());
    Assertions.assertEquals(1, map2.size());
    Assertions.assertEquals(1, map3.size());
  }

  @Test
  void test_size_3() {
    PersistentHashMap<String, Integer> map1 = new PersistentHashMap<>();
    PersistentHashMap<String, Integer> map2 = map1.put("a", 1);
    PersistentHashMap<String, Integer> map3 = map2.put("b", 2);
    PersistentHashMap<String, Integer> map4 = map3.remove("a");
    Assertions.assertEquals(0, map1.size());
    Assertions.assertEquals(1, map2.size());
    Assertions.assertEquals(2, map3.size());
    Assertions.assertEquals(1, map4.size());
  }

  @Test
  void test_remove_0() {
    PersistentHashMap<String, Integer> map1 = new PersistentHashMap<>();
    PersistentHashMap<String, Integer> map2 = map1.put("a", 1);
    PersistentHashMap<String, Integer> map3 = map2.remove("a");
    Assertions.assertNull(map1.get("a"));
    Assertions.assertEquals(1, map2.get("a"));
    Assertions.assertNull(map3.get("a"));
  }

  @Test
  void test_equals_0() {
    PersistentHashMap<String, Integer> map1 = new PersistentHashMap<>();
    PersistentHashMap<String, Integer> map2 = new PersistentHashMap<>();
    Assertions.assertEquals(map1, map2);
  }

  @Test
  void test_equals_1() {
    PersistentHashMap<String, Integer> map1 = new PersistentHashMap<>();
    PersistentHashMap<String, Integer> map2 = map1.put("a", 1);
    Assertions.assertNotEquals(map1, map2);
  }

  @Test
  void test_equals_2() {
    PersistentHashMap<String, Integer> map1 = new PersistentHashMap<>();
    PersistentHashMap<String, Integer> map2 = map1.put("a", 1);
    PersistentHashMap<String, Integer> map3 = map1.put("a", 1);
    Assertions.assertEquals(map2, map3);
  }

  @Test
  void test_hashCode_0() {
    PersistentHashMap<String, Integer> map1 = new PersistentHashMap<>();
    PersistentHashMap<String, Integer> map2 = new PersistentHashMap<>();
    Assertions.assertEquals(map1.hashCode(), map2.hashCode());
  }

  @Test
  void test_hashCode_1() {
    PersistentHashMap<String, Integer> map1 = new PersistentHashMap<>();
    PersistentHashMap<String, Integer> map2 = map1.put("a", 1);
    PersistentHashMap<String, Integer> map3 = map1.put("a", 1);
    Assertions.assertEquals(map2.hashCode(), map3.hashCode());
  }

  class ObjectWithHashCode {
    private final int hashCode;

    public ObjectWithHashCode(int hashCode) {
      this.hashCode = hashCode;
    }

    @Override
    public int hashCode() {
      return hashCode;
    }
  }

  @Test
  void test_conflict_0() {
    PersistentHashMap<ObjectWithHashCode, Integer> map1 = new PersistentHashMap<>();
    ObjectWithHashCode key1 = new ObjectWithHashCode(42);
    ObjectWithHashCode key2 = new ObjectWithHashCode(42);
    PersistentHashMap<ObjectWithHashCode, Integer> map2 = map1.put(key1, 1);
    PersistentHashMap<ObjectWithHashCode, Integer> map3 = map2.put(key2, 2);
    Assertions.assertNull(map1.get(key1));
    Assertions.assertNull(map1.get(key2));
    Assertions.assertEquals(1, map2.get(key1));
    Assertions.assertNull(map2.get(key2));
    Assertions.assertEquals(2, map3.get(key2));
  }
}
