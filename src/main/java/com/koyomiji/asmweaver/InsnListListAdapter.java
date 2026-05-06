package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.*;

public class InsnListListAdapter extends AbstractList<AbstractInsnNode> {
  private final InsnList insnList;

  public InsnListListAdapter(InsnList insnList) {
    this.insnList = insnList;
  }

  @Override
  public AbstractInsnNode get(int index) {
    return insnList.get(index);
  }

  @Override
  public int size() {
    return insnList.size();
  }

  @Override
  public AbstractInsnNode set(int index, AbstractInsnNode element) {
    AbstractInsnNode old = insnList.get(index);
    insnList.set(old, element);
    return old;
  }

  @Override
  public void add(int index, AbstractInsnNode element) {
    if (index == insnList.size()) {
      insnList.add(element);
    } else {
      insnList.insert(insnList.get(index), element);
    }
  }

  @Override
  public AbstractInsnNode remove(int index) {
    AbstractInsnNode old = insnList.get(index);
    insnList.remove(old);
    return old;
  }
}
