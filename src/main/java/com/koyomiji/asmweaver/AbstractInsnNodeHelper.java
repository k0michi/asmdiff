package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.CustomDataInput;
import com.koyomiji.asmweaver.io.CustomDataOutput;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class AbstractInsnNodeHelper {
  public static boolean equals(AbstractInsnNode node1, AbstractInsnNode node2) {
    return equals(node1, node2, Objects::equals, Objects::equals);
  }

  public static boolean equals(AbstractInsnNode node1, AbstractInsnNode node2, BiPredicate<LabelNode, LabelNode> labelEquals) {
    return equals(node1, node2, labelEquals, Objects::equals);
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

    if (!ListHelper.equalsNullToEmpty(node1.visibleTypeAnnotations, node2.visibleTypeAnnotations, AnnotationNodeHelper::equals)) {
      return false;
    }

    if (!ListHelper.equalsNullToEmpty(node1.invisibleTypeAnnotations, node2.invisibleTypeAnnotations, AnnotationNodeHelper::equals)) {
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
    } else if (node1.getClass() == LabelNode.class) {
      return equals((LabelNode) node1, (LabelNode) node2, labelEquals);
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

  private static boolean equals(LabelNode node1, LabelNode node2, BiPredicate<LabelNode, LabelNode> labelEquals) {
    return labelEquals.test(node1, node2);
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
    return hashCode(node, Objects::hashCode);
  }

  public static int hashCode(AbstractInsnNode node, ToIntFunction<LabelNode> labelHashCode) {
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
              labelHashCode.applyAsInt(jumpNode.label)
      );
    } else if (node.getClass() == LabelNode.class) {
      LabelNode labelNode = (LabelNode) node;
      return Objects.hash(
              node.getOpcode(),
              node.getType(),
              ListHelper.hashCode(node.visibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              ListHelper.hashCode(node.invisibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              labelHashCode.applyAsInt(labelNode)
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
              labelHashCode.applyAsInt(tableSwitchNode.dflt),
              ListHelper.hashCode(tableSwitchNode.labels, labelHashCode)
      );
    } else if (node.getClass() == LookupSwitchInsnNode.class) {
      LookupSwitchInsnNode lookupSwitchNode = (LookupSwitchInsnNode) node;
      return Objects.hash(
              node.getOpcode(),
              node.getType(),
              ListHelper.hashCode(node.visibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              ListHelper.hashCode(node.invisibleTypeAnnotations, AnnotationNodeHelper::hashCode),
              labelHashCode.applyAsInt(lookupSwitchNode.dflt),
              ListHelper.hashCode(lookupSwitchNode.labels, labelHashCode),
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
              labelHashCode.applyAsInt(lineNumberNode.start)
      );
    }

    return Objects.hash(node);
  }

  public static void write(AbstractInsnNode node, CustomDataOutput out, Function<LabelNode, Integer> labelToIndex) throws IOException {
    out.writeInt(node.getOpcode());
    out.writeInt(node.getType());
    ListHelper.write(
            ListHelper.nullToEmpty(node.visibleTypeAnnotations),
            out, (a, out2) -> AnnotationNodeHelper.write(a, out2, labelToIndex)
    );
    ListHelper.write(
            ListHelper.nullToEmpty(node.invisibleTypeAnnotations),
            out, (a, out2) -> AnnotationNodeHelper.write(a, out2, labelToIndex)
    );

    switch (node.getType()) {
      case AbstractInsnNode.INSN:
        break;
      case AbstractInsnNode.INT_INSN:
        IntInsnNode intInsnNode = (IntInsnNode) node;
        out.writeInt(intInsnNode.operand);
        break;
      case AbstractInsnNode.VAR_INSN:
        VarInsnNode varInsnNode = (VarInsnNode) node;
        out.writeInt(varInsnNode.var);
        break;
      case AbstractInsnNode.TYPE_INSN:
        TypeInsnNode typeInsnNode = (TypeInsnNode) node;
        out.writeUTF(typeInsnNode.desc);
        break;
      case AbstractInsnNode.FIELD_INSN:
        FieldInsnNode fieldInsnNode = (FieldInsnNode) node;
        out.writeUTF(fieldInsnNode.owner);
        out.writeUTF(fieldInsnNode.name);
        out.writeUTF(fieldInsnNode.desc);
        break;
      case AbstractInsnNode.METHOD_INSN:
        MethodInsnNode methodInsnNode = (MethodInsnNode) node;
        out.writeUTF(methodInsnNode.owner);
        out.writeUTF(methodInsnNode.name);
        out.writeUTF(methodInsnNode.desc);
        out.writeBoolean(methodInsnNode.itf);
        break;
      case AbstractInsnNode.INVOKE_DYNAMIC_INSN:
        InvokeDynamicInsnNode indyNode = (InvokeDynamicInsnNode) node;
        out.writeUTF(indyNode.name);
        out.writeUTF(indyNode.desc);
        HandleHelper.write(indyNode.bsm, out);
        ListHelper.write(Arrays.asList(indyNode.bsmArgs), out, ConstantHelper::write);
        break;
      case AbstractInsnNode.JUMP_INSN:
        JumpInsnNode jumpInsnNode = (JumpInsnNode) node;
        out.writeInt(labelToIndex.apply(jumpInsnNode.label));
        break;
      case AbstractInsnNode.LABEL:
        LabelNode labelNode = (LabelNode) node;
        out.writeInt(labelToIndex.apply(labelNode));
        break;
      case AbstractInsnNode.LDC_INSN:
        LdcInsnNode ldcInsnNode = (LdcInsnNode) node;
        ConstantHelper.write(ldcInsnNode.cst, out);
        break;
      case AbstractInsnNode.IINC_INSN:
        IincInsnNode iincInsnNode = (IincInsnNode) node;
        out.writeInt(iincInsnNode.var);
        out.writeInt(iincInsnNode.incr);
        break;
      case AbstractInsnNode.TABLESWITCH_INSN:
        TableSwitchInsnNode tableSwitchInsnNode = (TableSwitchInsnNode) node;
        out.writeInt(tableSwitchInsnNode.min);
        out.writeInt(tableSwitchInsnNode.max);
        out.writeInt(labelToIndex.apply(tableSwitchInsnNode.dflt));
        ListHelper.write(tableSwitchInsnNode.labels, out, (l, stream) -> stream.writeInt(labelToIndex.apply(l)));
        break;
      case AbstractInsnNode.LOOKUPSWITCH_INSN:
        LookupSwitchInsnNode lookupSwitchInsnNode = (LookupSwitchInsnNode) node;
        out.writeInt(labelToIndex.apply(lookupSwitchInsnNode.dflt));
        ListHelper.write(lookupSwitchInsnNode.keys, out, (k, stream) -> stream.writeInt(k));
        ListHelper.write(lookupSwitchInsnNode.labels, out, (l, stream) -> stream.writeInt(labelToIndex.apply(l)));
        break;
      case AbstractInsnNode.MULTIANEWARRAY_INSN:
        MultiANewArrayInsnNode multiANewArrayInsnNode = (MultiANewArrayInsnNode) node;
        out.writeUTF(multiANewArrayInsnNode.desc);
        out.writeInt(multiANewArrayInsnNode.dims);
        break;
      case AbstractInsnNode.FRAME:
        FrameNode frameNode = (FrameNode) node;
        out.writeInt(frameNode.type);
        ListHelper.write(
                frameNode.local,
                out,
                (value, stream) -> {
                  writeFrameValue(value, stream, labelToIndex);
                }
        );
        ListHelper.write(
                frameNode.stack,
                out,
                (value, stream) -> {
                  writeFrameValue(value, stream, labelToIndex);
                }
        );
        break;
      case AbstractInsnNode.LINE:
        LineNumberNode lineNumberNode = (LineNumberNode) node;
        out.writeInt(lineNumberNode.line);
        out.writeInt(labelToIndex.apply(lineNumberNode.start));
        break;
      default:
        throw new IllegalArgumentException("Unknown node type: " + node.toString());
    }
  }

  private static void writeFrameValue(Object value, CustomDataOutput out, Function<LabelNode, Integer> labelToIndex) throws IOException {
    if (value instanceof Integer) {
      out.writeInt(0);
      out.writeInt((Integer) value);
    } else if (value instanceof String) {
      out.writeInt(1);
      out.writeUTF((String) value);
    } else if (value instanceof LabelNode) {
      out.writeInt(2);
      out.writeInt(labelToIndex.apply((LabelNode) value));
    } else {
      throw new IllegalArgumentException("Unsupported frame value type: " + value.getClass());
    }
  }

  public static AbstractInsnNode read(CustomDataInput in, Function<Integer, LabelNode> indexToLabel) throws IOException {
    int opcode = in.readInt();
    int type = in.readInt();
    List<TypeAnnotationNode> visibleTypeAnnotations = ListHelper.read(in, AnnotationNodeHelper::readTypeAnnotationNode);
    List<TypeAnnotationNode> invisibleTypeAnnotations = ListHelper.read(in, AnnotationNodeHelper::readTypeAnnotationNode);
    AbstractInsnNode insnNode;

    switch (type) {
      case AbstractInsnNode.INSN:
        insnNode = new InsnNode(opcode);
        break;
      case AbstractInsnNode.INT_INSN:
        insnNode = new IntInsnNode(opcode, in.readInt());
        break;
      case AbstractInsnNode.VAR_INSN:
        insnNode = new VarInsnNode(opcode, in.readInt());
        break;
      case AbstractInsnNode.TYPE_INSN:
        insnNode = new TypeInsnNode(opcode, in.readUTF());
        break;
      case AbstractInsnNode.FIELD_INSN:
        insnNode = new FieldInsnNode(opcode, in.readUTF(), in.readUTF(), in.readUTF());
        break;
      case AbstractInsnNode.METHOD_INSN:
        insnNode = new MethodInsnNode(opcode, in.readUTF(), in.readUTF(), in.readUTF(), in.readBoolean());
        break;
      case AbstractInsnNode.INVOKE_DYNAMIC_INSN:
        insnNode = new InvokeDynamicInsnNode(
                in.readUTF(),
                in.readUTF(),
                HandleHelper.read(in),
                ListHelper.read(in, AnnotationNodeHelper::readTypeAnnotationNode).toArray()
        );
        break;
      case AbstractInsnNode.JUMP_INSN:
        insnNode = new JumpInsnNode(
                opcode,
                indexToLabel.apply(in.readInt())
        );
        break;
      case AbstractInsnNode.LABEL:
        insnNode = indexToLabel.apply(in.readInt());
        break;
      case AbstractInsnNode.LDC_INSN:
        insnNode = new LdcInsnNode(ConstantHelper.read(in));
        break;
      case AbstractInsnNode.IINC_INSN:
        insnNode = new IincInsnNode(in.readInt(), in.readInt());
        break;
      case AbstractInsnNode.TABLESWITCH_INSN:
        insnNode = new TableSwitchInsnNode(
                in.readInt(),
                in.readInt(),
                indexToLabel.apply(in.readInt()),
                ListHelper.read(in, i -> indexToLabel.apply(in.readInt())).toArray(new LabelNode[0])
        );
        break;
      case AbstractInsnNode.LOOKUPSWITCH_INSN:
        insnNode = new LookupSwitchInsnNode(
                indexToLabel.apply(in.readInt()),
                ListHelper.read(in, i -> in.readInt()).stream().mapToInt(Integer::intValue).toArray(),
                ListHelper.read(in, i -> indexToLabel.apply(in.readInt())).toArray(new LabelNode[0])
        );
        break;
      case AbstractInsnNode.MULTIANEWARRAY_INSN:
        insnNode = new MultiANewArrayInsnNode(
                in.readUTF(),
                in.readInt()
        );
        break;
      case AbstractInsnNode.FRAME:
        int frameType = in.readInt();
        List<Object> locals = ListHelper.read(in, i -> readFrameValue(in, indexToLabel));
        List<Object> stack = ListHelper.read(in, i -> readFrameValue(in, indexToLabel));
        insnNode = new FrameNode(
                frameType,
                locals.size(),
                locals.toArray(),
                stack.size(),
                stack.toArray()
        );
        break;
      case AbstractInsnNode.LINE:
        insnNode = new LineNumberNode(
                in.readInt(),
                indexToLabel.apply(in.readInt())
        );
        break;
      default:
        throw new IllegalArgumentException("Unsupported frame type: " + type);
    }

    insnNode.visibleTypeAnnotations = visibleTypeAnnotations;
    insnNode.invisibleTypeAnnotations = invisibleTypeAnnotations;

    return insnNode;
  }

  private static Object readFrameValue(CustomDataInput in, Function<Integer, LabelNode> indexToLabel) throws IOException {
    switch (in.readInt()) {
      case 0:
        return in.readInt();
      case 1:
        return in.readUTF();
      case 2:
        return indexToLabel.apply(in.readInt());
      default:
        throw new IllegalArgumentException("Unknown frame value type: " + in.readInt());
    }
  }

  public static boolean equalsIgnoreLabelsIgnoreLocals(AbstractInsnNode insn1, AbstractInsnNode insn2) {
    return equals(insn1, insn2, (l1, l2) -> true, (v1, v2) -> true);
  }

  public static boolean equalsIgnoreLabelsExactLocals(AbstractInsnNode insn1, AbstractInsnNode insn2) {
    return equals(insn1, insn2, (l1, l2) -> true, Integer::equals);
  }

  public static List<LabelNode> getLabelTargets(AbstractInsnNode insn) {
    List<LabelNode> targets = new ArrayList<>();

    if (insn instanceof JumpInsnNode) {
      targets.add(((JumpInsnNode) insn).label);
    } else if (insn instanceof LabelNode) {
      targets.add((LabelNode) insn);
    } else if (insn instanceof TableSwitchInsnNode) {
      TableSwitchInsnNode tsw = (TableSwitchInsnNode) insn;
      targets.add(tsw.dflt);
      targets.addAll(tsw.labels);
    } else if (insn instanceof LookupSwitchInsnNode) {
      LookupSwitchInsnNode lsw = (LookupSwitchInsnNode) insn;
      targets.add(lsw.dflt);
      targets.addAll(lsw.labels);
    } else if (insn instanceof LineNumberNode) {
      targets.add(((LineNumberNode) insn).start);
    } else if (insn instanceof IHasLabelNodes) {
      targets.addAll(((IHasLabelNodes) insn).getLabels());
    }

    return targets;
  }
}
