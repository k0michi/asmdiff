package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.HashCodeBuilder;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.List;

public class InsnListHelper {
  public static boolean equals(InsnList list1, InsnList list2) {
    if (list1 == list2) {
      return true;
    }

    if (list1 == null || list2 == null) {
      return false;
    }

    if (list1.getClass() != list2.getClass()) {
      return false;
    }

    if (list1.size() != list2.size()) {
      return false;
    }

    for (int i = 0; i < list1.size(); i++) {
      if (!AbstractInsnNodeHelper.equals(list1.get(i), list2.get(i))) {
        return false;
      }
    }

    return true;
  }

  public static int hashCode(InsnList list) {
    if (list == null) {
      return 0;
    }

    HashCodeBuilder builder = new HashCodeBuilder();

    for (int i = 0; i < list.size(); i++) {
      builder.append(list.get(i), AbstractInsnNodeHelper::hashCode);
    }

    return builder.build();
  }

  public static InsnList fromList(List<AbstractInsnNode> insns) {
    InsnList insnList = new InsnList();

    for (AbstractInsnNode insn : insns) {
      insnList.add(insn);
    }

    return insnList;
  }
}
