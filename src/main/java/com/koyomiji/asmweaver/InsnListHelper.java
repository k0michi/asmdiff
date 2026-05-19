package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.HashCodeBuilder;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;

import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class InsnListHelper {
  public static boolean equals(InsnList list1, InsnList list2) {
    return equals(list1, list2, Objects::equals);
  }

  public static boolean equals(InsnList list1, InsnList list2, BiPredicate<LabelNode, LabelNode> labelEquals) {
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
      if (!AbstractInsnNodeHelper.equals(list1.get(i), list2.get(i), labelEquals)) {
        return false;
      }
    }

    return true;
  }

  public static int hashCode(InsnList list) {
    return hashCode(list, Objects::hashCode);
  }

  public static int hashCode(InsnList list, ToIntFunction<LabelNode> labelHashCode) {
    if (list == null) {
      return 0;
    }

    HashCodeBuilder builder = new HashCodeBuilder();

    for (int i = 0; i < list.size(); i++) {
      builder.append(list.get(i), insn-> AbstractInsnNodeHelper.hashCode(insn, labelHashCode));
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
