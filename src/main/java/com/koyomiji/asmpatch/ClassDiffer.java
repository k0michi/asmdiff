package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ClassDiffer implements IDiffer<ClassNode, ClassPatch> {
  public ClassPatch diff(ClassNode classA, ClassNode classB) {
    var diff = new ClassPatch();

    var integerDiffer = new ValueDiffer<Integer>();
    var stringDiffer = new ValueDiffer<String>();
    var stringsDiffer = new ListDiffer<>(stringDiffer);

    diff.version = integerDiffer.diff(classA.version, classB.version);
    diff.access = integerDiffer.diff(classA.access, classB.access);
    diff.name = stringDiffer.diff(classA.name, classB.name);
    diff.signature = stringDiffer.diff(classA.signature, classB.signature);
    diff.superName = stringDiffer.diff(classA.superName, classB.superName);
    diff.interfaces = stringsDiffer.diff(classA.interfaces, classB.interfaces);
    diff.sourceFile = stringDiffer.diff(classA.sourceFile, classB.sourceFile);
    diff.module = new NullableDiffer<>(new ModuleDiffer()).diff(classA.module, classB.module);
    diff.sourceDebug = stringDiffer.diff(classA.sourceDebug, classB.sourceDebug);
    diff.outerClass = stringDiffer.diff(classA.outerClass, classB.outerClass);
    diff.outerMethod = stringDiffer.diff(classA.outerMethod, classB.outerMethod);
    diff.outerMethodDesc = stringDiffer.diff(classA.outerMethodDesc, classB.outerMethodDesc);

    var annotationsDiffer = new ListDiffer<>(
            new AnnotationDiffer()
    );

    diff.visibleAnnotations = annotationsDiffer.diff(
            ListHelper.orEmpty(classA.visibleAnnotations),
            ListHelper.orEmpty(classB.visibleAnnotations)
    );
    diff.invisibleAnnotations = annotationsDiffer.diff(
            ListHelper.orEmpty(classA.invisibleAnnotations),
            ListHelper.orEmpty(classB.invisibleAnnotations)
    );

    var typeAnnotationsDiffer = new ListDiffer<>(
            new TypeAnnotationDiffer()
    );

    diff.visibleTypeAnnotations = typeAnnotationsDiffer.diff(
            ListHelper.orEmpty(classA.visibleTypeAnnotations),
            ListHelper.orEmpty(classB.visibleTypeAnnotations)
    );
    diff.invisibleTypeAnnotations = typeAnnotationsDiffer.diff(
            ListHelper.orEmpty(classA.invisibleTypeAnnotations),
            ListHelper.orEmpty(classB.invisibleTypeAnnotations)
    );

    // TODO: attributes

    diff.innerClasses = new ListDiffer<>(
            new InnerClassDiffer()
    ).diff(classA.innerClasses, classB.innerClasses);
    diff.nestHostClass = stringDiffer.diff(classA.nestHostClass, classB.nestHostClass);
    diff.nestMembers = stringsDiffer.diff(
            ListHelper.orEmpty(classA.nestMembers),
            ListHelper.orEmpty(classB.nestMembers)
    );
    diff.permittedSubclasses = stringsDiffer.diff(
            ListHelper.orEmpty(classA.permittedSubclasses),
            ListHelper.orEmpty(classB.permittedSubclasses)
    );
    // TODO: record components
    var fieldsDiffer = new ListDiffer<>(
            new FieldDiffer()
    );
    diff.fields = fieldsDiffer.diff(classA.fields, classB.fields);
    // TODO: methods

    return diff;
  }

  @Override
  public int distance(ClassNode oldValue, ClassNode newValue) {
    // TODO
    return 0;
  }

  @Override
  public boolean canMatch(ClassNode oldValue, ClassNode newValue) {
    return Objects.equals(oldValue.name, newValue.name);
  }
}
