package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.LabelNode;

import java.util.Objects;

public class IndexedLabelNode extends LabelNode {
  public int index;

  public IndexedLabelNode(int index) {
    this.index = index;
  }

  public IndexedLabelNode(LabelNode labelNode, int index) {
    super(labelNode.getLabel());
    this.index = index;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    IndexedLabelNode that = (IndexedLabelNode) o;
    return index == that.index;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(index);
  }
}
