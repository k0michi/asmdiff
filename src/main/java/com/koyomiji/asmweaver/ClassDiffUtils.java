package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.CustomDataInput;
import com.koyomiji.asmweaver.io.CustomDataOutput;
import com.koyomiji.asmweaver.util.tuple.Pair;
import org.objectweb.asm.tree.*;

import java.io.DataInput;
import java.io.IOException;

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

    if (diff.version == null
            && diff.access == null
            && diff.name == null
            && diff.signature == null
            && diff.superName == null
            && diff.interfaces == null
            && diff.sourceFile == null
            && diff.sourceDebug == null
            && diff.module == null
            && diff.outerClass == null
            && diff.outerMethod == null
            && diff.outerMethodDesc == null
            && diff.visibleAnnotations == null
            && diff.invisibleAnnotations == null
            && diff.visibleTypeAnnotations == null
            && diff.invisibleTypeAnnotations == null
            && diff.innerClasses == null
            && diff.nestHostClass == null
            && diff.nestMembers == null
            && diff.permittedSubclasses == null
            && diff.recordComponents == null
            && diff.fields == null
            && diff.methods == null
    ) {
      return null;
    }


    return diff;
  }

  public static ClassNode patch(ClassNode node, ClassDiff diff) {
    if (diff == null) {
      return node;
    }

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

  public static ClassDiff invert(ClassDiff diff) {
    if (diff == null) {
      return null;
    }

    ClassDiff inverted = new ClassDiff();
    inverted.version = ListDiffUtils.invert(diff.version);
    inverted.access = ListDiffUtils.invert(diff.access);
    inverted.name = ListDiffUtils.invert(diff.name);
    inverted.signature = ListDiffUtils.invert(diff.signature);
    inverted.superName = ListDiffUtils.invert(diff.superName);
    inverted.interfaces = ListDiffUtils.invert(diff.interfaces);
    inverted.sourceFile = ListDiffUtils.invert(diff.sourceFile);
    inverted.sourceDebug = ListDiffUtils.invert(diff.sourceDebug);
    inverted.module = KeyedListDiffUtils.invert(
            diff.module,
            ModuleDiffUtils::invert
    );
    inverted.outerClass = ListDiffUtils.invert(diff.outerClass);
    inverted.outerMethod = ListDiffUtils.invert(diff.outerMethod);
    inverted.outerMethodDesc = ListDiffUtils.invert(diff.outerMethodDesc);
    inverted.visibleAnnotations = ListDiffUtils.invert(diff.visibleAnnotations);
    inverted.invisibleAnnotations = ListDiffUtils.invert(diff.invisibleAnnotations);
    inverted.visibleTypeAnnotations = ListDiffUtils.invert(diff.visibleTypeAnnotations);
    inverted.invisibleTypeAnnotations = ListDiffUtils.invert(diff.invisibleTypeAnnotations);
    inverted.innerClasses = ListDiffUtils.invert(diff.innerClasses);
    inverted.nestHostClass = ListDiffUtils.invert(diff.nestHostClass);
    inverted.nestMembers = ListDiffUtils.invert(diff.nestMembers);
    inverted.permittedSubclasses = ListDiffUtils.invert(diff.permittedSubclasses);
    inverted.recordComponents = KeyedListDiffUtils.invert(
            diff.recordComponents,
            RecordComponentDiffUtils::invert
    );
    inverted.fields = KeyedListDiffUtils.invert(
            diff.fields,
            FieldDiffUtils::invert
    );
    inverted.methods = KeyedListDiffUtils.invert(
            diff.methods,
            MethodDiffUtils::invert
    );
    return inverted;
  }

  public static ClassDiff compose(ClassDiff diff1, ClassDiff diff2) {
    if (diff1 == null) {
      return diff2;
    }

    if (diff2 == null) {
      return diff1;
    }

    ClassDiff composed = new ClassDiff();
    composed.version = ListDiffUtils.compose(diff1.version, diff2.version, Integer::equals);
    composed.access = ListDiffUtils.compose(diff1.access, diff2.access, Integer::equals);
    composed.name = ListDiffUtils.compose(diff1.name, diff2.name, String::equals);
    composed.signature = ListDiffUtils.compose(diff1.signature, diff2.signature, String::equals);
    composed.superName = ListDiffUtils.compose(diff1.superName, diff2.superName, String::equals);
    composed.interfaces = ListDiffUtils.compose(diff1.interfaces, diff2.interfaces, String::equals);
    composed.sourceFile = ListDiffUtils.compose(diff1.sourceFile, diff2.sourceFile, String::equals);
    composed.sourceDebug = ListDiffUtils.compose(diff1.sourceDebug, diff2.sourceDebug, String::equals);
    composed.module = KeyedListDiffUtils.compose(
            diff1.module,
            diff2.module,
            ModuleDiffUtils::compose,
            ModuleDiffUtils::patch,
            ModuleDiffUtils::invert
    );
    composed.outerClass = ListDiffUtils.compose(diff1.outerClass, diff2.outerClass, String::equals);
    composed.outerMethod = ListDiffUtils.compose(diff1.outerMethod, diff2.outerMethod, String::equals);
    composed.outerMethodDesc = ListDiffUtils.compose(diff1.outerMethodDesc, diff2.outerMethodDesc, String::equals);
    composed.visibleAnnotations = ListDiffUtils.compose(diff1.visibleAnnotations, diff2.visibleAnnotations, AnnotationNodeHelper::equals);
    composed.invisibleAnnotations = ListDiffUtils.compose(diff1.invisibleAnnotations, diff2.invisibleAnnotations, AnnotationNodeHelper::equals);
    composed.visibleTypeAnnotations = ListDiffUtils.compose(diff1.visibleTypeAnnotations, diff2.visibleTypeAnnotations, AnnotationNodeHelper::equals);
    composed.invisibleTypeAnnotations = ListDiffUtils.compose(diff1.invisibleTypeAnnotations, diff2.invisibleTypeAnnotations, AnnotationNodeHelper::equals);
    composed.innerClasses = ListDiffUtils.compose(diff1.innerClasses, diff2.innerClasses, InnerClassNodeHelper::equals);
    composed.nestHostClass = ListDiffUtils.compose(diff1.nestHostClass, diff2.nestHostClass, String::equals);
    composed.nestMembers = ListDiffUtils.compose(diff1.nestMembers, diff2.nestMembers, String::equals);
    composed.permittedSubclasses = ListDiffUtils.compose(diff1.permittedSubclasses, diff2.permittedSubclasses, String::equals);
    composed.recordComponents = KeyedListDiffUtils.compose(
            diff1.recordComponents,
            diff2.recordComponents,
            RecordComponentDiffUtils::compose,
            RecordComponentDiffUtils::patch,
            RecordComponentDiffUtils::invert
    );
    composed.fields = KeyedListDiffUtils.compose(
            diff1.fields,
            diff2.fields,
            FieldDiffUtils::compose,
            FieldDiffUtils::patch,
            FieldDiffUtils::invert
    );
    composed.methods = KeyedListDiffUtils.compose(
            diff1.methods,
            diff2.methods,
            MethodDiffUtils::compose,
            MethodDiffUtils::patch,
            MethodDiffUtils::invert
    );
    return composed;
  }

  public static Pair<ClassDiff, ClassDiff> commute(ClassDiff diff1, ClassDiff diff2) throws ConflictException {
    if (diff1 == null || diff2 == null) {
      return Pair.of(diff2, diff1);
    }

    ClassDiff diffPrime2 = new ClassDiff();
    ClassDiff diffPrime1 = new ClassDiff();

    Pair<ListDiff<Integer>, ListDiff<Integer>> version = ListDiffUtils.commute(diff1.version, diff2.version, Integer::equals);
    diffPrime2.version = version.first;
    diffPrime1.version = version.second;

    Pair<ListDiff<Integer>, ListDiff<Integer>> access = ListDiffUtils.commute(diff1.access, diff2.access, Integer::equals);
    diffPrime2.access = access.first;
    diffPrime1.access = access.second;

    Pair<ListDiff<String>, ListDiff<String>> name = ListDiffUtils.commute(diff1.name, diff2.name, String::equals);
    diffPrime2.name = name.first;
    diffPrime1.name = name.second;

    Pair<ListDiff<String>, ListDiff<String>> signature = ListDiffUtils.commute(diff1.signature, diff2.signature, String::equals);
    diffPrime2.signature = signature.first;
    diffPrime1.signature = signature.second;

    Pair<ListDiff<String>, ListDiff<String>> superName = ListDiffUtils.commute(diff1.superName, diff2.superName, String::equals);
    diffPrime2.superName = superName.first;
    diffPrime1.superName = superName.second;

    Pair<ListDiff<String>, ListDiff<String>> interfaces = ListDiffUtils.commute(diff1.interfaces, diff2.interfaces, String::equals);
    diffPrime2.interfaces = interfaces.first;
    diffPrime1.interfaces = interfaces.second;

    Pair<ListDiff<String>, ListDiff<String>> sourceFile = ListDiffUtils.commute(diff1.sourceFile, diff2.sourceFile, String::equals);
    diffPrime2.sourceFile = sourceFile.first;
    diffPrime1.sourceFile = sourceFile.second;

    Pair<ListDiff<String>, ListDiff<String>> sourceDebug = ListDiffUtils.commute(diff1.sourceDebug, diff2.sourceDebug, String::equals);
    diffPrime2.sourceDebug = sourceDebug.first;
    diffPrime1.sourceDebug = sourceDebug.second;

    Pair<KeyedListDiff<Integer, ModuleNode, ModuleDiff>, KeyedListDiff<Integer, ModuleNode, ModuleDiff>> module = KeyedListDiffUtils.commute(
            diff1.module,
            diff2.module,
            ModuleDiffUtils::commute,
            ModuleDiffUtils::diff
    );
    diffPrime2.module = module.first;
    diffPrime1.module = module.second;

    Pair<ListDiff<String>, ListDiff<String>> outerClass = ListDiffUtils.commute(diff1.outerClass, diff2.outerClass, String::equals);
    diffPrime2.outerClass = outerClass.first;
    diffPrime1.outerClass = outerClass.second;

    Pair<ListDiff<String>, ListDiff<String>> outerMethod = ListDiffUtils.commute(diff1.outerMethod, diff2.outerMethod, String::equals);
    diffPrime2.outerMethod = outerMethod.first;
    diffPrime1.outerMethod = outerMethod.second;

    Pair<ListDiff<String>, ListDiff<String>> outerMethodDesc = ListDiffUtils.commute(diff1.outerMethodDesc, diff2.outerMethodDesc, String::equals);
    diffPrime2.outerMethodDesc = outerMethodDesc.first;
    diffPrime1.outerMethodDesc = outerMethodDesc.second;

    Pair<ListDiff<AnnotationNode>, ListDiff<AnnotationNode>> visibleAnnotations = ListDiffUtils.commute(diff1.visibleAnnotations, diff2.visibleAnnotations, AnnotationNodeHelper::equals);
    diffPrime2.visibleAnnotations = visibleAnnotations.first;
    diffPrime1.visibleAnnotations = visibleAnnotations.second;

    Pair<ListDiff<AnnotationNode>, ListDiff<AnnotationNode>> invisibleAnnotations = ListDiffUtils.commute(diff1.invisibleAnnotations, diff2.invisibleAnnotations, AnnotationNodeHelper::equals);
    diffPrime2.invisibleAnnotations = invisibleAnnotations.first;
    diffPrime1.invisibleAnnotations = invisibleAnnotations.second;

    Pair<ListDiff<TypeAnnotationNode>, ListDiff<TypeAnnotationNode>> visibleTypeAnnotations = ListDiffUtils.commute(diff1.visibleTypeAnnotations, diff2.visibleTypeAnnotations, AnnotationNodeHelper::equals);
    diffPrime2.visibleTypeAnnotations = visibleTypeAnnotations.first;
    diffPrime1.visibleTypeAnnotations = visibleTypeAnnotations.second;

    Pair<ListDiff<TypeAnnotationNode>, ListDiff<TypeAnnotationNode>> invisibleTypeAnnotations = ListDiffUtils.commute(diff1.invisibleTypeAnnotations, diff2.invisibleTypeAnnotations, AnnotationNodeHelper::equals);
    diffPrime2.invisibleTypeAnnotations = invisibleTypeAnnotations.first;
    diffPrime1.invisibleTypeAnnotations = invisibleTypeAnnotations.second;

    Pair<ListDiff<InnerClassNode>, ListDiff<InnerClassNode>> innerClasses = ListDiffUtils.commute(diff1.innerClasses, diff2.innerClasses, InnerClassNodeHelper::equals);
    diffPrime2.innerClasses = innerClasses.first;
    diffPrime1.innerClasses = innerClasses.second;

    Pair<ListDiff<String>, ListDiff<String>> nestHostClass = ListDiffUtils.commute(diff1.nestHostClass, diff2.nestHostClass, String::equals);
    diffPrime2.nestHostClass = nestHostClass.first;
    diffPrime1.nestHostClass = nestHostClass.second;

    Pair<ListDiff<String>, ListDiff<String>> nestMembers = ListDiffUtils.commute(diff1.nestMembers, diff2.nestMembers, String::equals);
    diffPrime2.nestMembers = nestMembers.first;
    diffPrime1.nestMembers = nestMembers.second;

    Pair<ListDiff<String>, ListDiff<String>> permittedSubclasses = ListDiffUtils.commute(diff1.permittedSubclasses, diff2.permittedSubclasses, String::equals);
    diffPrime2.permittedSubclasses = permittedSubclasses.first;
    diffPrime1.permittedSubclasses = permittedSubclasses.second;

    Pair<KeyedListDiff<MemberKey, RecordComponentNode, RecordComponentDiff>, KeyedListDiff<MemberKey, RecordComponentNode, RecordComponentDiff>> recordComponents = KeyedListDiffUtils.commute(
            diff1.recordComponents,
            diff2.recordComponents,
            RecordComponentDiffUtils::commute,
            RecordComponentDiffUtils::diff
    );
    diffPrime2.recordComponents = recordComponents.first;
    diffPrime1.recordComponents = recordComponents.second;

    Pair<KeyedListDiff<MemberKey, FieldNode, FieldDiff>, KeyedListDiff<MemberKey, FieldNode, FieldDiff>> fields = KeyedListDiffUtils.commute(
            diff1.fields,
            diff2.fields,
            FieldDiffUtils::commute,
            FieldDiffUtils::diff
    );
    diffPrime2.fields = fields.first;
    diffPrime1.fields = fields.second;

    Pair<KeyedListDiff<MemberKey, MethodNode, MethodDiff>, KeyedListDiff<MemberKey, MethodNode, MethodDiff>> methods = KeyedListDiffUtils.commute(
            diff1.methods,
            diff2.methods,
            MethodDiffUtils::commute,
            MethodDiffUtils::diff
    );
    diffPrime2.methods = methods.first;
    diffPrime1.methods = methods.second;

    return Pair.of(diffPrime2, diffPrime1);
  }

  public static void write(ClassDiff diff, CustomDataOutput out) throws IOException {
    out.writeBoolean(diff == null);

    if (diff == null) {
      return;
    }

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
            MethodNodeHelper::write,
            MethodDiffUtils::write
    );
  }

  public static ClassDiff read(CustomDataInput in) throws IOException {
    if (in.readBoolean()) {
      return null;
    }

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
            stream -> MethodNodeHelper.read(in),
            stream -> MethodDiffUtils.read(in)
    );
    return diff;
  }

  public static int distance(ClassDiff diff) {
    if (diff == null) {
      return 0;
    }

    return ListDiffUtils.distance(diff.version)
            + ListDiffUtils.distance(diff.access)
            + ListDiffUtils.distance(diff.name)
            + ListDiffUtils.distance(diff.signature)
            + ListDiffUtils.distance(diff.superName)
            + ListDiffUtils.distance(diff.interfaces)
            + ListDiffUtils.distance(diff.sourceFile)
            + ListDiffUtils.distance(diff.sourceDebug)
            + KeyedListDiffUtils.distance(diff.module, ModuleDiffUtils::distance)
            + ListDiffUtils.distance(diff.outerClass)
            + ListDiffUtils.distance(diff.outerMethod)
            + ListDiffUtils.distance(diff.outerMethodDesc)
            + ListDiffUtils.distance(diff.visibleAnnotations)
            + ListDiffUtils.distance(diff.invisibleAnnotations)
            + ListDiffUtils.distance(diff.visibleTypeAnnotations)
            + ListDiffUtils.distance(diff.invisibleTypeAnnotations)
            + ListDiffUtils.distance(diff.innerClasses)
            + ListDiffUtils.distance(diff.nestHostClass)
            + ListDiffUtils.distance(diff.nestMembers)
            + ListDiffUtils.distance(diff.permittedSubclasses)
            + KeyedListDiffUtils.distance(diff.recordComponents, RecordComponentDiffUtils::distance)
            + KeyedListDiffUtils.distance(diff.fields, FieldDiffUtils::distance)
            + KeyedListDiffUtils.distance(diff.methods, MethodDiffUtils::distance);
  }
}
