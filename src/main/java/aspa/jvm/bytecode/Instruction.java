package aspa.jvm.bytecode;

import java.io.IOException;

import aspa.core.Context;
import aspa.core.Atom;
import aspa.core.Stream;
import aspa.core.Symbol;
import aspa.jvm.cp.CClass;
import aspa.jvm.cp.CValue;
import aspa.jvm.cp.ConstantPool;
import aspa.jvm.cp.ConstantPoolException;
import aspa.util.Pair;

/**
 * JVM instruction.
 * 
 * @author Eduardo Marques
 * 
 */
public final class Instruction extends Atom {
  /**
   * Instruction code.
   */
  private short  opcode;
  /**
   * Resolved instruction argument. If used, it will vary according to the
   * instruction type.
   */
  private Object arg;

  @Override
  public String toString() {
    return String.format("%s %s", INAME[opcode], arg != null ? arg.toString()
        : "");
  }

  @Override
  public void read(Stream in, Context ctx) throws IOException {
    ConstantPool cp = (ConstantPool) ctx;
    opcode = (short) in.readUnsignedByte();

    try {
      if (ILENGTH[opcode] != 1) {
        boolean wide = false;

        if (opcode == opc_wide) {
          wide = true;
          opcode = (short) in.readUnsignedByte();
        }

        switch (opcode) {
          case opc_bipush:
          case opc_newarray:
            arg = in.readByte();
            break;
          case opc_sipush:
            arg = in.readShort();
            break;
          case opc_iinc:
            if (wide)
              arg = new Pair<Integer, Short>(in.readUnsignedShort(), in.readShort());
            else
              arg = new Pair<Integer, Short>(in.readUnsignedByte(),
                  (short) in.readByte());
            break;
          case opc_multianewarray:
            arg = new Pair<CClass, Byte>(cp.get(in.readUnsignedShort(),
                CClass.class), in.readByte());
            break;
          case opc_ret:
          case opc_iload:
          case opc_lload:
          case opc_fload:
          case opc_dload:
          case opc_aload:
          case opc_istore:
          case opc_lstore:
          case opc_fstore:
          case opc_dstore:
          case opc_astore:
            arg = wide ? in.readUnsignedShort() : in.readUnsignedByte();
            break;
          case opc_jsr:
          case opc_goto:
          case opc_if_acmpne:
          case opc_if_acmpeq:
          case opc_if_icmpge:
          case opc_if_icmple:
          case opc_if_icmpgt:
          case opc_if_icmplt:
          case opc_if_icmpne:
          case opc_if_icmpeq:
          case opc_ifge:
          case opc_ifgt:
          case opc_ifne:
          case opc_ifle:
          case opc_iflt:
          case opc_ifeq:
          case opc_ifnull:
          case opc_ifnonnull:
            arg = in.readUnsignedShort();
            break;
          case opc_jsr_w:
          case opc_goto_w:
            arg = in.readInt();
            break;
          case opc_anewarray:
          case opc_invokestatic:
          case opc_invokevirtual:
          case opc_invokespecial:
          case opc_new:
          case opc_checkcast:
          case opc_instanceof:
          case opc_getstatic:
          case opc_putstatic:
          case opc_getfield:
          case opc_putfield:
          case opc_ldc_w:
          case opc_ldc2_w:
            arg = cp.get(in.readUnsignedShort());
            break;
          case opc_invokeinterface:
            arg = new Pair<CValue,Byte>(cp.get(in.readUnsignedShort()), in.readByte());
            if (in.readByte() != 0)
              throw new JavaBytecodeException("expected trailing 0 for invokeinterface");
            break;
          case opc_invokedynamic:
            arg = cp.get(in.readUnsignedShort());
            if (in.readShort() != 0)
              throw new JavaBytecodeException("expected trailing 0 pair for invokedynamic");
            break;
          case opc_ldc:
            arg = cp.get(in.readUnsignedByte());
            break;
          case opc_lookupswitch: {
            while ((in.getFilePointer() - in.mark()) % 4 != 0) {
              in.readByte();
            }
            int def = in.readInt();
            int n = in.readInt() * 2;
            int[] v = new int[n + 1];

            v[0] = def;
            for (int i = 1; i <= n; ++i) {
              v[i] = in.readInt();
            }
            arg = v;
          }
          break;
          case opc_tableswitch: {
            while ((in.getFilePointer() - in.mark()) % 4 != 0) {
              in.readByte();
            }

            int def = in.readInt(), lo = in.readInt(), hi = in.readInt();

            int[] v = new int[hi - lo + 4];

            v[0] = def;
            v[1] = lo;
            v[2] = hi;

            for (int i = 3; i != v.length; ++i) {
              v[i] = in.readInt();
            }
            arg = v;
          }
          break;
          default:
            throw new JavaBytecodeException("invalid or unhandled opcode:" + opcode);
        }
      }
    }
    catch (ConstantPoolException e) {
      throw new JavaBytecodeException("error while processing " + name(), e);
    }
    catch (JavaBytecodeException e) {
      throw e;
    }
    catch(RuntimeException e) {
      throw new JavaBytecodeException("error while processing " + name(), e);
    }
  }

  public String name() {
    return INAME[opcode];
  }
  
  @SuppressWarnings("unchecked")
  public void write(Stream out, Context ctx) throws IOException {
    out.writeByte(opcode);

    if (ILENGTH[opcode] == 1) {
      return;
    }

    ConstantPool cp = (ConstantPool) ctx;

    switch (opcode) {
      case opc_bipush:
      case opc_newarray:
        out.writeByte((Byte) arg);
        break;
      case opc_sipush:
        out.writeShort((Short) arg);
        break;
      case opc_iinc: {
        Pair<Integer, Short> varAndValue = (Pair<Integer, Short>) arg;
        if (varAndValue.first > 255 || varAndValue.second > 127
            || varAndValue.second < -128) {
          wide(out);
          out.writeShort(varAndValue.first);
          out.writeShort(varAndValue.second);
        } else {
          out.writeByte(varAndValue.first);
          out.writeByte(varAndValue.second);
        }
      }
        break;
      case opc_multianewarray: {
        Pair<CClass, Byte> classAndDim = (Pair<CClass, Byte>) arg;
        out.writeShort(cp.get(classAndDim.first));
        out.writeByte(classAndDim.second);
      }
        break;
      case opc_ret:
      case opc_iload:
      case opc_lload:
      case opc_fload:
      case opc_dload:
      case opc_aload:
      case opc_istore:
      case opc_lstore:
      case opc_fstore:
      case opc_dstore:
      case opc_astore: {
        int var = (Integer) arg;
        if (var > 255) {
          wide(out);
          out.writeShort(var);
        } else {
          out.writeByte(var);
        }
      }
      break;
      case opc_jsr:
      case opc_goto:
      case opc_if_acmpne:
      case opc_if_acmpeq:
      case opc_if_icmpge:
      case opc_if_icmple:
      case opc_if_icmpgt:
      case opc_if_icmplt:
      case opc_if_icmpne:
      case opc_if_icmpeq:
      case opc_ifge:
      case opc_ifgt:
      case opc_ifne:
      case opc_ifle:
      case opc_iflt:
      case opc_ifeq:
      case opc_ifnull:
      case opc_ifnonnull:
        out.writeShort((Integer) arg);
        break;
      case opc_jsr_w:
      case opc_goto_w:
        out.writeInt((Integer) arg);
        break;
      case opc_anewarray:
      case opc_new:
      case opc_checkcast:
      case opc_instanceof:
      case opc_invokestatic:
      case opc_invokevirtual:
      case opc_invokespecial:
      case opc_getstatic:
      case opc_putstatic:
      case opc_getfield:
      case opc_putfield:
      case opc_ldc2_w:
//        System.out.println(toString());
//        System.out.println(cp != null)        
        out.writeShort(cp.get((CValue) arg));
        break;
      case opc_invokeinterface: {
        Pair<CValue,Byte> pair = (Pair<CValue,Byte>) arg;
        out.writeShort(cp.get(pair.first));
        out.writeByte(pair.second);
        out.writeByte(0);
        break;
      }
      case opc_invokedynamic:
        out.writeShort(cp.get((CValue) arg));
        out.writeShort(0);
      case opc_ldc_w:
      case opc_ldc: {
        int idx = cp.get((CValue) arg);
        if (opcode == opc_ldc_w || idx > 255) {
          if (opcode == opc_ldc)
            changeInstr(out, opc_ldc_w);
          out.writeShort(idx);
        } else {
          out.writeByte(idx);
        }
        break;
      }
      case opc_lookupswitch: {
        while ((out.getFilePointer() - out.mark()) % 4 != 0)
          out.writeByte(0);

        int[] v = (int[]) arg;
        out.writeInt(v[0]);
        out.writeInt((v.length - 1) >> 1);

        for (int i = 1; i != v.length; ++i)
          out.writeInt(v[i]);
      }
        break;
      case opc_tableswitch: {
        while ((out.getFilePointer() - out.mark()) % 4 != 0)
          out.writeByte(0);

        int[] v = (int[]) arg;

        for (int i = 0; i != v.length; ++i)
          out.writeInt(v[i]);
      }
        break;
      default:
        throw new JavaBytecodeException("unhandled opcode:" + opcode);
    }
  }

  private static final int   opc_nop              = 0;
  private static final int   opc_aconst_null      = 1;
  private static final int   opc_iconst_m1        = 2;
  private static final int   opc_iconst_0         = 3;
  private static final int   opc_iconst_1         = 4;
  private static final int   opc_iconst_2         = 5;
  private static final int   opc_iconst_3         = 6;
  private static final int   opc_iconst_4         = 7;
  private static final int   opc_iconst_5         = 8;
  private static final int   opc_lconst_0         = 9;
  private static final int   opc_lconst_1         = 10;
  private static final int   opc_fconst_0         = 11;
  private static final int   opc_fconst_1         = 12;
  private static final int   opc_fconst_2         = 13;
  private static final int   opc_dconst_0         = 14;
  private static final int   opc_dconst_1         = 15;
  private static final int   opc_bipush           = 16;
  private static final int   opc_sipush           = 17;
  private static final int   opc_ldc              = 18;
  private static final int   opc_ldc_w            = 19;
  private static final int   opc_ldc2_w           = 20;
  private static final int   opc_iload            = 21;
  private static final int   opc_lload            = 22;
  private static final int   opc_fload            = 23;
  private static final int   opc_dload            = 24;
  private static final int   opc_aload            = 25;
  private static final int   opc_iload_0          = 26;
  private static final int   opc_iload_1          = 27;
  private static final int   opc_iload_2          = 28;
  private static final int   opc_iload_3          = 29;
  private static final int   opc_lload_0          = 30;
  private static final int   opc_lload_1          = 31;
  private static final int   opc_lload_2          = 32;
  private static final int   opc_lload_3          = 33;
  private static final int   opc_fload_0          = 34;
  private static final int   opc_fload_1          = 35;
  private static final int   opc_fload_2          = 36;
  private static final int   opc_fload_3          = 37;
  private static final int   opc_dload_0          = 38;
  private static final int   opc_dload_1          = 39;
  private static final int   opc_dload_2          = 40;
  private static final int   opc_dload_3          = 41;
  private static final int   opc_aload_0          = 42;
  private static final int   opc_aload_1          = 43;
  private static final int   opc_aload_2          = 44;
  private static final int   opc_aload_3          = 45;
  private static final int   opc_iaload           = 46;
  private static final int   opc_laload           = 47;
  private static final int   opc_faload           = 48;
  private static final int   opc_daload           = 49;
  private static final int   opc_aaload           = 50;
  private static final int   opc_baload           = 51;
  private static final int   opc_caload           = 52;
  private static final int   opc_saload           = 53;
  private static final int   opc_istore           = 54;
  private static final int   opc_lstore           = 55;
  private static final int   opc_fstore           = 56;
  private static final int   opc_dstore           = 57;
  private static final int   opc_astore           = 58;
  private static final int   opc_istore_0         = 59;
  private static final int   opc_istore_1         = 60;
  private static final int   opc_istore_2         = 61;
  private static final int   opc_istore_3         = 62;
  private static final int   opc_lstore_0         = 63;
  private static final int   opc_lstore_1         = 64;
  private static final int   opc_lstore_2         = 65;
  private static final int   opc_lstore_3         = 66;
  private static final int   opc_fstore_0         = 67;
  private static final int   opc_fstore_1         = 68;
  private static final int   opc_fstore_2         = 69;
  private static final int   opc_fstore_3         = 70;
  private static final int   opc_dstore_0         = 71;
  private static final int   opc_dstore_1         = 72;
  private static final int   opc_dstore_2         = 73;
  private static final int   opc_dstore_3         = 74;
  private static final int   opc_astore_0         = 75;
  private static final int   opc_astore_1         = 76;
  private static final int   opc_astore_2         = 77;
  private static final int   opc_astore_3         = 78;
  private static final int   opc_iastore          = 79;
  private static final int   opc_lastore          = 80;
  private static final int   opc_fastore          = 81;
  private static final int   opc_dastore          = 82;
  private static final int   opc_aastore          = 83;
  private static final int   opc_bastore          = 84;
  private static final int   opc_castore          = 85;
  private static final int   opc_sastore          = 86;
  private static final int   opc_pop              = 87;
  private static final int   opc_pop2             = 88;
  private static final int   opc_dup              = 89;
  private static final int   opc_dup_x1           = 90;
  private static final int   opc_dup_x2           = 91;
  private static final int   opc_dup2             = 92;
  private static final int   opc_dup2_x1          = 93;
  private static final int   opc_dup2_x2          = 94;
  private static final int   opc_swap             = 95;
  private static final int   opc_iadd             = 96;
  private static final int   opc_ladd             = 97;
  private static final int   opc_fadd             = 98;
  private static final int   opc_dadd             = 99;
  private static final int   opc_isub             = 100;
  private static final int   opc_lsub             = 101;
  private static final int   opc_fsub             = 102;
  private static final int   opc_dsub             = 103;
  private static final int   opc_imul             = 104;
  private static final int   opc_lmul             = 105;
  private static final int   opc_fmul             = 106;
  private static final int   opc_dmul             = 107;
  private static final int   opc_idiv             = 108;
  private static final int   opc_ldiv             = 109;
  private static final int   opc_fdiv             = 110;
  private static final int   opc_ddiv             = 111;
  private static final int   opc_irem             = 112;
  private static final int   opc_lrem             = 113;
  private static final int   opc_frem             = 114;
  private static final int   opc_drem             = 115;
  private static final int   opc_ineg             = 116;
  private static final int   opc_lneg             = 117;
  private static final int   opc_fneg             = 118;
  private static final int   opc_dneg             = 119;
  private static final int   opc_ishl             = 120;
  private static final int   opc_lshl             = 121;
  private static final int   opc_ishr             = 122;
  private static final int   opc_lshr             = 123;
  private static final int   opc_iushr            = 124;
  private static final int   opc_lushr            = 125;
  private static final int   opc_iand             = 126;
  private static final int   opc_land             = 127;
  private static final int   opc_ior              = 128;
  private static final int   opc_lor              = 129;
  private static final int   opc_ixor             = 130;
  private static final int   opc_lxor             = 131;
  private static final int   opc_iinc             = 132;
  private static final int   opc_i2l              = 133;
  private static final int   opc_i2f              = 134;
  private static final int   opc_i2d              = 135;
  private static final int   opc_l2i              = 136;
  private static final int   opc_l2f              = 137;
  private static final int   opc_l2d              = 138;
  private static final int   opc_f2i              = 139;
  private static final int   opc_f2l              = 140;
  private static final int   opc_f2d              = 141;
  private static final int   opc_d2i              = 142;
  private static final int   opc_d2l              = 143;
  private static final int   opc_d2f              = 144;
  private static final int   opc_i2b              = 145;
  private static final int   opc_i2c              = 146;
  private static final int   opc_i2s              = 147;
  private static final int   opc_lcmp             = 148;
  private static final int   opc_fcmpl            = 149;
  private static final int   opc_fcmpg            = 150;
  private static final int   opc_dcmpl            = 151;
  private static final int   opc_dcmpg            = 152;
  private static final int   opc_ifeq             = 153;
  private static final int   opc_ifne             = 154;
  private static final int   opc_iflt             = 155;
  private static final int   opc_ifge             = 156;
  private static final int   opc_ifgt             = 157;
  private static final int   opc_ifle             = 158;
  private static final int   opc_if_icmpeq        = 159;
  private static final int   opc_if_icmpne        = 160;
  private static final int   opc_if_icmplt        = 161;
  private static final int   opc_if_icmpge        = 162;
  private static final int   opc_if_icmpgt        = 163;
  private static final int   opc_if_icmple        = 164;
  private static final int   opc_if_acmpeq        = 165;
  private static final int   opc_if_acmpne        = 166;
  private static final int   opc_goto             = 167;
  private static final int   opc_jsr              = 168;
  private static final int   opc_ret              = 169;
  private static final int   opc_tableswitch      = 170;
  private static final int   opc_lookupswitch     = 171;
  private static final int   opc_ireturn          = 172;
  private static final int   opc_lreturn          = 173;
  private static final int   opc_freturn          = 174;
  private static final int   opc_dreturn          = 175;
  private static final int   opc_areturn          = 176;
  private static final int   opc_return           = 177;
  private static final int   opc_getstatic        = 178;
  private static final int   opc_putstatic        = 179;
  private static final int   opc_getfield         = 180;
  private static final int   opc_putfield         = 181;
  private static final int   opc_invokevirtual    = 182;
  private static final int   opc_invokespecial    = 183;
  private static final int   opc_invokestatic     = 184;
  private static final int   opc_invokeinterface  = 185;
  private static final int   opc_invokedynamic    = 186;
  private static final int   opc_new              = 187;
  private static final int   opc_newarray         = 188;
  private static final int   opc_anewarray        = 189;
  private static final int   opc_arraylength      = 190;
  private static final int   opc_athrow           = 191;
  private static final int   opc_checkcast        = 192;
  private static final int   opc_instanceof       = 193;
  private static final int   opc_monitorenter     = 194;
  private static final int   opc_monitorexit      = 195;
  private static final int   opc_wide             = 196;
  private static final int   opc_multianewarray   = 197;
  private static final int   opc_ifnull           = 198;
  private static final int   opc_ifnonnull        = 199;
  private static final int   opc_goto_w           = 200;
  private static final int   opc_jsr_w            = 201;
  private static final int   opc_breakpoint       = 202;

  // for backwards compatibility with 1.0
  private static final int   opc_int2byte         = opc_i2b;
  private static final int   opc_int2char         = opc_i2c;
  private static final int   opc_int2short        = opc_i2s;
  private static final int   opc_invokenonvirtual = opc_invokespecial;

  /**
   * JVM instruction names.
   */
  public static final String INAME[]              = { "nop", "aconst_null",
      "iconst_m1", "iconst_0", "iconst_1", "iconst_2", "iconst_3", "iconst_4",
      "iconst_5", "lconst_0", "lconst_1", "fconst_0", "fconst_1", "fconst_2",
      "dconst_0", "dconst_1", "bipush", "sipush", "ldc", "ldc_w", "ldc2_w",
      "iload", "lload", "fload", "dload", "aload", "iload_0", "iload_1",
      "iload_2", "iload_3", "lload_0", "lload_1", "lload_2", "lload_3",
      "fload_0", "fload_1", "fload_2", "fload_3", "dload_0", "dload_1",
      "dload_2", "dload_3", "aload_0", "aload_1", "aload_2", "aload_3",
      "iaload", "laload", "faload", "daload", "aaload", "baload", "caload",
      "saload", "istore", "lstore", "fstore", "dstore", "astore", "istore_0",
      "istore_1", "istore_2", "istore_3", "lstore_0", "lstore_1", "lstore_2",
      "lstore_3", "fstore_0", "fstore_1", "fstore_2", "fstore_3", "dstore_0",
      "dstore_1", "dstore_2", "dstore_3", "astore_0", "astore_1", "astore_2",
      "astore_3", "iastore", "lastore", "fastore", "dastore", "aastore",
      "bastore", "castore", "sastore", "pop", "pop2", "dup", "dup_x1",
      "dup_x2", "dup2", "dup2_x1", "dup2_x2", "swap", "iadd", "ladd", "fadd",
      "dadd", "isub", "lsub", "fsub", "dsub", "imul", "lmul", "fmul", "dmul",
      "idiv", "ldiv", "fdiv", "ddiv", "irem", "lrem", "frem", "drem", "ineg",
      "lneg", "fneg", "dneg", "ishl", "lshl", "ishr", "lshr", "iushr", "lushr",
      "iand", "land", "ior", "lor", "ixor", "lxor", "iinc", "i2l", "i2f",
      "i2d", "l2i", "l2f", "l2d", "f2i", "f2l", "f2d", "d2i", "d2l", "d2f",
      "i2b", "i2c", "i2s", "lcmp", "fcmpl", "fcmpg", "dcmpl", "dcmpg", "ifeq",
      "ifne", "iflt", "ifge", "ifgt", "ifle", "if_icmpeq", "if_icmpne",
      "if_icmplt", "if_icmpge", "if_icmpgt", "if_icmple", "if_acmpeq",
      "if_acmpne", "goto", "jsr", "ret", "tableswitch", "lookupswitch",
      "ireturn", "lreturn", "freturn", "dreturn", "areturn", "return",
      "getstatic", "putstatic", "getfield", "putfield", "invokevirtual",
      "invokespecial", "invokestatic", "invokeinterface", "invokedynamic",
      "new", "newarray", "anewarray", "arraylength", "athrow", "checkcast",
      "instanceof", "monitorenter", "monitorexit", "wide", "multianewarray",
      "ifnull", "ifnonnull", "goto_w", "jsr_w", "breakpoint" };

  /**
   * JVM instruction length.
   */
  private static final int[] ILENGTH              = { 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 2, 3, 3, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2,
      2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 99, 99, 1, 1, 1, 1,
      1, 1, 3, 3, 3, 3, 3, 3, 3, 5, 0, 3, 2, 3, 1, 1, 3, 3, 1, 1, 0, 4, 3, 3,
      5, 5, 1                                    };

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public int compareTo(Symbol s) {
    Instruction other = (Instruction) s;
    int c = opcode - other.opcode;

    if (c == 0 && arg != null) {
      if (opcode == opc_lookupswitch || opcode == opc_tableswitch) {
        int[] a = (int[]) arg;
        int[] b = (int[]) other.arg;

        c = a.length - b.length;

        for (int i = 0; c == 0 && i != a.length; ++i)
          c = a[i] - b[i];
      } else {
        Class<?> clazzA = arg.getClass();
        Class<?> clazzB = other.arg.getClass();

        if (clazzA != clazzB) {
          c = clazzA.getSimpleName().compareTo(clazzB.getSimpleName());
        } else {
          c = ((Comparable) arg).compareTo(other.arg);
        }
      }
    }

    return c;
  }

  private void wide(Stream out) throws IOException {
    changeInstr(out, opc_wide);
    out.writeByte(opcode);
  }

  private void changeInstr(Stream out, int altOpcode) throws IOException {
    out.seek(out.getFilePointer() - 1);
    out.writeByte(altOpcode);
  }
}
