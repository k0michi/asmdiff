package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.CustomDataInput;
import com.koyomiji.asmweaver.io.CustomDataOutput;
import com.koyomiji.asmweaver.util.tuple.Pair;
import org.objectweb.asm.tree.*;

import java.io.DataInput;
import java.io.IOException;

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

  public static ModuleDiff invert(ModuleDiff diff) {
    ModuleDiff invertedDiff = new ModuleDiff();
    invertedDiff.name = ListDiffUtils.invert(diff.name);
    invertedDiff.access = ListDiffUtils.invert(diff.access);
    invertedDiff.version = ListDiffUtils.invert(diff.version);
    invertedDiff.mainClass = ListDiffUtils.invert(diff.mainClass);
    invertedDiff.packages = ListDiffUtils.invert(diff.packages);
    invertedDiff.requires = ListDiffUtils.invert(diff.requires);
    invertedDiff.exports = ListDiffUtils.invert(diff.exports);
    invertedDiff.opens = ListDiffUtils.invert(diff.opens);
    invertedDiff.uses = ListDiffUtils.invert(diff.uses);
    invertedDiff.provides = ListDiffUtils.invert(diff.provides);
    return invertedDiff;
  }

  public static ModuleDiff compose(ModuleDiff diff1, ModuleDiff diff2) {
    ModuleDiff composedDiff = new ModuleDiff();
    composedDiff.name = ListDiffUtils.compose(diff1.name, diff2.name, String::equals);
    composedDiff.access = ListDiffUtils.compose(diff1.access, diff2.access, Integer::equals);
    composedDiff.version = ListDiffUtils.compose(diff1.version, diff2.version, String::equals);
    composedDiff.mainClass = ListDiffUtils.compose(diff1.mainClass, diff2.mainClass, String::equals);
    composedDiff.packages = ListDiffUtils.compose(diff1.packages, diff2.packages, String::equals);
    composedDiff.requires = ListDiffUtils.compose(diff1.requires, diff2.requires, ModuleRequireNodeHelper::equals);
    composedDiff.exports = ListDiffUtils.compose(diff1.exports, diff2.exports, ModuleExportNodeHelper::equals);
    composedDiff.opens = ListDiffUtils.compose(diff1.opens, diff2.opens, ModuleOpenNodeHelper::equals);
    composedDiff.uses = ListDiffUtils.compose(diff1.uses, diff2.uses, String::equals);
    composedDiff.provides = ListDiffUtils.compose(diff1.provides, diff2.provides, ModuleProvideNodeHelper::equals);
    return composedDiff;
  }

  public static Pair<ModuleDiff, ModuleDiff> commute(ModuleDiff diff1, ModuleDiff diff2) throws ConflictException {
    ModuleDiff diff2Prime = new ModuleDiff();
    ModuleDiff diff1Prime = new ModuleDiff();

    Pair<ListDiff<String>, ListDiff<String>> name = ListDiffUtils.commute(diff1.name, diff2.name, String::equals);
    diff2Prime.name = name.first;
    diff1Prime.name = name.second;

    Pair<ListDiff<Integer>, ListDiff<Integer>> access = ListDiffUtils.commute(diff1.access, diff2.access, Integer::equals);
    diff2Prime.access = access.first;
    diff1Prime.access = access.second;

    Pair<ListDiff<String>, ListDiff<String>> version = ListDiffUtils.commute(diff1.version, diff2.version, String::equals);
    diff2Prime.version = version.first;
    diff1Prime.version = version.second;

    Pair<ListDiff<String>, ListDiff<String>> mainClass = ListDiffUtils.commute(diff1.mainClass, diff2.mainClass, String::equals);
    diff2Prime.mainClass = mainClass.first;
    diff1Prime.mainClass = mainClass.second;

    Pair<ListDiff<String>, ListDiff<String>> packages = ListDiffUtils.commute(diff1.packages, diff2.packages, String::equals);
    diff2Prime.packages = packages.first;
    diff1Prime.packages = packages.second;

    Pair<ListDiff<ModuleRequireNode>, ListDiff<ModuleRequireNode>> requires = ListDiffUtils.commute(diff1.requires, diff2.requires, ModuleRequireNodeHelper::equals);
    diff2Prime.requires = requires.first;
    diff1Prime.requires = requires.second;

    Pair<ListDiff<ModuleExportNode>, ListDiff<ModuleExportNode>> exports = ListDiffUtils.commute(diff1.exports, diff2.exports, ModuleExportNodeHelper::equals);
    diff2Prime.exports = exports.first;
    diff1Prime.exports = exports.second;

    Pair<ListDiff<ModuleOpenNode>, ListDiff<ModuleOpenNode>> opens = ListDiffUtils.commute(diff1.opens, diff2.opens, ModuleOpenNodeHelper::equals);
    diff2Prime.opens = opens.first;
    diff1Prime.opens = opens.second;

    Pair<ListDiff<String>, ListDiff<String>> uses = ListDiffUtils.commute(diff1.uses, diff2.uses, String::equals);
    diff2Prime.uses = uses.first;
    diff1Prime.uses = uses.second;

    Pair<ListDiff<ModuleProvideNode>, ListDiff<ModuleProvideNode>> provides = ListDiffUtils.commute(diff1.provides, diff2.provides, ModuleProvideNodeHelper::equals);
    diff2Prime.provides = provides.first;
    diff1Prime.provides = provides.second;

    return Pair.of(diff2Prime, diff1Prime);
  }

  public static void write(ModuleDiff diff, CustomDataOutput out) throws IOException {
    ListDiffUtils.write(diff.name, out, (e, s) -> s.writeUTF(e));
    ListDiffUtils.write(diff.access, out, (e, s) -> s.writeInt(e));
    ListDiffUtils.write(diff.version, out, (e, s) -> s.writeUTF(e));
    ListDiffUtils.write(diff.mainClass, out, (e, s) -> s.writeUTF(e));
    ListDiffUtils.write(diff.packages, out, (e, s) -> s.writeUTF(e));
    ListDiffUtils.write(diff.requires, out, ModuleRequireNodeHelper::write);
    ListDiffUtils.write(diff.exports, out, ModuleExportNodeHelper::write);
    ListDiffUtils.write(diff.opens, out, ModuleOpenNodeHelper::write);
    ListDiffUtils.write(diff.uses, out, (e, s) -> s.writeUTF(e));
    ListDiffUtils.write(diff.provides, out, ModuleProvideNodeHelper::write);
  }

  public static ModuleDiff read(CustomDataInput in) throws IOException {
    ModuleDiff diff = new ModuleDiff();
    diff.name = ListDiffUtils.read(in, DataInput::readUTF);
    diff.access = ListDiffUtils.read(in, CustomDataInput::readInt);
    diff.version = ListDiffUtils.read(in, DataInput::readUTF);
    diff.mainClass = ListDiffUtils.read(in, DataInput::readUTF);
    diff.packages = ListDiffUtils.read(in, DataInput::readUTF);
    diff.requires = ListDiffUtils.read(in, ModuleRequireNodeHelper::read);
    diff.exports = ListDiffUtils.read(in, ModuleExportNodeHelper::read);
    diff.opens = ListDiffUtils.read(in, ModuleOpenNodeHelper::read);
    diff.uses = ListDiffUtils.read(in, DataInput::readUTF);
    diff.provides = ListDiffUtils.read(in, ModuleProvideNodeHelper::read);
    return diff;
  }
}
