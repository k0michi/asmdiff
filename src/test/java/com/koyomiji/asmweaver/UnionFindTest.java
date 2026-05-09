package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.UnionFind;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UnionFindTest {
  @Test
  void test_0() {
    UnionFind<String> uf = new UnionFind<>();
    uf.addNode("A");
    uf.addNode("B");
    uf.addNode("C");

    Assertions.assertEquals("A", uf.find("A"));
    Assertions.assertEquals("B", uf.find("B"));
    Assertions.assertEquals("C", uf.find("C"));
  }

  @Test
  void test_1() {
    UnionFind<String> uf = new UnionFind<>();
    uf.addNode("A");
    uf.addNode("B");
    uf.addNode("C");

    uf.union("A", "B");

    Assertions.assertEquals(uf.find("A"), uf.find("B"));
    Assertions.assertNotEquals(uf.find("A"), uf.find("C"));
  }

  @Test
  void test_2() {
    UnionFind<String> uf = new UnionFind<>();
    uf.addNode("A");
    uf.addNode("B");
    uf.addNode("C");
    uf.addNode("D");

    uf.union("A", "B");
    uf.union("C", "D");
    uf.union("B", "C");

    Assertions.assertEquals(uf.find("A"), uf.find("D"));
  }
}
