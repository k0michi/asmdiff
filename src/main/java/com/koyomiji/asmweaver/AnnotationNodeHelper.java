package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.CustomDataInput;
import com.koyomiji.asmweaver.util.tuple.Triplet;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableAnnotationNode;
import org.objectweb.asm.tree.TypeAnnotationNode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class AnnotationNodeHelper {
  public static boolean equals(AnnotationNode a, AnnotationNode b) {
    return equals(a, b, Objects::equals, Objects::equals);
  }

  public static boolean equals(AnnotationNode a, AnnotationNode b, BiPredicate<LabelNode, LabelNode> labelEquals, BiPredicate<Triplet<LabelNode, LabelNode, Integer>, Triplet<LabelNode, LabelNode, Integer>> localEquals) {
    if (a == b) {
      return true;
    }

    if (a == null || b == null) {
      return false;
    }

    if (a.getClass() != b.getClass()) {
      return false;
    }

    if (!Objects.equals(a.desc, b.desc)) {
      return false;
    }

    if (!ListHelper.equals(a.values, b.values, AnnotationNodeHelper::equalsValue)) {
      return false;
    }

    if (a.getClass() == AnnotationNode.class) {
      return true;
    } else if (a.getClass() == TypeAnnotationNode.class) {
      return equals((TypeAnnotationNode) a, (TypeAnnotationNode) b);
    } else if (a.getClass() == LocalVariableAnnotationNode.class) {
      return equals((LocalVariableAnnotationNode) a, (LocalVariableAnnotationNode) b, labelEquals, localEquals);
    }

    return Objects.equals(a, b);
  }

  private static boolean equalsValue(Object a, Object b) {
    if (a instanceof String[] && b instanceof String[]) {
      return Arrays.equals((String[]) a, (String[]) b);
    }

    if (a instanceof AnnotationNode && b instanceof AnnotationNode) {
      return AnnotationNodeHelper.equals((AnnotationNode) a, (AnnotationNode) b);
    }

    if (a instanceof List<?> && b instanceof List<?>) {
      return ListHelper.equals((List<Object>) a, (List<Object>) b, AnnotationNodeHelper::equalsValue);
    }

    return Objects.equals(a, b);
  }

  private static boolean equals(TypeAnnotationNode a, TypeAnnotationNode b) {
    return Objects.equals(a.typeRef, b.typeRef)
            && TypePathHelper.equals(a.typePath, b.typePath);
  }

  private static boolean equals(LocalVariableAnnotationNode a, LocalVariableAnnotationNode b, BiPredicate<LabelNode, LabelNode> labelEquals, BiPredicate<Triplet<LabelNode, LabelNode, Integer>, Triplet<LabelNode, LabelNode, Integer>> localEquals) {
    if (a.start.size() != b.start.size()) {
      return false;
    }

    if (a.end.size() != b.end.size()) {
      return false;
    }

    if (a.index.size() != b.index.size()) {
      return false;
    }

    int size = Math.max(a.start.size(), Math.max(a.end.size(), a.index.size()));

    for (int i = 0; i < size; i++) {
      if (
              !labelEquals.test(ListHelper.getOrNull(a.start, i), ListHelper.getOrNull(b.start, i))
                      || !labelEquals.test(ListHelper.getOrNull(a.end, i), ListHelper.getOrNull(b.end, i))
                      || !localEquals.test(
                      Triplet.of(ListHelper.getOrNull(a.start, i), ListHelper.getOrNull(a.end, i), ListHelper.getOrNull(a.index, i))
                      , Triplet.of(ListHelper.getOrNull(b.start, i), ListHelper.getOrNull(b.end, i), ListHelper.getOrNull(b.index, i))
              )
      ) {
        return false;
      }
    }

    return true;
  }

  public static int hashCode(AnnotationNode node) {
    if (node == null) {
      return 0;
    }

    if (node.getClass() == AnnotationNode.class) {
      return Objects.hash(node.desc, ListHelper.hashCode(node.values, AnnotationNodeHelper::annotationValueHashCode));
    } else if (node.getClass() == TypeAnnotationNode.class) {
      TypeAnnotationNode typeNode = (TypeAnnotationNode) node;
      return Objects.hash(node.desc, ListHelper.hashCode(node.values, AnnotationNodeHelper::annotationValueHashCode), typeNode.typeRef, TypePathHelper.hashCode(typeNode.typePath));
    } else if (node.getClass() == LocalVariableAnnotationNode.class) {
      LocalVariableAnnotationNode localVarNode = (LocalVariableAnnotationNode) node;
      return Objects.hash(node.desc, ListHelper.hashCode(node.values, AnnotationNodeHelper::annotationValueHashCode), ListHelper.hashCode(localVarNode.start, Objects::hashCode), ListHelper.hashCode(localVarNode.end, Objects::hashCode), ListHelper.hashCode(localVarNode.index, Objects::hashCode));
    }

    return Objects.hash(node);
  }

  private static int annotationValueHashCode(Object value) {
    // String[]
    if (value instanceof String[]) {
      return Arrays.hashCode((String[]) value);
    }

    // AnnotationNode
    if (value instanceof AnnotationNode) {
      return hashCode((AnnotationNode) value);
    }

    // List
    if (value instanceof List<?>) {
      return ListHelper.hashCode((List<Object>) value, AnnotationNodeHelper::annotationValueHashCode);
    }

    // Byte, Boolean, Character, Short, Integer, Long, Float, Double, String, Type
    return Objects.hashCode(value);
  }

  public static enum ValueType {
    BYTE,
    CHAR,
    DOUBLE,
    FLOAT,
    INT,
    LONG,
    SHORT,
    BOOLEAN,
    STRING,
    ENUM,
    CLASS,
    ANNOTATION,
    ARRAY
  }

  public static void write(AnnotationNode node, DataOutputStream out) throws IOException {
    write(node, out, label -> {
      throw new UnsupportedOperationException("Label index provider is required for writing annotations with labels");
    });
  }

  public static void write(AnnotationNode node, DataOutputStream out, Function<LabelNode, Integer> labelIndexProvider) throws IOException {
    out.writeUTF(node.desc);
    List<Object> values = node.values;

    if (values == null) {
      out.writeInt(0);
    } else {
      out.writeInt(values.size());
      for (Object value : values) {
        writeValue(value, out, labelIndexProvider);
      }
    }

    if (node instanceof TypeAnnotationNode) {
      TypeAnnotationNode typeNode = (TypeAnnotationNode) node;
      out.writeInt(typeNode.typeRef);
      TypePathHelper.write(typeNode.typePath, out);

      if (node instanceof LocalVariableAnnotationNode) {
        LocalVariableAnnotationNode localVarNode = (LocalVariableAnnotationNode) node;
        List<LabelNode> start = localVarNode.start;
        ListHelper.write(start, out, (label, stream) -> {
          stream.writeInt(labelIndexProvider.apply(label));
        });
        List<LabelNode> end = localVarNode.end;
        ListHelper.write(end, out, (label, stream) -> {
          stream.writeInt(labelIndexProvider.apply(label));
        });
        List<Integer> index = localVarNode.index;
        ListHelper.write(index, out, (idx, stream) -> {
          stream.writeInt(idx);
        });
      }
    }
  }

  public static void writeValue(Object value, DataOutputStream out) throws IOException {
    writeValue(value, out, FunctionHelper.throwIfInvokedFunction());
  }

  public static void writeValue(Object value, DataOutputStream out, Function<LabelNode, Integer> labelIndexProvider) throws IOException {
    if (value instanceof String) {
      out.writeByte(ValueType.STRING.ordinal());
      out.writeUTF((String) value);
    } else if (value instanceof Byte) {
      out.writeByte(ValueType.BYTE.ordinal());
      out.writeByte((Byte) value);
    } else if (value instanceof Character) {
      out.writeByte(ValueType.CHAR.ordinal());
      out.writeChar((Character) value);
    } else if (value instanceof Double) {
      out.writeByte(ValueType.DOUBLE.ordinal());
      out.writeDouble((Double) value);
    } else if (value instanceof Float) {
      out.writeByte(ValueType.FLOAT.ordinal());
      out.writeFloat((Float) value);
    } else if (value instanceof Integer) {
      out.writeByte(ValueType.INT.ordinal());
      out.writeInt((Integer) value);
    } else if (value instanceof Long) {
      out.writeByte(ValueType.LONG.ordinal());
      out.writeLong((Long) value);
    } else if (value instanceof Short) {
      out.writeByte(ValueType.SHORT.ordinal());
      out.writeShort((Short) value);
    } else if (value instanceof Boolean) {
      out.writeByte(ValueType.BOOLEAN.ordinal());
      out.writeBoolean((Boolean) value);
    } else if (value instanceof String[]) {
      out.writeByte(ValueType.ENUM.ordinal());
      String[] enumValue = (String[]) value;
      out.writeUTF(enumValue[0]); // Enum type descriptor
      out.writeUTF(enumValue[1]); // Enum constant name
    } else if (value instanceof org.objectweb.asm.Type) {
      out.writeByte(ValueType.CLASS.ordinal());
      out.writeUTF(((org.objectweb.asm.Type) value).getDescriptor());
    } else if (value instanceof AnnotationNode) {
      out.writeByte(ValueType.ANNOTATION.ordinal());
//      write((AnnotationNode) value, out);
      write((AnnotationNode) value, out, labelIndexProvider);
    } else if (value instanceof List<?>) {
      out.writeByte(ValueType.ARRAY.ordinal());
      List<Object> list = (List<Object>) value;
      out.writeInt(list.size());
      for (Object item : list) {
        writeValue(item, out, labelIndexProvider);
      }
    } else {
      throw new IllegalArgumentException("Unsupported annotation value type: " + value.getClass());
    }
  }

  public static AnnotationNode readAnnotationNode(CustomDataInput in) throws IOException {
    String desc = in.readUTF();
    AnnotationNode node = new AnnotationNode(desc);
    int valuesSize = in.readInt();

    if (valuesSize > 0) {
      node.values = new ArrayList<>(valuesSize);

      for (int i = 0; i < valuesSize; i++) {
        Object value = readValue(in);
        node.values.add(value);
      }
    }

    return node;
  }

  public static TypeAnnotationNode readTypeAnnotationNode(CustomDataInput in) throws IOException {
    AnnotationNode node = readAnnotationNode(in);

    int typeRef = in.readInt();
    TypePath typePath = TypePathHelper.read(in);
    TypeAnnotationNode typeNode = new TypeAnnotationNode(typeRef, typePath, node.desc);
    typeNode.values = node.values;
    return typeNode;
  }

  public static LocalVariableAnnotationNode readLocalVariableAnnotationNode(CustomDataInput in, Function<Integer, LabelNode> labelMapper) throws IOException {
    TypeAnnotationNode typeNode = readTypeAnnotationNode(in);

    List<LabelNode> start = ListHelper.read(in, stream -> {
      int labelIndex = stream.readInt();
      return labelMapper.apply(labelIndex);
    });

    List<LabelNode> end = ListHelper.read(in, stream -> {
      int labelIndex = stream.readInt();
      return labelMapper.apply(labelIndex);
    });

    List<Integer> index = ListHelper.read(in, CustomDataInput::readInt);

    LocalVariableAnnotationNode localVarNode = new LocalVariableAnnotationNode(
            typeNode.typeRef,
            typeNode.typePath,
            start.toArray(new LabelNode[0]),
            end.toArray(new LabelNode[0]),
            index.stream().mapToInt(Integer::intValue).toArray(),
            typeNode.desc
    );
    localVarNode.values = typeNode.values;

    return localVarNode;
  }

  public static Object readValue(CustomDataInput in) throws IOException {
    int typeOrdinal = in.readByte();
    ValueType type = ValueType.values()[typeOrdinal];

    switch (type) {
      case STRING:
        return in.readUTF();
      case BYTE:
        return in.readByte();
      case CHAR:
        return in.readChar();
      case DOUBLE:
        return in.readDouble();
      case FLOAT:
        return in.readFloat();
      case INT:
        return in.readInt();
      case LONG:
        return in.readLong();
      case SHORT:
        return in.readShort();
      case BOOLEAN:
        return in.readBoolean();
      case ENUM:
        String enumTypeDesc = in.readUTF();
        String enumConstName = in.readUTF();
        return new String[]{enumTypeDesc, enumConstName};
      case CLASS:
        String classDesc = in.readUTF();
        return org.objectweb.asm.Type.getType(classDesc);
      case ANNOTATION:
        return readAnnotationNode(in);
      case ARRAY:
        int size = in.readInt();
        List<Object> list = new java.util.ArrayList<>(size);
        for (int i = 0; i < size; i++) {
          list.add(readValue(in));
        }
        return list;
      default:
        throw new IllegalArgumentException("Unsupported annotation value type ordinal: " + typeOrdinal);
    }
  }
}