package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.*;

import java.util.*;

public class InsnListDiffUtils {
  public static InsnListDiff invert(InsnListDiff diff) {
    List<InsnListDiff.Operation> invertedOperations = new ArrayList<>();

    for (InsnListDiff.Operation op : diff.operations) {
      InsnListDiff.Operation invertedOp = new InsnListDiff.Operation();

      switch (op.type) {
        case MATCH:
          invertedOp.type = InsnListDiff.Operation.Type.MATCH;
          break;
        case INSERT_EXACT:
        case INSERT_BEFORE:
        case INSERT_AFTER:
          invertedOp.type = InsnListDiff.Operation.Type.DELETE;
          break;
        case DELETE:
          invertedOp.type = InsnListDiff.Operation.Type.INSERT_EXACT;
          break;
      }

      invertedOp.operand = op.operand;
      invertedOperations.add(invertedOp);
    }

    return new InsnListDiff(invertedOperations);
  }

  public static List<LabelNode> extractLabels(List<AbstractInsnNode> insnList) {
    List<LabelNode> labels = new ArrayList<>();

    for (AbstractInsnNode insn : insnList) {
      if (insn instanceof LabelNode) {
        labels.add((LabelNode) insn);
      }
    }

    return labels;
  }

  public static boolean compareLabels(LabelNode label1, LabelNode label2, Map<LabelNode, LabelNode> labelMap) {
    return labelMap.get(label1) == label2;
  }

  public static boolean compareLabels(List<LabelNode> labels1, List<LabelNode> labels2, Map<LabelNode, LabelNode> labelMap) {
    if (labels1.size() != labels2.size()) {
      return false;
    }

    for (int i = 0; i < labels1.size(); i++) {
      if (labelMap.get(labels1.get(i)) != labels2.get(i)) {
        return false;
      }
    }

    return true;
  }

  public static boolean compareInsns(AbstractInsnNode insn1, AbstractInsnNode insn2, Map<LabelNode, LabelNode> labelMap) {
    if (insn1 instanceof InsnNode && insn2 instanceof InsnNode) {
      return true;
    } else if (insn1 instanceof IntInsnNode && insn2 instanceof IntInsnNode) {
      return ((IntInsnNode) insn1).operand == ((IntInsnNode) insn2).operand;
    } else if (insn1 instanceof VarInsnNode && insn2 instanceof VarInsnNode) {
      return ((VarInsnNode) insn1).var == ((VarInsnNode) insn2).var;
    } else if (insn1 instanceof TypeInsnNode && insn2 instanceof TypeInsnNode) {
      return Objects.equals(((TypeInsnNode) insn1).desc, ((TypeInsnNode) insn2).desc);
    } else if (insn1 instanceof FieldInsnNode && insn2 instanceof FieldInsnNode) {
      FieldInsnNode field1 = (FieldInsnNode) insn1;
      FieldInsnNode field2 = (FieldInsnNode) insn2;
      return Objects.equals(field1.owner, field2.owner)
              && Objects.equals(field1.name, field2.name)
              && Objects.equals(field1.desc, field2.desc);
    } else if (insn1 instanceof MethodInsnNode && insn2 instanceof MethodInsnNode) {
      MethodInsnNode method1 = (MethodInsnNode) insn1;
      MethodInsnNode method2 = (MethodInsnNode) insn2;
      return Objects.equals(method1.owner, method2.owner)
              && Objects.equals(method1.name, method2.name)
              && Objects.equals(method1.desc, method2.desc)
              && method1.itf == method2.itf;
    } else if (insn1 instanceof InvokeDynamicInsnNode && insn2 instanceof InvokeDynamicInsnNode) {
      InvokeDynamicInsnNode indy1 = (InvokeDynamicInsnNode) insn1;
      InvokeDynamicInsnNode indy2 = (InvokeDynamicInsnNode) insn2;
      return Objects.equals(indy1.name, indy2.name)
              && Objects.equals(indy1.desc, indy2.desc)
              && Objects.equals(indy1.bsm, indy2.bsm)
              && Arrays.equals(indy1.bsmArgs, indy2.bsmArgs);
    } else if (insn1 instanceof JumpInsnNode && insn2 instanceof JumpInsnNode) {
      JumpInsnNode jump1 = (JumpInsnNode) insn1;
      JumpInsnNode jump2 = (JumpInsnNode) insn2;
      return compareLabels(jump1.label, jump2.label, labelMap);
    } else if (insn1 instanceof LabelNode && insn2 instanceof LabelNode) {
      return compareLabels((LabelNode) insn1, (LabelNode) insn2, labelMap);
    } else if (insn1 instanceof LdcInsnNode && insn2 instanceof LdcInsnNode) {
      return Objects.equals(((LdcInsnNode) insn1).cst, ((LdcInsnNode) insn2).cst);
    } else if (insn1 instanceof IincInsnNode && insn2 instanceof IincInsnNode) {
      IincInsnNode iinc1 = (IincInsnNode) insn1;
      IincInsnNode iinc2 = (IincInsnNode) insn2;
      return iinc1.var == iinc2.var && iinc1.incr == iinc2.incr;
    } else if (insn1 instanceof TableSwitchInsnNode && insn2 instanceof TableSwitchInsnNode) {
      TableSwitchInsnNode switch1 = (TableSwitchInsnNode) insn1;
      TableSwitchInsnNode switch2 = (TableSwitchInsnNode) insn2;
      return switch1.min == switch2.min
              && switch1.max == switch2.max
              && compareLabels(switch1.dflt, switch2.dflt, labelMap)
              && compareLabels(switch1.labels, switch2.labels, labelMap);
    } else if (insn1 instanceof LookupSwitchInsnNode && insn2 instanceof LookupSwitchInsnNode) {
      LookupSwitchInsnNode switch1 = (LookupSwitchInsnNode) insn1;
      LookupSwitchInsnNode switch2 = (LookupSwitchInsnNode) insn2;
      return switch1.dflt == labelMap.get(switch2.dflt)
              && Objects.equals(switch1.keys, switch2.keys)
              && compareLabels(switch1.labels, switch2.labels, labelMap);
    } else if (insn1 instanceof MultiANewArrayInsnNode && insn2 instanceof MultiANewArrayInsnNode) {
      MultiANewArrayInsnNode array1 = (MultiANewArrayInsnNode) insn1;
      MultiANewArrayInsnNode array2 = (MultiANewArrayInsnNode) insn2;
      return Objects.equals(array1.desc, array2.desc) && array1.dims == array2.dims;
    } else if (insn1 instanceof FrameNode && insn2 instanceof FrameNode) {
      FrameNode frame1 = (FrameNode) insn1;
      FrameNode frame2 = (FrameNode) insn2;
      return frame1.type == frame2.type
              && Objects.equals(frame1.local, frame2.local)
              && Objects.equals(frame1.stack, frame2.stack);
    } else if (insn1 instanceof LineNumberNode && insn2 instanceof LineNumberNode) {
      LineNumberNode line1 = (LineNumberNode) insn1;
      LineNumberNode line2 = (LineNumberNode) insn2;
      return line1.line == line2.line && compareLabels(line1.start, line2.start, labelMap);
    }

    // Non-structural instructions
    return Objects.equals(insn1, insn2);
  }

  /**
   * Compare two instruction lists taking labels into account.
   * @param list1
   * @param list2
   * @return
   */
  public static boolean compareInsnLists(List<AbstractInsnNode> list1, List<AbstractInsnNode> list2) {
    List<LabelNode> labels1 = extractLabels(list1);
    List<LabelNode> labels2 = extractLabels(list2);

    if (labels1.size() != labels2.size()) {
      return false;
    }

    Map<LabelNode, LabelNode> labelMap = new HashMap<>();

    for (int i = 0; i < labels1.size(); i++) {
      labelMap.put(labels1.get(i), labels2.get(i));
    }

    Iterator<AbstractInsnNode> iter1 = list1.iterator();
    Iterator<AbstractInsnNode> iter2 = list2.iterator();

    while (iter1.hasNext() && iter2.hasNext()) {
      if (!compareInsns(iter1.next(), iter2.next(), labelMap)) {
        return false;
      }
    }

    return !iter1.hasNext() && !iter2.hasNext();
  }

  /**
   * Extracts the base instructions from the diff.
   * @param diff
   * @return
   */
  public static List<AbstractInsnNode> extractBase(InsnListDiff diff) {
    List<AbstractInsnNode> baseInstructions = new ArrayList<>();

    for (InsnListDiff.Operation op : diff.operations) {
      if (op.type == InsnListDiff.Operation.Type.MATCH || op.type == InsnListDiff.Operation.Type.DELETE) {
        baseInstructions.add(op.operand);
      }
    }

    return baseInstructions;
  }

  /**
   * Returns true if diff1 and diff2 have the same base instructions.
   * @param diff1
   * @param diff2
   * @return
   */
  public static boolean compareBases(InsnListDiff diff1, InsnListDiff diff2) {
    List<AbstractInsnNode> base1 = extractBase(diff1);
    List<AbstractInsnNode> base2 = extractBase(diff2);
    return compareInsnLists(base1, base2);
  }

  /**
   * Note that this merge is not symmetric with respect to diff1 and diff2; directional insertions (INSERT_BEFORE/INSERT_AFTER) will be sorted according to the order of diff1 and diff2.
   */
//  public static InsnListDiff merge(InsnListDiff diff1, InsnListDiff diff2) throws ConflictException {
//    if (!compareBases(diff1, diff2)) {
//      throw new ConflictException("Cannot merge diffs with different bases");
//    }
//
//  }
}
