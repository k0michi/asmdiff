package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.CustomDataInput;
import com.koyomiji.asmweaver.io.CustomDataOutput;
import com.koyomiji.asmweaver.io.DataStreamHelper;
import com.koyomiji.asmweaver.util.AutoIncrementBiHashMap;
import com.koyomiji.asmweaver.util.HashCodeBuilder;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LabelNode;

import java.io.DataInput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class ClassNodeHelper {
  public static boolean equalsNormalizeLabels(ClassNode node1, ClassNode node2) {
    Map<LabelNode, LabelNode> labelMap = new HashMap<>();
    return equals(node1, node2, (l1, l2) -> MapHelper.putIfAbsentAndTest(labelMap, l1, l2));
  }

  public static boolean equals(ClassNode node1, ClassNode node2, BiPredicate<LabelNode, LabelNode> labelEquals) {
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
            && ListHelper.equalsNullToEmpty(node1.visibleAnnotations, node2.visibleAnnotations, AnnotationNodeHelper::equals)
            && ListHelper.equalsNullToEmpty(node1.invisibleAnnotations, node2.invisibleAnnotations, AnnotationNodeHelper::equals)
            && ListHelper.equalsNullToEmpty(node1.visibleTypeAnnotations, node2.visibleTypeAnnotations, AnnotationNodeHelper::equals)
            && ListHelper.equalsNullToEmpty(node1.invisibleTypeAnnotations, node2.invisibleTypeAnnotations, AnnotationNodeHelper::equals)
            && ListHelper.equalsNullToEmpty(node1.attrs, node2.attrs, Objects::equals)
            && ListHelper.equalsNullToEmpty(node1.innerClasses, node2.innerClasses, InnerClassNodeHelper::equals)
            && Objects.equals(node1.nestHostClass, node2.nestHostClass)
            && ListHelper.equalsNullToEmpty(node1.nestMembers, node2.nestMembers)
            && ListHelper.equalsNullToEmpty(node1.permittedSubclasses, node2.permittedSubclasses)
            && ListHelper.equalsNullToEmpty(node1.recordComponents, node2.recordComponents, RecordComponentNodeHelper::equals)
            && ListHelper.equals(node1.fields, node2.fields, FieldNodeHelper::equals)
            && ListHelper.equals(node1.methods, node2.methods, (m1, m2) -> MethodNodeHelper.equals(m1, m2, labelEquals));
  }

  public static int hashCodeNormalizeLabels(ClassNode node) {
    return hashCode(node, (new AutoIncrementBiHashMap<>())::get);
  }

  public static int hashCode(ClassNode node, ToIntFunction<LabelNode> labelHashCode) {
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
            .append(node.visibleAnnotations, l -> ListHelper.hashCodeNullToEmpty(l, AnnotationNodeHelper::hashCode))
            .append(node.invisibleAnnotations, l -> ListHelper.hashCodeNullToEmpty(l, AnnotationNodeHelper::hashCode))
            .append(node.visibleTypeAnnotations, l -> ListHelper.hashCodeNullToEmpty(l, AnnotationNodeHelper::hashCode))
            .append(node.invisibleTypeAnnotations, l -> ListHelper.hashCodeNullToEmpty(l, AnnotationNodeHelper::hashCode))
            .append(node.attrs, l -> ListHelper.hashCodeNullToEmpty(l, Objects::hash))
            .append(node.innerClasses, l -> ListHelper.hashCodeNullToEmpty(l, InnerClassNodeHelper::hashCode))
            .append(node.nestHostClass)
            .append(node.nestMembers, l -> ListHelper.hashCodeNullToEmpty(l, Objects::hash))
            .append(node.permittedSubclasses, l -> ListHelper.hashCodeNullToEmpty(l, Objects::hash))
            .append(node.recordComponents, l -> ListHelper.hashCodeNullToEmpty(l, RecordComponentNodeHelper::hashCode))
            .append(node.fields, l -> ListHelper.hashCode(l, FieldNodeHelper::hashCode))
            .append(node.methods, l -> ListHelper.hashCode(l, m -> MethodNodeHelper.hashCode(m, labelHashCode)))
            .build();
  }

  public static void write(ClassNode node, CustomDataOutput out, Function<LabelNode, Integer> labelToIndex) throws IOException {
    out.writeInt(node.version);
    out.writeInt(node.access);
    out.writeUTF(node.name);
    NullableHelper.write(node.signature, out, (element, stream) -> stream.writeUTF(element));
    NullableHelper.write(node.superName, out, (element, stream) -> stream.writeUTF(element));
    ListHelper.write(node.interfaces, out, (element, stream) -> stream.writeUTF(element));
    NullableHelper.write(node.sourceFile, out, (element, stream) -> stream.writeUTF(element));
    NullableHelper.write(node.sourceDebug, out, (element, stream) -> stream.writeUTF(element));
    NullableHelper.write(node.module, out, ModuleNodeHelper::write);
    NullableHelper.write(node.outerClass, out, (element, stream) -> stream.writeUTF(element));
    NullableHelper.write(node.outerMethod, out, (element, stream) -> stream.writeUTF(element));
    NullableHelper.write(node.outerMethodDesc, out, (element, stream) -> stream.writeUTF(element));
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
    ListHelper.write(
            ListHelper.nullToEmpty(node.innerClasses),
            out,
            InnerClassNodeHelper::write
    );
    NullableHelper.write(node.nestHostClass, out, (element, stream) -> stream.writeUTF(element));
    ListHelper.write(
            ListHelper.nullToEmpty(node.nestMembers),
            out,
            (element, stream) -> stream.writeUTF(element)
    );
    ListHelper.write(
            ListHelper.nullToEmpty(node.permittedSubclasses),
            out,
            (element, stream) -> stream.writeUTF(element)
    );
    ListHelper.write(
            ListHelper.nullToEmpty(node.recordComponents),
            out,
            RecordComponentNodeHelper::write
    );
    ListHelper.write(
            node.fields,
            out,
            FieldNodeHelper::write
    );
    ListHelper.write(
            node.methods,
            out,
            MethodNodeHelper::write
    );
  }

  public static ClassNode read(CustomDataInput in, Function<Integer, LabelNode> labelToIndex) throws IOException {
    ClassNode node = new ClassNode();
    node.version = in.readInt();
    node.access = in.readInt();
    node.name = in.readUTF();
    node.signature = DataStreamHelper.readUTFNullable(in);
    node.superName = DataStreamHelper.readUTFNullable(in);
    node.interfaces = ListHelper.read(in, DataInput::readUTF);
    node.sourceFile = NullableHelper.read(in, DataInput::readUTF);
    node.sourceDebug = NullableHelper.read(in, DataInput::readUTF);
    node.module = NullableHelper.read(in, ModuleNodeHelper::read);
    node.outerClass = NullableHelper.read(in, DataInput::readUTF);
    node.outerMethod = NullableHelper.read(in, DataInput::readUTF);
    node.outerMethodDesc = NullableHelper.read(in, DataInput::readUTF);
    node.visibleAnnotations = ListHelper.read(in, AnnotationNodeHelper::readAnnotationNode);
    node.invisibleAnnotations = ListHelper.read(in, AnnotationNodeHelper::readAnnotationNode);
    node.visibleTypeAnnotations = ListHelper.read(in, AnnotationNodeHelper::readTypeAnnotationNode);
    node.invisibleTypeAnnotations = ListHelper.read(in, AnnotationNodeHelper::readTypeAnnotationNode);
    node.innerClasses = ListHelper.read(in, InnerClassNodeHelper::read);
    node.nestHostClass = NullableHelper.read(in, DataInput::readUTF);
    node.nestMembers = ListHelper.read(in, DataInput::readUTF);
    node.permittedSubclasses = ListHelper.read(in, DataInput::readUTF);
    node.recordComponents = ListHelper.read(in, RecordComponentNodeHelper::read);
    node.fields = ListHelper.read(in, FieldNodeHelper::read);
    node.methods = ListHelper.read(in, MethodNodeHelper::read);
    return node;
  }
}
