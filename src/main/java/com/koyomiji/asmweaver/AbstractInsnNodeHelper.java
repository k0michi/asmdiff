package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiPredicate;

public class AbstractInsnNodeHelper {
  public static boolean equals(AbstractInsnNode node1, AbstractInsnNode node2, BiPredicate<LabelNode, LabelNode> labelEquals, BiPredicate<Integer, Integer> localEquals) {
    if (node1 == node2) {
      return true;
    }

    if (node1 == null || node2 == null) {
      return false;
    }

    if (node1.getClass() != node2.getClass()) {
      return false;
    }

    if (node1.getOpcode() != node2.getOpcode()) {
      return false;
    }

    if (node1.getType() != node2.getType()) {
      return false;
    }

    if (!ListHelper.equals(node1.visibleTypeAnnotations, node2.visibleTypeAnnotations, TypeAnnotationNodeHelper::equals)) {
      return false;
    }

    if (!ListHelper.equals(node1.invisibleTypeAnnotations, node2.invisibleTypeAnnotations, TypeAnnotationNodeHelper::equals)) {
      return false;
    }

    if (node1 instanceof InsnNode && node2 instanceof InsnNode) {
      return equals((InsnNode) node1, (InsnNode) node2);
    } else if (node1 instanceof IntInsnNode && node2 instanceof IntInsnNode) {
      return equals((IntInsnNode) node1, (IntInsnNode) node2);
    } else if (node1 instanceof VarInsnNode && node2 instanceof VarInsnNode) {
      return equals((VarInsnNode) node1, (VarInsnNode) node2, localEquals);
    } else if (node1 instanceof TypeInsnNode && node2 instanceof TypeInsnNode) {
      return equals((TypeInsnNode) node1, (TypeInsnNode) node2);
    } else if (node1 instanceof FieldInsnNode && node2 instanceof FieldInsnNode) {
      return equals((FieldInsnNode) node1, (FieldInsnNode) node2);
    } else if (node1 instanceof MethodInsnNode && node2 instanceof MethodInsnNode) {
      return equals((MethodInsnNode) node1, (MethodInsnNode) node2);
    } else if (node1 instanceof InvokeDynamicInsnNode && node2 instanceof InvokeDynamicInsnNode) {
      return equals((InvokeDynamicInsnNode) node1, (InvokeDynamicInsnNode) node2);
    } else if (node1 instanceof JumpInsnNode && node2 instanceof JumpInsnNode) {
      return equals((JumpInsnNode) node1, (JumpInsnNode) node2, labelEquals);
    } else if (node1 instanceof LdcInsnNode && node2 instanceof LdcInsnNode) {
      return equals((LdcInsnNode) node1, (LdcInsnNode) node2);
    } else if (node1 instanceof IincInsnNode && node2 instanceof IincInsnNode) {
      return equals((IincInsnNode) node1, (IincInsnNode) node2, localEquals);
    } else if (node1 instanceof TableSwitchInsnNode && node2 instanceof TableSwitchInsnNode) {
      return equals((TableSwitchInsnNode) node1, (TableSwitchInsnNode) node2, labelEquals);
    } else if (node1 instanceof LookupSwitchInsnNode && node2 instanceof LookupSwitchInsnNode) {
      return equals((LookupSwitchInsnNode) node1, (LookupSwitchInsnNode) node2, labelEquals);
    } else if (node1 instanceof MultiANewArrayInsnNode && node2 instanceof MultiANewArrayInsnNode) {
      return equals((MultiANewArrayInsnNode) node1, (MultiANewArrayInsnNode) node2);
    } else if (node1 instanceof FrameNode && node2 instanceof FrameNode) {
      return equals((FrameNode) node1, (FrameNode) node2, labelEquals);
    } else if (node1 instanceof LineNumberNode && node2 instanceof LineNumberNode) {
      return equals((LineNumberNode) node1, (LineNumberNode) node2, labelEquals);
    }

    return Objects.equals(node1, node2);
  }

  private static boolean equals(InsnNode node1, InsnNode node2) {
    return true;
  }

  private static boolean equals(IntInsnNode node1, IntInsnNode node2) {
    return node1.operand == node2.operand;
  }

  private static boolean equals(VarInsnNode node1, VarInsnNode node2, BiPredicate<Integer, Integer> localEquals) {
    return localEquals.test(node1.var, node2.var);
  }

  private static boolean equals(TypeInsnNode node1, TypeInsnNode node2) {
    return Objects.equals(node1.desc, node2.desc);
  }

  private static boolean equals(FieldInsnNode node1, FieldInsnNode node2) {
    return Objects.equals(node1.owner, node2.owner)
            && Objects.equals(node1.name, node2.name)
            && Objects.equals(node1.desc, node2.desc);
  }

  private static boolean equals(MethodInsnNode node1, MethodInsnNode node2) {
    return Objects.equals(node1.owner, node2.owner)
            && Objects.equals(node1.name, node2.name)
            && Objects.equals(node1.desc, node2.desc)
            && node1.itf == node2.itf;
  }

  private static boolean equals(InvokeDynamicInsnNode node1, InvokeDynamicInsnNode node2) {
    return Objects.equals(node1.name, node2.name)
            && Objects.equals(node1.desc, node2.desc)
            && Objects.equals(node1.bsm, node2.bsm)
            && Arrays.equals(node1.bsmArgs, node2.bsmArgs);
  }

  private static boolean equals(JumpInsnNode node1, JumpInsnNode node2, BiPredicate<LabelNode, LabelNode> labelEquals) {
    return labelEquals.test(node1.label, node2.label);
  }

  private static boolean equals(LdcInsnNode node1, LdcInsnNode node2) {
    return Objects.equals(node1.cst, node2.cst);
  }

  private static boolean equals(IincInsnNode node1, IincInsnNode node2, BiPredicate<Integer, Integer> localEquals) {
    return localEquals.test(node1.var, node2.var)
            && node1.incr == node2.incr;
  }

  private static boolean equals(TableSwitchInsnNode node1, TableSwitchInsnNode node2, BiPredicate<LabelNode, LabelNode> labelEquals) {
    return node1.min == node2.min
            && node1.max == node2.max
            && labelEquals.test(node1.dflt, node2.dflt)
            && ListHelper.equals(node1.labels, node2.labels, labelEquals);
  }

  private static boolean equals(LookupSwitchInsnNode node1, LookupSwitchInsnNode node2, BiPredicate<LabelNode, LabelNode> labelEquals) {
    return labelEquals.test(node1.dflt, node2.dflt)
            && ListHelper.equals(node1.labels, node2.labels, labelEquals)
            && ListHelper.equals(node1.keys, node2.keys);
  }

  private static boolean equals(MultiANewArrayInsnNode node1, MultiANewArrayInsnNode node2) {
    return Objects.equals(node1.desc, node2.desc)
            && node1.dims == node2.dims;
  }

  private static boolean compareObjectOrLabel(Object a, Object b, BiPredicate<LabelNode, LabelNode> labelEquals) {
    if (a instanceof LabelNode && b instanceof LabelNode) {
      return labelEquals.test((LabelNode) a, (LabelNode) b);
    }

    return Objects.equals(a, b);
  }

  private static boolean equals(FrameNode node1, FrameNode node2, BiPredicate<LabelNode, LabelNode> labelEquals) {
    return node1.type == node2.type
            && ListHelper.equals(node1.local, node2.local, (a, b) -> compareObjectOrLabel(a, b, labelEquals))
            && ListHelper.equals(node1.stack, node2.stack, (a, b) -> compareObjectOrLabel(a, b, labelEquals));
  }

  private static boolean equals(LineNumberNode node1, LineNumberNode node2, BiPredicate<LabelNode, LabelNode> labelEquals) {
    return node1.line == node2.line
            && labelEquals.test(node1.start, node2.start);
  }
}
