package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

class KeyedListDiffUtilsTest {
  class KeyedObject {
    int key;
    String value;

    public KeyedObject(int key, String value) {
      this.key = key;
      this.value = value;
    }

    public int getKey() {
      return key;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      KeyedObject that = (KeyedObject) o;
      return key == that.key && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(key, value);
    }
  }

  class KeyedObjectDiff implements IDiff {
    ListDiff<String> value;

    public KeyedObjectDiff(ListDiff<String> value) {
      this.value = value;
    }

    @Override
    public boolean isEmpty() {
      return value.isEmpty();
    }
  }

  private KeyedObjectDiff diffKeyedObject(KeyedObject o1, KeyedObject o2) {
    return new KeyedObjectDiff(ListDiffUtils.diff(List.of(o1.value), List.of(o2.value), String::equals));
  }

  private KeyedObject patchKeyedObject(KeyedObject o, KeyedObjectDiff diff) {
    return new KeyedObject(o.key, ListDiffUtils.getPatched(diff.value).get(0));
  }

  @Test
  void test_diff_0() {
    var list = List.of(
            new KeyedObject(1, "a"),
            new KeyedObject(2, "b"),
            new KeyedObject(3, "c")
    );
    var diff = KeyedListDiffUtils.diff(list, list,
            KeyedObject::getKey,
            this::diffKeyedObject
    );

    Assertions.assertTrue(diff.isEmpty());
    Assertions.assertEquals(3, diff.operations.size());
    Assertions.assertEquals(KeyedListDiff.Operation.Type.MATCH, diff.operations.get(0).type);
    Assertions.assertTrue(diff.operations.get(0).operandDiff.isEmpty());
    Assertions.assertEquals(KeyedListDiff.Operation.Type.MATCH, diff.operations.get(1).type);
    Assertions.assertTrue(diff.operations.get(1).operandDiff.isEmpty());
    Assertions.assertEquals(KeyedListDiff.Operation.Type.MATCH, diff.operations.get(2).type);
    Assertions.assertTrue(diff.operations.get(2).operandDiff.isEmpty());
  }

  @Test
  void test_diff_1() {
    var oldList = List.of(
            new KeyedObject(1, "a"),
            new KeyedObject(2, "b"),
            new KeyedObject(3, "c")
    );
    var newList = List.of(
            new KeyedObject(1, "a"),
            new KeyedObject(2, "x"),
            new KeyedObject(4, "d")
    );
    var diff = KeyedListDiffUtils.diff(oldList, newList,
            KeyedObject::getKey,
            this::diffKeyedObject
    );

    Assertions.assertEquals(4, diff.operations.size());
    Assertions.assertEquals(KeyedListDiff.Operation.Type.MATCH, diff.operations.get(0).type);
    Assertions.assertTrue(diff.operations.get(0).operandDiff.isEmpty());
    Assertions.assertEquals(KeyedListDiff.Operation.Type.MATCH, diff.operations.get(1).type);
    Assertions.assertFalse(diff.operations.get(1).operandDiff.isEmpty());
    Assertions.assertEquals(KeyedListDiff.Operation.Type.DELETE, diff.operations.get(2).type);
    Assertions.assertEquals(KeyedListDiff.Operation.Type.INSERT, diff.operations.get(3).type);
  }

  // Insertion only
  @Test
  void test_diff_2() {
    List<KeyedObject> list1 = List.of(
    );

    var list2 = List.of(
            new KeyedObject(1, "a"),
            new KeyedObject(2, "b"),
            new KeyedObject(3, "c")
    );

    var diff = KeyedListDiffUtils.diff(list1, list2,
            KeyedObject::getKey,
            this::diffKeyedObject
    );

    Assertions.assertEquals(3, diff.operations.size());
    Assertions.assertEquals(KeyedListDiff.Operation.Type.INSERT, diff.operations.get(0).type);
    Assertions.assertEquals(KeyedListDiff.Operation.Type.INSERT, diff.operations.get(1).type);
    Assertions.assertEquals(KeyedListDiff.Operation.Type.INSERT, diff.operations.get(2).type);
  }

  // Deletion only
  @Test
  void test_diff_3() {
    var list1 = List.of(
            new KeyedObject(1, "a"),
            new KeyedObject(2, "b"),
            new KeyedObject(3, "c")
    );

    List<KeyedObject> list2 = List.of(
    );

    var diff = KeyedListDiffUtils.diff(list1, list2,
            KeyedObject::getKey,
            this::diffKeyedObject
    );

    Assertions.assertEquals(3, diff.operations.size());
    Assertions.assertEquals(KeyedListDiff.Operation.Type.DELETE, diff.operations.get(0).type);
    Assertions.assertEquals(KeyedListDiff.Operation.Type.DELETE, diff.operations.get(1).type);
    Assertions.assertEquals(KeyedListDiff.Operation.Type.DELETE, diff.operations.get(2).type);
  }

  // Empty
  @Test
  void test_diff_4() {
    var list1 = List.<KeyedObject>of(
    );

    var diff = KeyedListDiffUtils.diff(list1, list1,
            KeyedObject::getKey,
            this::diffKeyedObject
    );

    Assertions.assertTrue(diff.isEmpty());
    Assertions.assertEquals(0, diff.operations.size());
  }

  @Test
  void test_patch_0() {
    var oldList = List.of(
            new KeyedObject(1, "a"),
            new KeyedObject(2, "b"),
            new KeyedObject(3, "c")
    );
    var newList = List.of(
            new KeyedObject(1, "a"),
            new KeyedObject(2, "x"),
            new KeyedObject(4, "d")
    );
    var diff = KeyedListDiffUtils.diff(oldList, newList,
            KeyedObject::getKey,
            this::diffKeyedObject
    );

    var patched = KeyedListDiffUtils.patch(oldList, diff,
            this::patchKeyedObject
    );

    Assertions.assertEquals(newList, patched);
  }
}
