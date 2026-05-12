package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.ClassNode;

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
    diff.module = KeyedListDiffUtils.diff(ListHelper.ofNullable(class1.module), ListHelper.ofNullable(class2.module), (m) -> 0, ModuleDiffUtils::diff);
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
}
