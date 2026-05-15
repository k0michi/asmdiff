package com.koyomiji.asmweaver;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;

public class OpcodesHelper {
  public static final int ASM_LATEST = Opcodes.ASM9;

  public static int getType(int opcode) {
    switch (opcode) {
      case Opcodes.NOP:
      case Opcodes.ACONST_NULL:
      case Opcodes.ICONST_M1:
      case Opcodes.ICONST_0:
      case Opcodes.ICONST_1:
      case Opcodes.ICONST_2:
      case Opcodes.ICONST_3:
      case Opcodes.ICONST_4:
      case Opcodes.ICONST_5:
      case Opcodes.LCONST_0:
      case Opcodes.LCONST_1:
      case Opcodes.FCONST_0:
      case Opcodes.FCONST_1:
      case Opcodes.FCONST_2:
      case Opcodes.DCONST_0:
      case Opcodes.DCONST_1:
      case Opcodes.IALOAD:
      case Opcodes.LALOAD:
      case Opcodes.FALOAD:
      case Opcodes.DALOAD:
      case Opcodes.AALOAD:
      case Opcodes.BALOAD:
      case Opcodes.CALOAD:
      case Opcodes.SALOAD:
      case Opcodes.IASTORE:
      case Opcodes.LASTORE:
      case Opcodes.FASTORE:
      case Opcodes.DASTORE:
      case Opcodes.AASTORE:
      case Opcodes.BASTORE:
      case Opcodes.CASTORE:
      case Opcodes.SASTORE:
      case Opcodes.POP:
      case Opcodes.POP2:
      case Opcodes.DUP:
      case Opcodes.DUP_X1:
      case Opcodes.DUP_X2:
      case Opcodes.DUP2:
      case Opcodes.DUP2_X1:
      case Opcodes.DUP2_X2:
      case Opcodes.SWAP:
      case Opcodes.IADD:
      case Opcodes.LADD:
      case Opcodes.FADD:
      case Opcodes.DADD:
      case Opcodes.ISUB:
      case Opcodes.LSUB:
      case Opcodes.FSUB:
      case Opcodes.DSUB:
      case Opcodes.IMUL:
      case Opcodes.LMUL:
      case Opcodes.FMUL:
      case Opcodes.DMUL:
      case Opcodes.IDIV:
      case Opcodes.LDIV:
      case Opcodes.FDIV:
      case Opcodes.DDIV:
      case Opcodes.IREM:
      case Opcodes.LREM:
      case Opcodes.FREM:
      case Opcodes.DREM:
      case Opcodes.INEG:
      case Opcodes.LNEG:
      case Opcodes.FNEG:
      case Opcodes.DNEG:
      case Opcodes.ISHL:
      case Opcodes.LSHL:
      case Opcodes.ISHR:
      case Opcodes.LSHR:
      case Opcodes.IUSHR:
      case Opcodes.LUSHR:
      case Opcodes.IAND:
      case Opcodes.LAND:
      case Opcodes.IOR:
      case Opcodes.LOR:
      case Opcodes.IXOR:
      case Opcodes.LXOR:
      case Opcodes.I2L:
      case Opcodes.I2F:
      case Opcodes.I2D:
      case Opcodes.L2I:
      case Opcodes.L2F:
      case Opcodes.L2D:
      case Opcodes.F2I:
      case Opcodes.F2L:
      case Opcodes.F2D:
      case Opcodes.D2I:
      case Opcodes.D2L:
      case Opcodes.D2F:
      case Opcodes.I2B:
      case Opcodes.I2C:
      case Opcodes.I2S:
      case Opcodes.LCMP:
      case Opcodes.FCMPL:
      case Opcodes.FCMPG:
      case Opcodes.DCMPL:
      case Opcodes.DCMPG:
      case Opcodes.IRETURN:
      case Opcodes.LRETURN:
      case Opcodes.FRETURN:
      case Opcodes.DRETURN:
      case Opcodes.ARETURN:
      case Opcodes.RETURN:
      case Opcodes.ARRAYLENGTH:
      case Opcodes.ATHROW:
      case Opcodes.MONITORENTER:
      case Opcodes.MONITOREXIT:
        return AbstractInsnNode.INSN;

      case Opcodes.BIPUSH:
      case Opcodes.SIPUSH:
      case Opcodes.NEWARRAY:
        return AbstractInsnNode.INT_INSN;

      case Opcodes.LDC:
        return AbstractInsnNode.LDC_INSN;

      case Opcodes.ILOAD:
      case Opcodes.LLOAD:
      case Opcodes.FLOAD:
      case Opcodes.DLOAD:
      case Opcodes.ALOAD:
      case Opcodes.ISTORE:
      case Opcodes.LSTORE:
      case Opcodes.FSTORE:
      case Opcodes.DSTORE:
      case Opcodes.ASTORE:
      case Opcodes.RET:
        return AbstractInsnNode.VAR_INSN;

      case Opcodes.IINC:
        return AbstractInsnNode.IINC_INSN;

      case Opcodes.IFEQ:
      case Opcodes.IFNE:
      case Opcodes.IFLT:
      case Opcodes.IFGE:
      case Opcodes.IFGT:
      case Opcodes.IFLE:
      case Opcodes.IF_ICMPEQ:
      case Opcodes.IF_ICMPNE:
      case Opcodes.IF_ICMPLT:
      case Opcodes.IF_ICMPGE:
      case Opcodes.IF_ICMPGT:
      case Opcodes.IF_ICMPLE:
      case Opcodes.IF_ACMPEQ:
      case Opcodes.IF_ACMPNE:
      case Opcodes.GOTO:
      case Opcodes.JSR:
      case Opcodes.IFNULL:
      case Opcodes.IFNONNULL:
        return AbstractInsnNode.JUMP_INSN;

      case Opcodes.TABLESWITCH:
        return AbstractInsnNode.TABLESWITCH_INSN;

      case Opcodes.LOOKUPSWITCH:
        return AbstractInsnNode.LOOKUPSWITCH_INSN;

      case Opcodes.GETSTATIC:
      case Opcodes.PUTSTATIC:
      case Opcodes.GETFIELD:
      case Opcodes.PUTFIELD:
        return AbstractInsnNode.FIELD_INSN;

      case Opcodes.INVOKEVIRTUAL:
      case Opcodes.INVOKESPECIAL:
      case Opcodes.INVOKESTATIC:
      case Opcodes.INVOKEINTERFACE:
        return AbstractInsnNode.METHOD_INSN;

      case Opcodes.INVOKEDYNAMIC:
        return AbstractInsnNode.INVOKE_DYNAMIC_INSN;

      case Opcodes.NEW:
      case Opcodes.ANEWARRAY:
      case Opcodes.CHECKCAST:
      case Opcodes.INSTANCEOF:
        return AbstractInsnNode.TYPE_INSN;

      case Opcodes.MULTIANEWARRAY:
        return AbstractInsnNode.MULTIANEWARRAY_INSN;

      default:
        throw new IllegalArgumentException("Unknown opcode: " + opcode);
    }
  }
}
