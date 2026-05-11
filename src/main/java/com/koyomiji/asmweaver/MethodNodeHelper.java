package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.HashCodeBuilder;
import org.objectweb.asm.tree.MethodNode;

import java.util.Objects;

public class MethodNodeHelper {
  public static boolean equals(MethodNode node1, MethodNode node2) {
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
            && Objects.equals(node1.exceptions, node2.exceptions)
            && Objects.equals(node1.parameters, node2.parameters)
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
            && InsnListHelper.equals(node1.instructions, node2.instructions)
            && ListHelper.equalsNullToEmpty(node1.tryCatchBlocks, node2.tryCatchBlocks, TryCatchBlockNodeHelper::equals)
            && node1.maxStack == node2.maxStack
            && node1.maxLocals == node2.maxLocals
            && ListHelper.equalsNullToEmpty(node1.localVariables, node2.localVariables, LocalVariableNodeHelper::equals)
            && ListHelper.equalsNullToEmpty(node1.visibleLocalVariableAnnotations, node2.visibleLocalVariableAnnotations, AnnotationNodeHelper::equals)
            && ListHelper.equalsNullToEmpty(node1.invisibleLocalVariableAnnotations, node2.invisibleLocalVariableAnnotations, AnnotationNodeHelper::equals);
  }

  public static int hashCode(MethodNode node) {
    if (node == null) {
      return 0;
    }

    return new HashCodeBuilder()
            .append(node.access)
            .append(node.name)
            .append(node.desc)
            .append(node.signature)
            .append(node.exceptions)
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
            .append(node.instructions, InsnListHelper::hashCode)
            .append(node.tryCatchBlocks, l -> ListHelper.hashCodeNullToEmpty(l, TryCatchBlockNodeHelper::hashCode))
            .append(node.maxStack)
            .append(node.maxLocals)
            .append(node.localVariables, l -> ListHelper.hashCodeNullToEmpty(l, LocalVariableNodeHelper::hashCode))
            .append(node.visibleLocalVariableAnnotations, l -> ListHelper.hashCodeNullToEmpty(l, AnnotationNodeHelper::hashCode))
            .append(node.invisibleLocalVariableAnnotations, l -> ListHelper.hashCodeNullToEmpty(l, AnnotationNodeHelper::hashCode))
            .build();
  }
}
