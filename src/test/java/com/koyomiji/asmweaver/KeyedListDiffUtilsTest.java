package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.BinaryReader;
import com.koyomiji.asmweaver.io.BinaryWriter;
import com.koyomiji.asmweaver.io.CustomDataInput;
import com.koyomiji.asmweaver.io.CustomDataOutput;
import com.koyomiji.asmweaver.util.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.IOException;
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

  class KeyedObjectDiff {
    ListDiff<String> value;

    public KeyedObjectDiff(ListDiff<String> value) {
      this.value = value;
    }
  }

  private KeyedObjectDiff diffKeyedObject(KeyedObject o1, KeyedObject o2) {
    var listDiff = ListDiffUtils.diff(List.of(o1.value), List.of(o2.value), String::equals);

    if (listDiff == null) {
      return null;
    }

    return new KeyedObjectDiff(listDiff);
  }

  private KeyedObject patchKeyedObject(KeyedObject o, KeyedObjectDiff diff) {
    if (diff == null) {
      return o;
    }

    return new KeyedObject(o.key, ListDiffUtils.patchNonNullableValue(o.value, diff.value));
  }

  private KeyedObjectDiff invertKeyedObjectDiff(KeyedObjectDiff diff) {
    if (diff == null) {
      return null;
    }

    return new KeyedObjectDiff(ListDiffUtils.invert(diff.value));
  }

  private Pair<KeyedObjectDiff, KeyedObjectDiff> commuteKeyedObjectDiffs(KeyedObjectDiff diff1, KeyedObjectDiff diff2) throws ConflictException {
    if (diff1 == null || diff2 == null) {
      return Pair.of(diff2, diff1);
    }

    var commuted = ListDiffUtils.commute(diff1.value, diff2.value, Objects::equals);
    return Pair.of(new KeyedObjectDiff(commuted.first), new KeyedObjectDiff(commuted.second));
  }

  private KeyedObjectDiff composeKeyedObjectDiffs(KeyedObjectDiff diff1, KeyedObjectDiff diff2) {
    if (diff1 == null) {
      return diff2;
    }

    if (diff2 == null) {
      return diff1;
    }

    return new KeyedObjectDiff(ListDiffUtils.compose(diff1.value, diff2.value, Objects::equals));
  }

  private int distanceKeyedObjectDiff(KeyedObjectDiff diff) {
    return ListDiffUtils.distance(diff.value);
  }

  private void writeDiff(KeyedObjectDiff diff, CustomDataOutput out) throws IOException {
    ListDiffUtils.write(diff.value, out, (v, s) -> s.writeUTF(v));
  }

  private KeyedObjectDiff readDiff(CustomDataInput in) throws IOException {
    return new KeyedObjectDiff(
            ListDiffUtils.read(in, DataInput::readUTF)
    );
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

    Assertions.assertNull(diff);
//    Assertions.assertEquals(3, diff.operations.size());
//    Assertions.assertEquals(KeyedListDiff.Operation.Type.MATCH, diff.operations.get(0).type);
//    Assertions.assertTrue(diff.operations.get(0).operandDiff.isEmpty());
//    Assertions.assertEquals(KeyedListDiff.Operation.Type.MATCH, diff.operations.get(1).type);
//    Assertions.assertTrue(diff.operations.get(1).operandDiff.isEmpty());
//    Assertions.assertEquals(KeyedListDiff.Operation.Type.MATCH, diff.operations.get(2).type);
//    Assertions.assertTrue(diff.operations.get(2).operandDiff.isEmpty());
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
    Assertions.assertNull(diff.operations.get(0).operandDiff);
    Assertions.assertEquals(KeyedListDiff.Operation.Type.MATCH, diff.operations.get(1).type);
    Assertions.assertNotNull(diff.operations.get(1).operandDiff);
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

    Assertions.assertNull(diff);
  }

  @Test
  void test_distance() {
    var list1 = List.of(
            new KeyedObject(1, "a"),
            new KeyedObject(2, "b"),
            new KeyedObject(3, "c")
    );

    var list2 = List.<KeyedObject>of(
            new KeyedObject(1, "a"),
            new KeyedObject(2, "b"),
            new KeyedObject(3, "c")
    );

    var diff = KeyedListDiffUtils.diff(list1, list2,
            KeyedObject::getKey,
            this::diffKeyedObject
    );

    Assertions.assertEquals(0, KeyedListDiffUtils.distance(diff, this::distanceKeyedObjectDiff));
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

  @Test
  void test_invert() {
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

    var inverted = KeyedListDiffUtils.invert(diff, this::invertKeyedObjectDiff);
    var patched = KeyedListDiffUtils.patch(newList, inverted,
            this::patchKeyedObject
    );

    Assertions.assertEquals(oldList, patched);
  }

  @Test
  void test_readWrite() throws IOException {
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

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BinaryWriter dos = new BinaryWriter(baos);
    KeyedListDiffUtils.write(
            diff,
            dos,
            (k, s) -> s.writeInt(k),
            (v, s) -> {
              s.writeInt(v.key);
              s.writeUTF(v.value);
            },
            this::writeDiff
    );

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    BinaryReader dis = new BinaryReader(bais);
    var readDiff = KeyedListDiffUtils.read(
            dis,
            CustomDataInput::readInt,
            s -> new KeyedObject(s.readInt(), s.readUTF()),
            this::readDiff
    );

    var patched = KeyedListDiffUtils.patch(oldList, readDiff,
            this::patchKeyedObject
    );

    Assertions.assertEquals(newList, patched);
  }

  @Test
  void test_commute_0() throws ConflictException {
    var list1 = List.of(
            new KeyedObject(1, "a")
    );
    var list2 = List.of(
            new KeyedObject(1, "a"),
            new KeyedObject(2, "b")
    );
    var list3 = List.of(
            new KeyedObject(1, "a"),
            new KeyedObject(2, "b"),
            new KeyedObject(3, "c")
    );

    var diff12 = KeyedListDiffUtils.diff(list1, list2,
            KeyedObject::getKey,
            this::diffKeyedObject
    );
    var diff23 = KeyedListDiffUtils.diff(list2, list3,
            KeyedObject::getKey,
            this::diffKeyedObject
    );

    var commuted = KeyedListDiffUtils.commute(
            diff12,
            diff23,
            this::commuteKeyedObjectDiffs,
            this::diffKeyedObject
    );

    // 1 -> 1 3
    Assertions.assertEquals(2, commuted.first.operations.size());
    Assertions.assertEquals(KeyedListDiff.Operation.Type.MATCH, commuted.first.operations.get(0).type);
    Assertions.assertEquals(KeyedListDiff.Operation.Type.INSERT, commuted.first.operations.get(1).type);

    // 1 3 -> 1 2 3
    Assertions.assertEquals(3, commuted.second.operations.size());
    Assertions.assertEquals(KeyedListDiff.Operation.Type.MATCH, commuted.second.operations.get(0).type);
    Assertions.assertEquals(KeyedListDiff.Operation.Type.INSERT, commuted.second.operations.get(1).type);
    Assertions.assertEquals(KeyedListDiff.Operation.Type.MATCH, commuted.second.operations.get(2).type);
  }

  @Test
  void test_compose_0() {
    var list1 = List.of(
            new KeyedObject(1, "a")
    );
    var list2 = List.of(
            new KeyedObject(1, "a"),
            new KeyedObject(2, "b")
    );
    var list3 = List.of(
            new KeyedObject(1, "a"),
            new KeyedObject(2, "b"),
            new KeyedObject(3, "c")
    );

    var diff12 = KeyedListDiffUtils.diff(list1, list2,
            KeyedObject::getKey,
            this::diffKeyedObject
    );
    var diff23 = KeyedListDiffUtils.diff(list2, list3,
            KeyedObject::getKey,
            this::diffKeyedObject
    );

    var computed = KeyedListDiffUtils.compose(
            diff12,
            diff23,
            this::composeKeyedObjectDiffs,
            this::patchKeyedObject,
            this::invertKeyedObjectDiff
    );

    Assertions.assertEquals(3, computed.operations.size());
    Assertions.assertEquals(KeyedListDiff.Operation.Type.MATCH, computed.operations.get(0).type);
    Assertions.assertEquals(KeyedListDiff.Operation.Type.INSERT, computed.operations.get(1).type);
    Assertions.assertEquals(KeyedListDiff.Operation.Type.INSERT, computed.operations.get(2).type);
  }

  @Test
  void test_diffIndexed_0() {
    var list1 = List.of("a");
    var list2 = List.of("b");

    var diff = KeyedListDiffUtils.diffIndexed(
            list1,
            list2,
            (v1, v2) -> ListDiffUtils.diffNonNullableValue(v1, v2, String::equals)
    );

    Assertions.assertEquals(1, diff.operations.size());
    Assertions.assertEquals(KeyedListDiff.Operation.Type.MATCH, diff.operations.get(0).type);

    Assertions.assertEquals(2, diff.operations.get(0).operandDiff.operations.size());
  }

  @Test
  void test_diffIndexed_1() {
    var list1 = List.of("a", "a");
    var list2 = List.of("a", "b");

    var diff = KeyedListDiffUtils.diffIndexed(
            list1,
            list2,
            (v1, v2) -> ListDiffUtils.diffNonNullableValue(v1, v2, String::equals)
    );

    Assertions.assertEquals(2, diff.operations.size());
    Assertions.assertEquals(KeyedListDiff.Operation.Type.MATCH, diff.operations.get(0).type);
    Assertions.assertEquals(KeyedListDiff.Operation.Type.MATCH, diff.operations.get(1).type);

    Assertions.assertEquals(0, ListDiffUtils.distance(diff.operations.get(0).operandDiff));
    Assertions.assertEquals(2, ListDiffUtils.distance(diff.operations.get(1).operandDiff));

//    Assertions.assertEquals(1, diff.operations.get(0).operandDiff.operations.size());
//    Assertions.assertEquals(2, diff.operations.get(1).operandDiff.operations.size());
  }
}
