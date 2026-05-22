package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.CustomDataInput;
import com.koyomiji.asmweaver.io.CustomDataOutput;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LabelNode;

import java.io.DataInput;
import java.io.IOException;
import java.util.function.Function;

public class ClassDiffUtils {

  public static ClassDiff diff(ClassNode class1, ClassNode class2) {
    ClassDiff diff = new ClassDiff();
    diff.version = ListDiffUtils.diff(ListHelper.ofNullable(class1.version), ListHelper.ofNullable(class2.version), Integer::equals);
    diff.access = ListDiffUtils.diff(ListHelper.ofNullable(class1.access), ListHelper.ofNullable(class2.access), Integer::equals);
    diff.name = ListDiffUtils.diff(ListHelper.ofNullable(class1.name), ListHelper.ofNullable(class2.name), String::equals);
    diff.signature = ListDiffUtils.diff(ListHelper.ofNullable(class1.signature), ListHelper.ofNullable(class2.signature), String::equals);
    diff.superName = ListDiffUtils.diff(ListHelper.ofNullable(class1.superName), ListHelper.ofNullable(class2.superName), String::equals);
    diff.interfaces = ListDiffUtils.diff(class1.interfaces, class2.interfaces, String::equals);
    diff.sourceFile = ListDiffUtils.diff(ListHelper.ofNullable(class1.sourceFile), ListHelper.ofNullable(class2.sourceFile), String::equals);
    diff.sourceDebug = ListDiffUtils.diff(ListHelper.ofNullable(class1.sourceDebug), ListHelper.ofNullable(class2.sourceDebug), String::equals);
    diff.module = KeyedListDiffUtils.diffNullableValue(
            class1.module,
            class2.module,
            ModuleDiffUtils::diff
    );
    diff.outerClass = ListDiffUtils.diff(ListHelper.ofNullable(class1.outerClass), ListHelper.ofNullable(class2.outerClass), String::equals);
    diff.outerMethod = ListDiffUtils.diff(ListHelper.ofNullable(class1.outerMethod), ListHelper.ofNullable(class2.outerMethod), String::equals);
    diff.outerMethodDesc = ListDiffUtils.diff(ListHelper.ofNullable(class1.outerMethodDesc), ListHelper.ofNullable(class2.outerMethodDesc), String::equals);
    diff.visibleAnnotations = ListDiffUtils.diff(ListHelper.nullToEmpty(class1.visibleAnnotations), ListHelper.nullToEmpty(class2.visibleAnnotations), AnnotationNodeHelper::equals);
    diff.invisibleAnnotations = ListDiffUtils.diff(ListHelper.nullToEmpty(class1.invisibleAnnotations), ListHelper.nullToEmpty(class2.invisibleAnnotations), AnnotationNodeHelper::equals);
    diff.visibleTypeAnnotations = ListDiffUtils.diff(ListHelper.nullToEmpty(class1.visibleTypeAnnotations), ListHelper.nullToEmpty(class2.visibleTypeAnnotations), AnnotationNodeHelper::equals);
    diff.invisibleTypeAnnotations = ListDiffUtils.diff(ListHelper.nullToEmpty(class1.invisibleTypeAnnotations), ListHelper.nullToEmpty(class2.invisibleTypeAnnotations), AnnotationNodeHelper::equals);
    // attributes
    diff.innerClasses = ListDiffUtils.diff(class1.innerClasses, class2.innerClasses, InnerClassNodeHelper::equals);
    diff.nestHostClass = ListDiffUtils.diff(ListHelper.ofNullable(class1.nestHostClass), ListHelper.ofNullable(class2.nestHostClass), String::equals);
    diff.nestMembers = ListDiffUtils.diff(ListHelper.nullToEmpty(class1.nestMembers), ListHelper.nullToEmpty(class2.nestMembers), String::equals);
    diff.permittedSubclasses = ListDiffUtils.diff(ListHelper.nullToEmpty(class1.permittedSubclasses), ListHelper.nullToEmpty(class2.permittedSubclasses), String::equals);
    diff.recordComponents = KeyedListDiffUtils.diff(ListHelper.nullToEmpty(class1.recordComponents), ListHelper.nullToEmpty(class2.recordComponents), (rc) -> new MemberKey(rc.name, rc.descriptor), RecordComponentDiffUtils::diff);
    diff.fields = KeyedListDiffUtils.diff(class1.fields, class2.fields, (f) -> new MemberKey(f.name, f.desc), FieldDiffUtils::diff);
    diff.methods = KeyedListDiffUtils.diff(class1.methods, class2.methods, (m) -> new MemberKey(m.name, m.desc), MethodDiffUtils::diff);
    return diff;
  }

  public static ClassNode patch(ClassNode node, ClassDiff diff) {
    ClassNode patched = new ClassNode();
    patched.version = ListDiffUtils.patchNonNullableValue(
            node.version,
            diff.version
    );
    patched.access = ListDiffUtils.patchNonNullableValue(
            node.access,
            diff.access
    );
    patched.name = ListDiffUtils.patchNonNullableValue(
            node.name,
            diff.name
    );
    patched.signature = ListDiffUtils.patchNullableValue(
            node.signature,
            diff.signature
    );
    patched.superName = ListDiffUtils.patchNullableValue(
            node.superName,
            diff.superName
    );
    patched.interfaces = ListDiffUtils.patch(
            node.interfaces,
            diff.interfaces
    );
    patched.sourceFile = ListDiffUtils.patchNullableValue(
            node.sourceFile,
            diff.sourceFile
    );
    patched.sourceDebug = ListDiffUtils.patchNullableValue(
            node.sourceDebug,
            diff.sourceDebug
    );
    patched.module = KeyedListDiffUtils.patchNullableValue(
            node.module,
            diff.module,
            ModuleDiffUtils::patch
    );
    patched.outerClass = ListDiffUtils.patchNullableValue(
            node.outerClass,
            diff.outerClass
    );
    patched.outerMethod = ListDiffUtils.patchNullableValue(
            node.outerMethod,
            diff.outerMethod
    );
    patched.outerMethodDesc = ListDiffUtils.patchNullableValue(
            node.outerMethodDesc,
            diff.outerMethodDesc
    );
    patched.visibleAnnotations = ListDiffUtils.patch(
            node.visibleAnnotations,
            diff.visibleAnnotations
    );
    patched.invisibleAnnotations = ListDiffUtils.patch(
            node.invisibleAnnotations,
            diff.invisibleAnnotations
    );
    patched.visibleTypeAnnotations = ListDiffUtils.patch(
            node.visibleTypeAnnotations,
            diff.visibleTypeAnnotations
    );
    patched.invisibleTypeAnnotations = ListDiffUtils.patch(
            node.invisibleTypeAnnotations,
            diff.invisibleTypeAnnotations
    );
    // TODO: attrs
    patched.innerClasses = ListDiffUtils.patch(
            node.innerClasses,
            diff.innerClasses
    );
    patched.nestHostClass = ListDiffUtils.patchNullableValue(
            node.nestHostClass,
            diff.nestHostClass
    );
    patched.nestMembers = ListDiffUtils.patch(
            node.nestMembers,
            diff.nestMembers
    );
    patched.permittedSubclasses = ListDiffUtils.patch(
            node.permittedSubclasses,
            diff.permittedSubclasses
    );
    patched.recordComponents = KeyedListDiffUtils.patch(
            node.recordComponents,
            diff.recordComponents,
            RecordComponentDiffUtils::patch
    );
    patched.fields = KeyedListDiffUtils.patch(
            node.fields,
            diff.fields,
            FieldDiffUtils::patch
    );
    patched.methods = KeyedListDiffUtils.patch(
            node.methods,
            diff.methods,
            MethodDiffUtils::patch
    );

    return patched;
  }

  public static void write(ClassDiff diff, CustomDataOutput out, Function<LabelNode, Integer> labelToIndex) throws IOException {
    ListDiffUtils.write(diff.version, out, (element, stream) -> stream.writeInt(element));
    ListDiffUtils.write(diff.access, out, (element, stream) -> stream.writeInt(element));
    ListDiffUtils.write(diff.name, out, (element, stream) -> stream.writeUTF(element));
    ListDiffUtils.write(diff.signature, out, (element, stream) -> stream.writeUTF(element));
    ListDiffUtils.write(diff.superName, out, (element, stream) -> stream.writeUTF(element));
    ListDiffUtils.write(diff.interfaces, out, (element, stream) -> stream.writeUTF(element));
    ListDiffUtils.write(diff.sourceFile, out, (element, stream) -> stream.writeUTF(element));
    ListDiffUtils.write(diff.sourceDebug, out, (element, stream) -> stream.writeUTF(element));
    KeyedListDiffUtils.write(diff.module, out,
            (element, stream) -> stream.writeInt(element),
            ModuleNodeHelper::write,
            ModuleDiffUtils::write
    );
    ListDiffUtils.write(diff.outerClass, out, (element, stream) -> stream.writeUTF(element));
    ListDiffUtils.write(diff.outerMethod, out, (element, stream) -> stream.writeUTF(element));
    ListDiffUtils.write(diff.outerMethodDesc, out, (element, stream) -> stream.writeUTF(element));
    ListDiffUtils.write(diff.visibleAnnotations, out, AnnotationNodeHelper::write);
    ListDiffUtils.write(diff.invisibleAnnotations, out, AnnotationNodeHelper::write);
    ListDiffUtils.write(diff.visibleTypeAnnotations, out, AnnotationNodeHelper::write);
    ListDiffUtils.write(diff.invisibleTypeAnnotations, out, AnnotationNodeHelper::write);
    ListDiffUtils.write(diff.innerClasses, out, InnerClassNodeHelper::write);
    ListDiffUtils.write(diff.nestHostClass, out, ((element, stream) -> stream.writeUTF(element)));
    ListDiffUtils.write(diff.nestMembers, out, ((element, stream) -> stream.writeUTF(element)));
    ListDiffUtils.write(diff.permittedSubclasses, out, ((element, stream) -> stream.writeUTF(element)));
    KeyedListDiffUtils.write(diff.recordComponents, out,
            MemberKeyUtils::write,
            RecordComponentNodeHelper::write,
            RecordComponentDiffUtils::write
    );
    KeyedListDiffUtils.write(diff.fields, out,
            MemberKeyUtils::write,
            FieldNodeHelper::write,
            FieldDiffUtils::write
    );
    KeyedListDiffUtils.write(diff.methods, out,
            MemberKeyUtils::write,
            (element, stream) -> MethodNodeHelper.write(element, stream, labelToIndex),
            (element, stream) -> MethodDiffUtils.write(element, stream, labelToIndex)
    );
  }

  public static ClassDiff read(CustomDataInput in, Function<Integer, LabelNode> indexToLabel) throws IOException {
    ClassDiff diff = new ClassDiff();
    diff.version = ListDiffUtils.read(in, DataInput::readInt);
    diff.access = ListDiffUtils.read(in, DataInput::readInt);
    diff.name = ListDiffUtils.read(in, DataInput::readUTF);
    diff.signature = ListDiffUtils.read(in, DataInput::readUTF);
    diff.superName = ListDiffUtils.read(in, DataInput::readUTF);
    diff.interfaces = ListDiffUtils.read(in, DataInput::readUTF);
    diff.sourceFile = ListDiffUtils.read(in, DataInput::readUTF);
    diff.sourceDebug = ListDiffUtils.read(in, DataInput::readUTF);
    diff.module = KeyedListDiffUtils.read(in, DataInput::readInt, ModuleNodeHelper::read, ModuleDiffUtils::read);
    diff.outerClass = ListDiffUtils.read(in, DataInput::readUTF);
    diff.outerMethod = ListDiffUtils.read(in, DataInput::readUTF);
    diff.outerMethodDesc = ListDiffUtils.read(in, DataInput::readUTF);
    diff.visibleAnnotations = ListDiffUtils.read(in, AnnotationNodeHelper::readAnnotationNode);
    diff.invisibleAnnotations = ListDiffUtils.read(in, AnnotationNodeHelper::readAnnotationNode);
    diff.visibleTypeAnnotations = ListDiffUtils.read(in, AnnotationNodeHelper::readTypeAnnotationNode);
    diff.invisibleTypeAnnotations = ListDiffUtils.read(in, AnnotationNodeHelper::readTypeAnnotationNode);
    diff.innerClasses = ListDiffUtils.read(in, InnerClassNodeHelper::read);
    diff.nestHostClass = ListDiffUtils.read(in, DataInput::readUTF);
    diff.nestMembers = ListDiffUtils.read(in, DataInput::readUTF);
    diff.permittedSubclasses = ListDiffUtils.read(in, DataInput::readUTF);
    diff.recordComponents = KeyedListDiffUtils.read(
            in,
            MemberKeyUtils::read,
            RecordComponentNodeHelper::read,
            RecordComponentDiffUtils::read
    );
    diff.fields = KeyedListDiffUtils.read(
            in,
            MemberKeyUtils::read,
            FieldNodeHelper::read,
            FieldDiffUtils::read
    );
    diff.methods = KeyedListDiffUtils.read(
            in,
            MemberKeyUtils::read,
            stream -> MethodNodeHelper.read(in, indexToLabel),
            stream -> MethodDiffUtils.read(in, indexToLabel)
    );
    return diff;
  }
}
