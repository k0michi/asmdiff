package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiPredicate;

public class AbstractInsnNodeHelper {
  public static boolean equals(AbstractInsnNode node1, AbstractInsnNode node2) {
    return equals(node1, node2, Objects::equals, Objects::equals);
  }

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

    if (!ListHelper.equals(node1.visibleTypeAnnotations, node2.visibleTypeAnnotations, AnnotationNodeHelper::equals)) {
      return false;
    }

    if (!ListHelper.equals(node1.invisibleTypeAnnotations, node2.invisibleTypeAnnotations, AnnotationNodeHelper::equals)) {
      return false;
    }

    if (node1.getClass() == InsnNode.class) {
      return equals((InsnNode) node1, (InsnNode) node2);
    } else if (node1.getClass() == IntInsnNode.class) {
      return equals((IntInsnNode) node1, (IntInsnNode) node2);
    } else if (node1.getClass() == VarInsnNode.class) {
      return equals((VarInsnNode) node1, (VarInsnNode) node2, localEquals);
    } else if (node1.getClass() == TypeInsnNode.class) {
      return equals((TypeInsnNode) node1, (TypeInsnNode) node2);
    } else if (node1.getClass() == FieldInsnNode.class) {
      return equals((FieldInsnNode) node1, (FieldInsnNode) node2);
    } else if (node1.getClass() == MethodInsnNode.class) {
      return equals((MethodInsnNode) node1, (MethodInsnNode) node2);
    } else if (node1.getClass() == InvokeDynamicInsnNode.class) {
      return equals((InvokeDynamicInsnNode) node1, (InvokeDynamicInsnNode) node2);
    } else if (node1.getClass() == JumpInsnNode.class) {
      return equals((JumpInsnNode) node1, (JumpInsnNode) node2, labelEquals);
    } else if (node1.getClass() == LdcInsnNode.class) {
      return equals((LdcInsnNode) node1, (LdcInsnNode) node2);
    } else if (node1.getClass() == IincInsnNode.class) {
      return equals((IincInsnNode) node1, (IincInsnNode) node2, localEquals);
    } else if (node1.getClass() == TableSwitchInsnNode.class) {
      return equals((TableSwitchInsnNode) node1, (TableSwitchInsnNode) node2, labelEquals);
    } else if (node1.getClass() == LookupSwitchInsnNode.class) {
      return equals((LookupSwitchInsnNode) node1, (LookupSwitchInsnNode) node2, labelEquals);
    } else if (node1.getClass() == MultiANewArrayInsnNode.class) {
      return equals((MultiANewArrayInsnNode) node1, (MultiANewArrayInsnNode) node2);
    } else if (node1.getClass() == FrameNode.class) {
      return equals((FrameNode) node1, (FrameNode) node2, labelEquals);
    } else if (node1.getClass() == LineNumberNode.class) {
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

  public static int hashCode(AbstractInsnNode node) {
    if (node == null) {
      return 0;
    }

    if (node.getClass() == InsnNode.class) {
      return Objects.hash(
              node.getOpcode(),
              node.getType(),
              ListHelper.hashCode(node.visibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              ListHelper.hashCode(node.invisibleTypeAnnotations, AnnotationNodeHelper::hashCode)
      );
    } else if (node.getClass() == IntInsnNode.class) {
      IntInsnNode intNode = (IntInsnNode) node;
      return Objects.hash(
              node.getOpcode(),
              node.getType(),
              ListHelper.hashCode(node.visibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              ListHelper.hashCode(node.invisibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              intNode.operand
      );
    } else if (node.getClass() == VarInsnNode.class) {
      VarInsnNode varNode = (VarInsnNode) node;
      return Objects.hash(
              node.getOpcode(),
              node.getType(),
              ListHelper.hashCode(node.visibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              ListHelper.hashCode(node.invisibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              varNode.var
      );
    } else if (node.getClass() == TypeInsnNode.class) {
      TypeInsnNode typeNode = (TypeInsnNode) node;
      return Objects.hash(
              node.getOpcode(),
              node.getType(),
              ListHelper.hashCode(node.visibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              ListHelper.hashCode(node.invisibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              typeNode.desc
      );
    } else if (node.getClass() == FieldInsnNode.class) {
      FieldInsnNode fieldNode = (FieldInsnNode) node;
      return Objects.hash(
              node.getOpcode(),
              node.getType(),
              ListHelper.hashCode(node.visibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              ListHelper.hashCode(node.invisibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              fieldNode.owner,
              fieldNode.name,
              fieldNode.desc
      );
    } else if (node.getClass() == MethodInsnNode.class) {
      MethodInsnNode methodNode = (MethodInsnNode) node;
      return Objects.hash(
              node.getOpcode(),
              node.getType(),
              ListHelper.hashCode(node.visibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              ListHelper.hashCode(node.invisibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              methodNode.owner,
              methodNode.name,
              methodNode.desc,
              methodNode.itf
      );
    } else if (node.getClass() == InvokeDynamicInsnNode.class) {
      InvokeDynamicInsnNode indyNode = (InvokeDynamicInsnNode) node;
      return Objects.hash(
              node.getOpcode(),
              node.getType(),
              ListHelper.hashCode(node.visibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              ListHelper.hashCode(node.invisibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              indyNode.name,
              indyNode.desc,
              indyNode.bsm,
              Arrays.hashCode(indyNode.bsmArgs)
      );
    } else if (node.getClass() == JumpInsnNode.class) {
      JumpInsnNode jumpNode = (JumpInsnNode) node;
      return Objects.hash(
              node.getOpcode(),
              node.getType(),
              ListHelper.hashCode(node.visibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              ListHelper.hashCode(node.invisibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              jumpNode.label
      );
    } else if (node.getClass() == LdcInsnNode.class) {
      LdcInsnNode ldcNode = (LdcInsnNode) node;
      return Objects.hash(
              node.getOpcode(),
              node.getType(),
              ListHelper.hashCode(node.visibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              ListHelper.hashCode(node.invisibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              ldcNode.cst
      );
    } else if (node.getClass() == IincInsnNode.class) {
      IincInsnNode iincNode = (IincInsnNode) node;
      return Objects.hash(
              node.getOpcode(),
              node.getType(),
              ListHelper.hashCode(node.visibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              ListHelper.hashCode(node.invisibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              iincNode.var,
              iincNode.incr
      );
    } else if (node.getClass() == TableSwitchInsnNode.class) {
      TableSwitchInsnNode tableSwitchNode = (TableSwitchInsnNode) node;
      return Objects.hash(
              node.getOpcode(),
              node.getType(),
              ListHelper.hashCode(node.visibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              ListHelper.hashCode(node.invisibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              tableSwitchNode.min,
              tableSwitchNode.max,
              tableSwitchNode.dflt,
              ListHelper.hashCode(tableSwitchNode.labels, Objects::hashCode)
      );
    } else if (node.getClass() == LookupSwitchInsnNode.class) {
      LookupSwitchInsnNode lookupSwitchNode = (LookupSwitchInsnNode) node;
      return Objects.hash(
              node.getOpcode(),
              node.getType(),
              ListHelper.hashCode(node.visibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              ListHelper.hashCode(node.invisibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              lookupSwitchNode.dflt,
              ListHelper.hashCode(lookupSwitchNode.labels, Objects::hashCode),
              ListHelper.hashCode(lookupSwitchNode.keys, Objects::hashCode)
      );
    } else if (node.getClass() == MultiANewArrayInsnNode.class) {
      MultiANewArrayInsnNode multiANewArrayNode = (MultiANewArrayInsnNode) node;
      return Objects.hash(
              node.getOpcode(),
              node.getType(),
              ListHelper.hashCode(node.visibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              ListHelper.hashCode(node.invisibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              multiANewArrayNode.desc,
              multiANewArrayNode.dims
      );
    } else if (node.getClass() == FrameNode.class) {
      FrameNode frameNode = (FrameNode) node;
      return Objects.hash(
              node.getOpcode(),
              node.getType(),
              ListHelper.hashCode(node.visibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              ListHelper.hashCode(node.invisibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              frameNode.type,
              ListHelper.hashCode(frameNode.local, Objects::hashCode),
              ListHelper.hashCode(frameNode.stack, Objects::hashCode)
      );
    } else if (node.getClass() == LineNumberNode.class) {
      LineNumberNode lineNumberNode = (LineNumberNode) node;
      return Objects.hash(
              node.getOpcode(),
              node.getType(),
              ListHelper.hashCode(node.visibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              ListHelper.hashCode(node.invisibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              lineNumberNode.line,
              lineNumberNode.start
      );
    }

    return Objects.hash(node);
  }
}
