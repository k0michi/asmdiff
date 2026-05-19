package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.CustomDataInput;
import com.koyomiji.asmweaver.io.CustomDataOutput;
import com.koyomiji.asmweaver.io.DataStreamHelper;
import com.koyomiji.asmweaver.util.HashCodeBuilder;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.DataInput;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class MethodNodeHelper {
  public static boolean equals(MethodNode node1, MethodNode node2) {
    return equals(node1, node2, Objects::equals);
  }

  public static boolean equals(MethodNode node1, MethodNode node2, BiPredicate<LabelNode, LabelNode> labelEquals) {
    if (node1 == node2) {
      return true;
    }

    if (node1 == null || node2 == null) {
      return false;
    }

    if (node1.getClass() != node2.getClass()) {
      return false;
    }

    return node1.access == node2.access
            && Objects.equals(node1.name, node2.name)
            && Objects.equals(node1.desc, node2.desc)
            && Objects.equals(node1.signature, node2.signature)
            && ListHelper.equalsNullToEmpty(node1.exceptions, node2.exceptions, Objects::equals)
            && ListHelper.equalsNullToEmpty(node1.parameters, node2.parameters, ParameterNodeHelper::equals)
            && ListHelper.equalsNullToEmpty(node1.visibleAnnotations, node2.visibleAnnotations, AnnotationNodeHelper::equals)
            && ListHelper.equalsNullToEmpty(node1.invisibleAnnotations, node2.invisibleAnnotations, AnnotationNodeHelper::equals)
            && ListHelper.equalsNullToEmpty(node1.visibleTypeAnnotations, node2.visibleTypeAnnotations, AnnotationNodeHelper::equals)
            && ListHelper.equalsNullToEmpty(node1.invisibleTypeAnnotations, node2.invisibleTypeAnnotations, AnnotationNodeHelper::equals)
            && ListHelper.equalsNullToEmpty(node1.attrs, node2.attrs, Objects::equals)
            && Objects.equals(node1.annotationDefault, node2.annotationDefault)
            && node1.visibleAnnotableParameterCount == node2.visibleAnnotableParameterCount
            && ListHelper.equals(
            ListHelper.ofNullableArray(node1.visibleParameterAnnotations),
            ListHelper.ofNullableArray(node2.visibleParameterAnnotations),
            (a, b) -> ListHelper.equals(a, b, AnnotationNodeHelper::equals)
    )
            && node1.invisibleAnnotableParameterCount == node2.invisibleAnnotableParameterCount
            && ListHelper.equals(
            ListHelper.ofNullableArray(node1.invisibleParameterAnnotations),
            ListHelper.ofNullableArray(node2.invisibleParameterAnnotations),
            (a, b) -> ListHelper.equals(a, b, AnnotationNodeHelper::equals)
    )
            && InsnListHelper.equals(node1.instructions, node2.instructions, labelEquals)
            && ListHelper.equalsNullToEmpty(node1.tryCatchBlocks, node2.tryCatchBlocks, (tcb1, tcb2) -> TryCatchBlockNodeHelper.equals(tcb1, tcb2, labelEquals))
            && node1.maxStack == node2.maxStack
            && node1.maxLocals == node2.maxLocals
            && ListHelper.equalsNullToEmpty(node1.localVariables, node2.localVariables, (lv1, lv2) -> LocalVariableNodeHelper.equals(lv1, lv2, labelEquals))
            && ListHelper.equalsNullToEmpty(node1.visibleLocalVariableAnnotations, node2.visibleLocalVariableAnnotations, (a, b) -> AnnotationNodeHelper.equals(a, b, labelEquals))
            && ListHelper.equalsNullToEmpty(node1.invisibleLocalVariableAnnotations, node2.invisibleLocalVariableAnnotations, (a, b) -> AnnotationNodeHelper.equals(a, b, labelEquals));
  }

  public static int hashCode(MethodNode node) {
    return hashCode(node, Objects::hashCode);
  }

  public static int hashCode(MethodNode node, ToIntFunction<LabelNode> labelHashCode) {
    if (node == null) {
      return 0;
    }

    return new HashCodeBuilder()
            .append(node.access)
            .append(node.name)
            .append(node.desc)
            .append(node.signature)
            .append(node.exceptions,
                    l -> ListHelper.hashCodeNullToEmpty(l, Objects::hashCode)
            )
            .append(node.parameters,
                    l -> ListHelper.hashCodeNullToEmpty(l, ParameterNodeHelper::hashCode)
            )
            .append(node.visibleAnnotations,
                    l -> ListHelper.hashCodeNullToEmpty(l, AnnotationNodeHelper::hashCode)
            )
            .append(node.invisibleAnnotations,
                    l -> ListHelper.hashCodeNullToEmpty(l, AnnotationNodeHelper::hashCode)
            )
            .append(node.visibleTypeAnnotations,
                    l -> ListHelper.hashCodeNullToEmpty(l, AnnotationNodeHelper::hashCode)
            )
            .append(node.invisibleTypeAnnotations,
                    l -> ListHelper.hashCodeNullToEmpty(l, AnnotationNodeHelper::hashCode)
            )
            .append(node.attrs,
                    l -> ListHelper.hashCodeNullToEmpty(l, Objects::hashCode)
            )
            .append(node.annotationDefault)
            .append(node.visibleAnnotableParameterCount)
            .append(ListHelper.ofNullableArray(node.visibleParameterAnnotations),
                    l -> ListHelper.hashCodeNullToEmpty(
                            l, al -> ListHelper.hashCode(al, AnnotationNodeHelper::hashCode)
                    )
            )
            .append(node.invisibleAnnotableParameterCount)
            .append(ListHelper.ofNullableArray(node.invisibleParameterAnnotations),
                    l -> ListHelper.hashCodeNullToEmpty(
                            l, al -> ListHelper.hashCode(al, AnnotationNodeHelper::hashCode)
                    )
            )
            .append(node.instructions, (list) -> InsnListHelper.hashCode(list, labelHashCode))
            .append(node.maxStack)
            .append(node.maxLocals)
            .append(node.localVariables, l -> ListHelper.hashCodeNullToEmpty(l, lv -> LocalVariableNodeHelper.hashCode(lv, labelHashCode)))
            .append(node.visibleLocalVariableAnnotations, l -> ListHelper.hashCodeNullToEmpty(l, a -> AnnotationNodeHelper.hashCode(a, labelHashCode)))
            .append(node.invisibleLocalVariableAnnotations, l -> ListHelper.hashCodeNullToEmpty(l, a -> AnnotationNodeHelper.hashCode(a, labelHashCode)))
            .build();
  }

  public static void write(MethodNode node, CustomDataOutput out, Function<LabelNode, Integer> labelToIndex) throws IOException {
    out.writeInt(node.access);
    out.writeUTF(node.name);
    out.writeUTF(node.desc);
    DataStreamHelper.writeUTFNullable(out, node.signature);
    ListHelper.write(
            ListHelper.nullToEmpty(node.exceptions),
            out,
            (element, stream) -> stream.writeUTF(element)
    );
    ListHelper.write(
            ListHelper.nullToEmpty(node.parameters),
            out,
            ParameterNodeHelper::write
    );
    ListHelper.write(
            ListHelper.nullToEmpty(node.visibleAnnotations),
            out,
            AnnotationNodeHelper::write
    );
    ListHelper.write(
            ListHelper.nullToEmpty(node.invisibleAnnotations),
            out,
            AnnotationNodeHelper::write
    );
    ListHelper.write(
            ListHelper.nullToEmpty(node.visibleTypeAnnotations),
            out,
            AnnotationNodeHelper::write
    );
    ListHelper.write(
            ListHelper.nullToEmpty(node.invisibleTypeAnnotations),
            out,
            AnnotationNodeHelper::write
    );
    // TODO: attrs
    NullableHelper.write(
            node.annotationDefault,
            out,
            AnnotationNodeHelper::writeValue
    );
    out.writeInt(node.visibleAnnotableParameterCount);
    ListHelper.write(
            ListHelper.ofNullableArray(node.visibleParameterAnnotations),
            out,
            (element, stream) -> {
              ListHelper.write(
                      element,
                      stream,
                      AnnotationNodeHelper::write
              );
            }
    );
    ListHelper.write(
            ListHelper.ofNullableArray(node.invisibleParameterAnnotations),
            out,
            (element, stream) -> {
              ListHelper.write(
                      element,
                      stream,
                      AnnotationNodeHelper::write
              );
            }
    );
    ListHelper.write(
            new InsnListListAdapter(node.instructions),
            out,
            (element, stream) -> {
              AbstractInsnNodeHelper.write(
                      element,
                      stream,
                      labelToIndex
              );
            }
    );
    ListHelper.write(
            node.tryCatchBlocks,
            out,
            (element, stream) -> {
              TryCatchBlockNodeHelper.write(
                      element,
                      stream,
                      labelToIndex
              );
            }
    );
    out.writeInt(node.maxStack);
    out.writeInt(node.maxLocals);
    ListHelper.write(
            node.localVariables,
            out,
            (element, stream) -> {
              LocalVariableNodeHelper.write(
                      element,
                      stream,
                      labelToIndex
              );
            }
    );
    ListHelper.write(
            ListHelper.nullToEmpty(node.visibleLocalVariableAnnotations),
            out,
            (element, stream) -> {
              AnnotationNodeHelper.write(
                      element,
                      stream,
                      labelToIndex
              );
            }
    );
    ListHelper.write(
            ListHelper.nullToEmpty(node.invisibleLocalVariableAnnotations),
            out,
            (element, stream) -> {
              AnnotationNodeHelper.write(
                      element,
                      stream,
                      labelToIndex
              );
            }
    );
  }

  public static MethodNode read(CustomDataInput in, Function<Integer, LabelNode> indexToLabel) throws IOException {
    int access = in.readInt();
    String name = in.readUTF();
    String desc = in.readUTF();
    String signature = DataStreamHelper.readUTFNullable(in);
    List<String> exceptions = ListHelper.read(
            in,
            DataInput::readUTF
    );
    MethodNode node = new MethodNode(
            access, name, desc, signature, exceptions.toArray(new String[0])
    );
    node.parameters = ListHelper.read(
            in,
            ParameterNodeHelper::read
    );
    node.visibleAnnotations = ListHelper.read(
            in,
            AnnotationNodeHelper::readAnnotationNode
    );
    node.invisibleAnnotations = ListHelper.read(
            in,
            AnnotationNodeHelper::readAnnotationNode
    );
    node.visibleTypeAnnotations = ListHelper.read(
            in,
            AnnotationNodeHelper::readTypeAnnotationNode
    );
    node.invisibleTypeAnnotations = ListHelper.read(
            in,
            AnnotationNodeHelper::readTypeAnnotationNode
    );
    // TODO: attrs
    node.annotationDefault = NullableHelper.read(
            in,
            AnnotationNodeHelper::readValue
    );
    node.tryCatchBlocks = ListHelper.read(
            in,
            stream -> TryCatchBlockNodeHelper.read(stream, indexToLabel)
    );
    node.maxStack = in.readInt();
    node.maxLocals = in.readInt();
    node.localVariables = ListHelper.read(
            in,
            stream -> LocalVariableNodeHelper.read(stream, indexToLabel)
    );
    node.visibleLocalVariableAnnotations = ListHelper.read(
            in,
            stream -> AnnotationNodeHelper.readLocalVariableAnnotationNode(stream, indexToLabel)
    );
    node.invisibleLocalVariableAnnotations = ListHelper.read(
            in,
            stream -> AnnotationNodeHelper.readLocalVariableAnnotationNode(stream, indexToLabel)
    );

    return node;
  }
}
