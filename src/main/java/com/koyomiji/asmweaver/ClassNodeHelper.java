package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.HashCodeBuilder;
import org.objectweb.asm.tree.ClassNode;

import java.util.Objects;

public class ClassNodeHelper {
  public static boolean equals(ClassNode node1, ClassNode node2) {
    if (node1 == node2) {
      return true;
    }

    if (node1 == null || node2 == null) {
      return false;
    }

    if (node1.getClass() != node2.getClass()) {
      return false;
    }

    return node1.version == node2.version
            && node1.access == node2.access
            && Objects.equals(node1.name, node2.name)
            && Objects.equals(node1.signature, node2.signature)
            && Objects.equals(node1.superName, node2.superName)
            && Objects.equals(node1.interfaces, node2.interfaces)
            && Objects.equals(node1.sourceFile, node2.sourceFile)
            && Objects.equals(node1.sourceDebug, node2.sourceDebug)
            && ModuleNodeHelper.equals(node1.module, node2.module)
            && Objects.equals(node1.outerClass, node2.outerClass)
            && Objects.equals(node1.outerMethod, node2.outerMethod)
            && Objects.equals(node1.outerMethodDesc, node2.outerMethodDesc)
            && ListHelper.equals(node1.visibleAnnotations, node2.visibleAnnotations, AnnotationNodeHelper::equals)
            && ListHelper.equals(node1.invisibleAnnotations, node2.invisibleAnnotations, AnnotationNodeHelper::equals)
            && ListHelper.equals(node1.visibleTypeAnnotations, node2.visibleTypeAnnotations, AnnotationNodeHelper::equals)
            && ListHelper.equals(node1.invisibleTypeAnnotations, node2.invisibleTypeAnnotations, AnnotationNodeHelper::equals)
            && ListHelper.equals(node1.attrs, node2.attrs, Objects::equals)
            && ListHelper.equals(node1.innerClasses, node2.innerClasses, InnerClassNodeHelper::equals)
            && Objects.equals(node1.nestHostClass, node2.nestHostClass)
            && ListHelper.equals(node1.nestMembers, node2.nestMembers)
            && ListHelper.equals(node1.permittedSubclasses, node2.permittedSubclasses)
            && ListHelper.equals(node1.recordComponents, node2.recordComponents, RecordComponentNodeHelper::equals)
            && ListHelper.equals(node1.fields, node2.fields, FieldNodeHelper::equals)
            && ListHelper.equals(node1.methods, node2.methods, MethodNodeHelper::equals);
  }

  public static int hashCode(ClassNode node) {
    if (node == null) {
      return 0;
    }

    return new HashCodeBuilder()
            .append(node.version)
            .append(node.access)
            .append(node.name)
            .append(node.signature)
            .append(node.superName)
            .append(node.interfaces)
            .append(node.sourceFile)
            .append(node.sourceDebug)
            .append(node.module, ModuleNodeHelper::hashCode)
            .append(node.outerClass)
            .append(node.outerMethod)
            .append(node.outerMethodDesc)
            .append(node.visibleAnnotations, l -> ListHelper.hashCode(l, AnnotationNodeHelper::hashCode))
            .append(node.invisibleAnnotations, l -> ListHelper.hashCode(l, AnnotationNodeHelper::hashCode))
            .append(node.visibleTypeAnnotations, l -> ListHelper.hashCode(l, AnnotationNodeHelper::hashCode))
            .append(node.invisibleTypeAnnotations, l -> ListHelper.hashCode(l, AnnotationNodeHelper::hashCode))
            .append(node.attrs, l -> ListHelper.hashCode(l, Objects::hash))
            .append(node.innerClasses, l -> ListHelper.hashCode(l, InnerClassNodeHelper::hashCode))
            .append(node.nestHostClass)
            .append(node.nestMembers, l -> ListHelper.hashCode(l, Objects::hash))
            .append(node.permittedSubclasses, l -> ListHelper.hashCode(l, Objects::hash))
            .append(node.recordComponents, l -> ListHelper.hashCode(l, RecordComponentNodeHelper::hashCode))
            .append(node.fields, l -> ListHelper.hashCode(l, FieldNodeHelper::hashCode))
            .append(node.methods, l -> ListHelper.hashCode(l, MethodNodeHelper::hashCode))
            .build();
  }
}
