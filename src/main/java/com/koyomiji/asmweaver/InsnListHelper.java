package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.AutoIncrementBiHashMap;
import com.koyomiji.asmweaver.util.tuple.Pair;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;

import java.util.ArrayList;
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

  public static Pair<List<AbstractInsnNode>, List<LineNumberNode>> splitLineNumbers(List<AbstractInsnNode> insns) {
    List<AbstractInsnNode> nonLineNumbers = new ArrayList<>();
    List<LineNumberNode> lineNumbers = new ArrayList<>();

    for (AbstractInsnNode insn : insns) {
      if (insn instanceof LineNumberNode) {
        lineNumbers.add((LineNumberNode) insn);
      } else {
        nonLineNumbers.add(insn);
      }
    }

    return new Pair<>(nonLineNumbers, lineNumbers);
  }

  public static List<AbstractInsnNode> mergeLineNumbers(List<AbstractInsnNode> insns, List<LineNumberNode> lineNumbers) {
    List<AbstractInsnNode> merged = new ArrayList<>(insns);

    for (LineNumberNode lineNumber : lineNumbers) {
      int index = merged.indexOf(lineNumber.start);

      if (index != -1) {
        merged.add(index + 1, lineNumber);
      } else {
        throw new RuntimeException("No instruction found for line number: " + lineNumber.line);
      }
    }

    return merged;
  }

  public static List<LineNumberNode> relativizeLineNumbers(List<LineNumberNode> lineNumbers) {
    List<LineNumberNode> relativized = new ArrayList<>();
    int lastLine = 0;

    for (LineNumberNode lineNumber : lineNumbers) {
      int originalLine = lineNumber.line;
      int delta = originalLine - lastLine;
      LineNumberNode relativizedNode = new LineNumberNode(delta, lineNumber.start);
      relativized.add(relativizedNode);
      lastLine = originalLine;
    }

    return relativized;
  }

  public static List<LineNumberNode> absolutizeLineNumbers(List<LineNumberNode> lineNumbers) {
    List<LineNumberNode> absolutized = new ArrayList<>();
    int lastLine = 0;

    for (LineNumberNode lineNumber : lineNumbers) {
      lastLine += lineNumber.line;
      absolutized.add(new LineNumberNode(lastLine, lineNumber.start));
    }

    return absolutized;
  }
}
