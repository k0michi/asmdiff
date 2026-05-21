package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.AutoIncrementBiHashMap;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;

public class InsnListHelper {
  public static boolean equalsNormalizeLabels(List<AbstractInsnNode> list1, List<AbstractInsnNode> list2) {
    Map<LabelNode, LabelNode> labelMap = new HashMap<>();
    return equals(list1, list2, (l1, l2) -> MapHelper.putIfAbsentAndTest(labelMap, l1, l2));
  }

  //  public static boolean equals(InsnList list1, InsnList list2, BiPredicate<LabelNode, LabelNode> labelEquals) {
  public static boolean equals(List<AbstractInsnNode> list1, List<AbstractInsnNode> list2, BiPredicate<LabelNode, LabelNode> labelEquals) {
    return ListHelper.equals(
            list1,
            list2,
            (i1, i2) -> AbstractInsnNodeHelper.equals(i1, i2, labelEquals)
    );
  }

  public static int hashCodeNormalizeLabels(List<AbstractInsnNode> list) {
    return hashCode(list, (new AutoIncrementBiHashMap<>())::get);
  }

  public static int hashCode(List<AbstractInsnNode> list, ToIntFunction<LabelNode> labelEquals) {
    return ListHelper.hashCode(
            list,
            i -> AbstractInsnNodeHelper.hashCode(i, labelEquals)
    );
  }

  public static InsnList fromList(List<AbstractInsnNode> insns) {
    InsnList insnList = new InsnList();

    for (AbstractInsnNode insn : insns) {
      insnList.add(insn);
    }

    return insnList;
  }
}
