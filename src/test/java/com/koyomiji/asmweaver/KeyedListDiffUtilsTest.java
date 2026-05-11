package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

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

  @Test
  void test_diff_0() {
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
    Assertions.assertEquals(KeyedListDiff.Operation.Type.MATCH, diff.operations.get(1).type);
    Assertions.assertEquals(KeyedListDiff.Operation.Type.DELETE, diff.operations.get(2).type);
    Assertions.assertEquals(KeyedListDiff.Operation.Type.INSERT, diff.operations.get(3).type);
  }
}
