package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;

import java.util.Objects;

public class ClassDiffer implements IDiffer<ClassNode, ClassPatch> {
  @Override
  public boolean canDiff(ClassNode oldValue, ClassNode newValue) {
    return Objects.equals(oldValue.name, newValue.name);
  }

  public ClassPatch diff(ClassNode classA, ClassNode classB) {
    var diff = new ClassPatch();

    var integerDiffer = new ValueDiffer<Integer>();
    var stringDiffer = new ValueDiffer<String>();
    var stringsDiffer = new ListDiffer<String, ValuePatch<String>, String>(stringDiffer, new ValueKeyProvider<>());

    diff.version = integerDiffer.diff(classA.version, classB.version);
    diff.access = integerDiffer.diff(classA.access, classB.access);
    diff.name = stringDiffer.diff(classA.name, classB.name);
    diff.signature = stringDiffer.diff(classA.signature, classB.signature);
    diff.superName = stringDiffer.diff(classA.superName, classB.superName);
    diff.interfaces = stringsDiffer.diff(classA.interfaces, classB.interfaces);
    diff.sourceFile = stringDiffer.diff(classA.sourceFile, classB.sourceFile);

    // TODO: module

    diff.sourceDebug = stringDiffer.diff(classA.sourceDebug, classB.sourceDebug);
    diff.outerClass = stringDiffer.diff(classA.outerClass, classB.outerClass);
    diff.outerMethod = stringDiffer.diff(classA.outerMethod, classB.outerMethod);
    diff.outerMethodDesc = stringDiffer.diff(classA.outerMethodDesc, classB.outerMethodDesc);

    // TODO: annotations
    // TODO: attributes

    diff.innerClasses = new ListDiffer<InnerClassNode, InnerClassPatch, String>(
            new InnerClassDiffer(),
            new InnerClassKeyProvider()
    ).diff(classA.innerClasses, classB.innerClasses);
    diff.nestHostClass = stringDiffer.diff(classA.nestHostClass, classB.nestHostClass);
    diff.nestMembers = stringsDiffer.diff(classA.nestMembers, classB.nestMembers);
    diff.permittedSubclasses = stringsDiffer.diff(classA.permittedSubclasses, classB.permittedSubclasses);
    // TODO: record components
    // TODO: fields
    // TODO: methods

    return diff;
  }
}
