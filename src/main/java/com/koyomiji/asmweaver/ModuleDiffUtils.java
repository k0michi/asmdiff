package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.ModuleNode;

public class ModuleDiffUtils {
  public static ModuleDiff diff(ModuleNode node1, ModuleNode node2) {
    ModuleDiff diff = new ModuleDiff();
    diff.name = ListDiffUtils.diff(
            ListHelper.ofNonNullable(node1.name),
            ListHelper.ofNonNullable(node2.name),
            String::equals
    );
    diff.access = ListDiffUtils.diff(
            ListHelper.ofNonNullable(node1.access),
            ListHelper.ofNonNullable(node2.access),
            Integer::equals
    );
    diff.version = ListDiffUtils.diff(
            ListHelper.ofNullable(node1.version),
            ListHelper.ofNullable(node2.version),
            String::equals
    );
    diff.mainClass = ListDiffUtils.diff(
            ListHelper.ofNullable(node1.mainClass),
            ListHelper.ofNullable(node2.mainClass),
            String::equals
    );
    diff.packages = ListDiffUtils.diff(
            ListHelper.nullToEmpty(node1.packages),
            ListHelper.nullToEmpty(node2.packages),
            String::equals
    );
    diff.requires = ListDiffUtils.diff(
            ListHelper.nullToEmpty(node1.requires),
            ListHelper.nullToEmpty(node2.requires),
            ModuleRequireNodeHelper::equals
    );
    diff.exports = ListDiffUtils.diff(
            ListHelper.nullToEmpty(node1.exports),
            ListHelper.nullToEmpty(node2.exports),
            ModuleExportNodeHelper::equals
    );
    diff.opens = ListDiffUtils.diff(
            ListHelper.nullToEmpty(node1.opens),
            ListHelper.nullToEmpty(node2.opens),
            ModuleOpenNodeHelper::equals
    );
    diff.uses = ListDiffUtils.diff(
            ListHelper.nullToEmpty(node1.uses),
            ListHelper.nullToEmpty(node2.uses),
            String::equals
    );
    diff.provides = ListDiffUtils.diff(
            ListHelper.nullToEmpty(node1.provides),
            ListHelper.nullToEmpty(node2.provides),
            ModuleProvideNodeHelper::equals
    );
    return diff;
  }

  public static ModuleNode patch(ModuleNode node, ModuleDiff diff) {
    String name = ListDiffUtils.patchNonNullableValue(node.name, diff.name);
    int access = ListDiffUtils.patchNonNullableValue(node.access, diff.access);
    String version = ListDiffUtils.patchNullableValue(node.version, diff.version);
    ModuleNode patchedNode = new ModuleNode(name, access, version);
    patchedNode.mainClass = ListDiffUtils.patchNullableValue(node.mainClass, diff.mainClass);
    patchedNode.packages = ListDiffUtils.patch(
            ListHelper.nullToEmpty(node.packages),
            diff.packages
    );
    patchedNode.requires = ListDiffUtils.patch(
            ListHelper.nullToEmpty(node.requires),
            diff.requires
    );
    patchedNode.exports = ListDiffUtils.patch(
            ListHelper.nullToEmpty(node.exports),
            diff.exports
    );
    patchedNode.opens = ListDiffUtils.patch(
            ListHelper.nullToEmpty(node.opens),
            diff.opens
    );
    patchedNode.uses = ListDiffUtils.patch(
            ListHelper.nullToEmpty(node.uses),
            diff.uses
    );
    patchedNode.provides = ListDiffUtils.patch(
            ListHelper.nullToEmpty(node.provides),
            diff.provides
    );
    return patchedNode;
  }
}
