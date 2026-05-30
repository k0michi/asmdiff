package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.analysis.DefUse;
import com.koyomiji.asmweaver.analysis.DefUseChainAnalyzer;
import com.koyomiji.asmweaver.io.CustomDataInput;
import com.koyomiji.asmweaver.io.CustomDataOutput;
import com.koyomiji.asmweaver.util.UnionFind;
import com.koyomiji.asmweaver.util.tuple.Pair;
import com.koyomiji.asmweaver.util.tuple.Triplet;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.io.DataInput;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MethodDiffUtils {
  public static MethodDiff diff(MethodNode node1, MethodNode node2) {
    MethodDiff diff = new MethodDiff();
    diff.access = ListDiffUtils.diff(ListHelper.ofNullable(node1.access), ListHelper.ofNullable(node2.access), Integer::equals);
    diff.name = ListDiffUtils.diff(ListHelper.ofNullable(node1.name), ListHelper.ofNullable(node2.name), String::equals);
    diff.desc = ListDiffUtils.diff(ListHelper.ofNullable(node1.desc), ListHelper.ofNullable(node2.desc), String::equals);
    diff.signature = ListDiffUtils.diff(ListHelper.ofNullable(node1.signature), ListHelper.ofNullable(node2.signature), String::equals);
    diff.exceptions = ListDiffUtils.diff(node1.exceptions, node2.exceptions, String::equals);
    diff.parameters = ListDiffUtils.diff(ListHelper.nullToEmpty(node1.parameters), ListHelper.nullToEmpty(node2.parameters), ParameterNodeHelper::equals);
    diff.visibleAnnotations = ListDiffUtils.diff(
            ListHelper.nullToEmpty(node1.visibleAnnotations),
            ListHelper.nullToEmpty(node2.visibleAnnotations),
            AnnotationNodeHelper::equals
    );
    diff.invisibleAnnotations = ListDiffUtils.diff(
            ListHelper.nullToEmpty(node1.invisibleAnnotations),
            ListHelper.nullToEmpty(node2.invisibleAnnotations),
            AnnotationNodeHelper::equals
    );
    diff.visibleTypeAnnotations = ListDiffUtils.diff(
            ListHelper.nullToEmpty(node1.visibleTypeAnnotations),
            ListHelper.nullToEmpty(node2.visibleTypeAnnotations),
            AnnotationNodeHelper::equals
    );
    diff.invisibleTypeAnnotations = ListDiffUtils.diff(
            ListHelper.nullToEmpty(node1.invisibleTypeAnnotations),
            ListHelper.nullToEmpty(node2.invisibleTypeAnnotations),
            AnnotationNodeHelper::equals
    );
    // attrs
    diff.annotationDefault = ListDiffUtils.diff(ListHelper.ofNullable(node1.annotationDefault), ListHelper.ofNullable(node2.annotationDefault), Object::equals);
    diff.visibleAnnotableParameterCount = ListDiffUtils.diff(ListHelper.ofNullable(node1.visibleAnnotableParameterCount), ListHelper.ofNullable(node2.visibleAnnotableParameterCount), Integer::equals);

    {
      List<List<AnnotationNode>> vpa1 = normalizeAnnotations(node1.visibleParameterAnnotations);
      List<List<AnnotationNode>> vpa2 = normalizeAnnotations(node2.visibleParameterAnnotations);
      diff.visibleParameterAnnotations = KeyedListDiffUtils.diffIndexed(
              vpa1,
              vpa2,
              (l1, l2) -> ListDiffUtils.diff(l1, l2, AnnotationNodeHelper::equals)
      );
    }

    diff.invisibleAnnotableParameterCount = ListDiffUtils.diff(ListHelper.ofNullable(node1.invisibleAnnotableParameterCount), ListHelper.ofNullable(node2.invisibleAnnotableParameterCount), Integer::equals);

    {
      List<List<AnnotationNode>> ipa1 = normalizeAnnotations(node1.invisibleParameterAnnotations);
      List<List<AnnotationNode>> ipa2 = normalizeAnnotations(node2.invisibleParameterAnnotations);
      diff.invisibleParameterAnnotations = KeyedListDiffUtils.diffIndexed(
              ipa1,
              ipa2,
              (l1, l2) -> ListDiffUtils.diff(l1, l2, AnnotationNodeHelper::equals)
      );
    }

    DefUseChainAnalyzer analyzer1 = new DefUseChainAnalyzer();
    DefUseChainAnalyzer analyzer2 = new DefUseChainAnalyzer();
    Map<AbstractInsnNode, Integer> duChains1 = new HashMap<>();
    Map<AbstractInsnNode, Integer> duChains2 = new HashMap<>();

    try {
      UnionFind<DefUse> uf1 = analyzer1.analyze("", node1);
      var groups1 = uf1.getGroups();

      for (int i = 0; i < groups1.size(); i++) {
        for (DefUse du : groups1.get(i)) {
          duChains1.put(du.insn, i);
        }
      }
    } catch (AnalyzerException e) {
    }

    try {
      UnionFind<DefUse> uf2 = analyzer2.analyze("", node2);
      var groups2 = uf2.getGroups();

      for (int i = 0; i < groups2.size(); i++) {
        for (DefUse du : groups2.get(i)) {
          duChains2.put(du.insn, i);
        }
      }
    } catch (AnalyzerException e) {
    }

    Pair<List<AbstractInsnNode>, List<LineNumberNode>> split1 = InsnListHelper.splitLineNumbers(new InsnListListAdapter(node1.instructions));
    Pair<List<AbstractInsnNode>, List<LineNumberNode>> split2 = InsnListHelper.splitLineNumbers(new InsnListListAdapter(node2.instructions));

    diff.instructions = InsnListDiffUtils.diff(
            split1.first,
            duChains1::get,
            split2.first,
            duChains2::get
    );

    Map<LabelNode, IndexedLabelNode> labelMap = InsnListDiffUtils.extractIndexedLabelMap(
            split1.first,
            split2.first,
            diff.instructions
    );

    if (diff.instructions != null) {
      diff.instructions.operations = ListHelper.map(diff.instructions.operations, op -> InsnListDiffUtils.mapLabels(op, labelMap::get));
    }

    diff.lineNumbers = ListDiffUtils.diff(
            ListHelper.map(
                    split1.second,
                    ln -> (LineNumberNode) AbstractInsnNodeHelper.mapLabelTargets(ln, labelMap::get)
            ),
            ListHelper.map(
                    split2.second,
                    ln -> (LineNumberNode) AbstractInsnNodeHelper.mapLabelTargets(ln, labelMap::get)
            ),
            AbstractInsnNodeHelper::equals
    );

    diff.tryCatchBlocks = ListDiffUtils.diff(
            ListHelper.map(node1.tryCatchBlocks, tcb -> TryCatchBlockNodeHelper.mapLabels(tcb, labelMap::get)),
            ListHelper.map(node2.tryCatchBlocks, tcb -> TryCatchBlockNodeHelper.mapLabels(tcb, labelMap::get)),
            TryCatchBlockNodeHelper::equals
    );

    diff.maxStack = ListDiffUtils.diffNonNullableValue(
            node1.maxStack,
            node2.maxStack,
            Integer::equals
    );

    diff.maxLocals = ListDiffUtils.diffNonNullableValue(
            node1.maxLocals,
            node2.maxLocals,
            Integer::equals
    );

    diff.localVariables = ListDiffUtils.diff(
            ListHelper.map(ListHelper.nullToEmpty(node1.localVariables), lv -> LocalVariableNodeHelper.mapLabels(lv, labelMap::get)),
            ListHelper.map(ListHelper.nullToEmpty(node2.localVariables), lv -> LocalVariableNodeHelper.mapLabels(lv, labelMap::get)),
            LocalVariableNodeHelper::equals
    );

    diff.visibleLocalVariableAnnotations = ListDiffUtils.diff(
            ListHelper.map(ListHelper.nullToEmpty(node1.visibleLocalVariableAnnotations), lva -> AnnotationNodeHelper.mapLabels(lva, labelMap::get)),
            ListHelper.map(ListHelper.nullToEmpty(node2.visibleLocalVariableAnnotations), lva -> AnnotationNodeHelper.mapLabels(lva, labelMap::get)),
            AnnotationNodeHelper::equals
    );

    diff.invisibleLocalVariableAnnotations = ListDiffUtils.diff(
            ListHelper.map(ListHelper.nullToEmpty(node1.invisibleLocalVariableAnnotations), lva -> AnnotationNodeHelper.mapLabels(lva, labelMap::get)),
            ListHelper.map(ListHelper.nullToEmpty(node2.invisibleLocalVariableAnnotations), lva -> AnnotationNodeHelper.mapLabels(lva, labelMap::get)),
            AnnotationNodeHelper::equals
    );

    if (diff.access == null
            && diff.name == null
            && diff.desc == null
            && diff.signature == null
            && diff.exceptions == null
            && diff.parameters == null
            && diff.visibleAnnotations == null
            && diff.invisibleAnnotations == null
            && diff.visibleTypeAnnotations == null
            && diff.invisibleTypeAnnotations == null
            && diff.annotationDefault == null
            && diff.visibleAnnotableParameterCount == null
            && diff.visibleParameterAnnotations == null
            && diff.invisibleAnnotableParameterCount == null
            && diff.invisibleParameterAnnotations == null
            && diff.instructions == null
            && diff.lineNumbers == null
            && diff.tryCatchBlocks == null
            && diff.maxStack == null
            && diff.maxLocals == null
            && diff.localVariables == null
            && diff.visibleLocalVariableAnnotations == null
            && diff.invisibleLocalVariableAnnotations == null) {
      return null;
    }

    return diff;
  }

  public static MethodNode patch(MethodNode node, MethodDiff diff) {
    if (diff == null) {
      return node;
    }

    MethodNode patched = new MethodNode();
    patched.access = ListDiffUtils.patchNonNullableValue(node.access, diff.access);
    patched.name = ListDiffUtils.patchNonNullableValue(node.name, diff.name);
    patched.desc = ListDiffUtils.patchNonNullableValue(node.desc, diff.desc);
    patched.signature = ListDiffUtils.patchNullableValue(node.signature, diff.signature);
    patched.exceptions = ListDiffUtils.patch(node.exceptions, diff.exceptions);
    patched.parameters = ListDiffUtils.patch(ListHelper.nullToEmpty(node.parameters), diff.parameters);
    patched.visibleAnnotations = ListDiffUtils.patch(
            ListHelper.nullToEmpty(node.visibleAnnotations),
            diff.visibleAnnotations
    );
    patched.invisibleAnnotations = ListDiffUtils.patch(
            ListHelper.nullToEmpty(node.invisibleAnnotations),
            diff.invisibleAnnotations
    );
    patched.visibleTypeAnnotations = ListDiffUtils.patch(
            ListHelper.nullToEmpty(node.visibleTypeAnnotations),
            diff.visibleTypeAnnotations
    );
    patched.invisibleTypeAnnotations = ListDiffUtils.patch(
            ListHelper.nullToEmpty(node.invisibleTypeAnnotations),
            diff.invisibleTypeAnnotations
    );
    // attrs
    patched.annotationDefault = ListDiffUtils.patchNullableValue(node.annotationDefault, diff.annotationDefault);
    patched.visibleAnnotableParameterCount = ListDiffUtils.patchNonNullableValue(node.visibleAnnotableParameterCount, diff.visibleAnnotableParameterCount);
    patched.visibleParameterAnnotations = KeyedListDiffUtils.patch(
            normalizeAnnotations(node.visibleParameterAnnotations),
            diff.visibleParameterAnnotations,
            ListDiffUtils::patch
    ).toArray(new List[0]);
    patched.invisibleAnnotableParameterCount = ListDiffUtils.patchNonNullableValue(node.invisibleAnnotableParameterCount, diff.invisibleAnnotableParameterCount);
    patched.invisibleParameterAnnotations = KeyedListDiffUtils.patch(
            normalizeAnnotations(node.invisibleParameterAnnotations),
            diff.invisibleParameterAnnotations,
            ListDiffUtils::patch
    ).toArray(new List[0]);

    Pair<List<AbstractInsnNode>, List<LineNumberNode>> split = InsnListHelper.splitLineNumbers(new InsnListListAdapter(node.instructions));

    HashMap<LabelNode, IndexedLabelNode> labelMap = new HashMap<>();
    List<AbstractInsnNode> instructions = InsnListDiffUtils.patch(
            split.first,
            diff.instructions,
            labelMap
    );

    HashMap<LabelNode, LabelNode> toPlain = new HashMap<>();

    for (IndexedLabelNode iln : labelMap.values()) {
      toPlain.put(iln, new LabelNode());
    }

    List<LineNumberNode> lineNumbers =
            ListDiffUtils.patch(
                    ListHelper.map(
                            split.second,
                            ln -> (LineNumberNode) AbstractInsnNodeHelper.mapLabelTargets(ln, labelMap::get)
                    ),
                    diff.lineNumbers
            );

    patched.instructions = InsnListHelper.fromList(InsnListHelper.mergeLineNumbers(
            instructions,
            lineNumbers
    ));

    InsnListHelper.mapInPlace(patched.instructions, insn -> AbstractInsnNodeHelper.mapLabelTargets(insn, toPlain::get));

    patched.tryCatchBlocks =
            ListDiffUtils.patch(
                    ListHelper.map(
                            node.tryCatchBlocks,
                            tcb -> TryCatchBlockNodeHelper.mapLabels(tcb, labelMap::get)
                    ),
                    diff.tryCatchBlocks
            );

    patched.tryCatchBlocks = ListHelper.map(
            patched.tryCatchBlocks,
            tcb -> TryCatchBlockNodeHelper.mapLabels(tcb, toPlain::get)
    );

    patched.maxStack = ListDiffUtils.patchNonNullableValue(node.maxStack, diff.maxStack);
    patched.maxLocals = ListDiffUtils.patchNonNullableValue(node.maxLocals, diff.maxLocals);

    patched.localVariables =
            ListDiffUtils.patch(
                    ListHelper.map(
                            ListHelper.nullToEmpty(node.localVariables),
                            lv -> LocalVariableNodeHelper.mapLabels(lv, labelMap::get)
                    ),
                    diff.localVariables
            );

    patched.localVariables = ListHelper.map(
            patched.localVariables,
            lv -> LocalVariableNodeHelper.mapLabels(lv, toPlain::get)
    );

    patched.visibleLocalVariableAnnotations = ListDiffUtils.patch(
            ListHelper.map(
                    ListHelper.nullToEmpty(node.visibleLocalVariableAnnotations),
                    lva -> AnnotationNodeHelper.mapLabels(lva, labelMap::get)
            ),
            diff.visibleLocalVariableAnnotations
    );

    patched.visibleLocalVariableAnnotations = ListHelper.map(
            patched.visibleLocalVariableAnnotations,
            lva -> AnnotationNodeHelper.mapLabels(lva, toPlain::get)
    );

    patched.invisibleLocalVariableAnnotations = ListDiffUtils.patch(
            ListHelper.map(
                    ListHelper.nullToEmpty(node.invisibleLocalVariableAnnotations),
                    lva -> AnnotationNodeHelper.mapLabels(lva, labelMap::get)
            ),
            diff.invisibleLocalVariableAnnotations
    );

    patched.invisibleLocalVariableAnnotations = ListHelper.map(
            patched.invisibleLocalVariableAnnotations,
            lva -> AnnotationNodeHelper.mapLabels(lva, toPlain::get)
    );

    return patched;
  }

  private static List<List<AnnotationNode>> normalizeAnnotations(List<AnnotationNode>[] annotations) {
    return ListHelper.map(
            ListHelper.ofNullableArray(annotations),
            ListHelper::nullToEmpty
    );
  }

  public static MethodDiff invert(MethodDiff diff) {
    if (diff == null) {
      return null;
    }

    MethodDiff inverted = new MethodDiff();
    inverted.access = ListDiffUtils.invert(diff.access);
    inverted.name = ListDiffUtils.invert(diff.name);
    inverted.desc = ListDiffUtils.invert(diff.desc);
    inverted.signature = ListDiffUtils.invert(diff.signature);
    inverted.exceptions = ListDiffUtils.invert(diff.exceptions);
    inverted.parameters = ListDiffUtils.invert(diff.parameters);
    inverted.visibleAnnotations = ListDiffUtils.invert(diff.visibleAnnotations);
    inverted.invisibleAnnotations = ListDiffUtils.invert(diff.invisibleAnnotations);
    inverted.visibleTypeAnnotations = ListDiffUtils.invert(diff.visibleTypeAnnotations);
    inverted.invisibleTypeAnnotations = ListDiffUtils.invert(diff.invisibleTypeAnnotations);

    inverted.annotationDefault = ListDiffUtils.invert(diff.annotationDefault);
    inverted.visibleAnnotableParameterCount = ListDiffUtils.invert(diff.visibleAnnotableParameterCount);
    inverted.visibleParameterAnnotations = KeyedListDiffUtils.invert(diff.visibleParameterAnnotations, ListDiffUtils::invert);
    inverted.invisibleAnnotableParameterCount = ListDiffUtils.invert(diff.invisibleAnnotableParameterCount);
    inverted.invisibleParameterAnnotations = KeyedListDiffUtils.invert(diff.invisibleParameterAnnotations, ListDiffUtils::invert);
    inverted.instructions = InsnListDiffUtils.invert(diff.instructions);
    inverted.lineNumbers = ListDiffUtils.invert(diff.lineNumbers);
    inverted.tryCatchBlocks = ListDiffUtils.invert(diff.tryCatchBlocks);
    inverted.maxStack = ListDiffUtils.invert(diff.maxStack);
    inverted.maxLocals = ListDiffUtils.invert(diff.maxLocals);
    inverted.localVariables = ListDiffUtils.invert(diff.localVariables);
    inverted.visibleLocalVariableAnnotations = ListDiffUtils.invert(diff.visibleLocalVariableAnnotations);
    inverted.invisibleLocalVariableAnnotations = ListDiffUtils.invert(diff.invisibleLocalVariableAnnotations);
    return inverted;
  }

  public static MethodDiff compose(MethodDiff diff1, MethodDiff diff2) {
    if (diff1 == null) {
      return diff2;
    }

    if (diff2 == null) {
      return diff1;
    }

    MethodDiff composed = new MethodDiff();
    composed.access = ListDiffUtils.compose(diff1.access, diff2.access, Integer::equals);
    composed.name = ListDiffUtils.compose(diff1.name, diff2.name, String::equals);
    composed.desc = ListDiffUtils.compose(diff1.desc, diff2.desc, String::equals);
    composed.signature = ListDiffUtils.compose(diff1.signature, diff2.signature, String::equals);
    composed.exceptions = ListDiffUtils.compose(diff1.exceptions, diff2.exceptions, String::equals);
    composed.parameters = ListDiffUtils.compose(diff1.parameters, diff2.parameters, ParameterNodeHelper::equals);
    composed.visibleAnnotations = ListDiffUtils.compose(diff1.visibleAnnotations, diff2.visibleAnnotations, AnnotationNodeHelper::equals);
    composed.invisibleAnnotations = ListDiffUtils.compose(diff1.invisibleAnnotations, diff2.invisibleAnnotations, AnnotationNodeHelper::equals);
    composed.visibleTypeAnnotations = ListDiffUtils.compose(diff1.visibleTypeAnnotations, diff2.visibleTypeAnnotations, AnnotationNodeHelper::equals);
    composed.invisibleTypeAnnotations = ListDiffUtils.compose(diff1.invisibleTypeAnnotations, diff2.invisibleTypeAnnotations, AnnotationNodeHelper::equals);
    composed.annotationDefault = ListDiffUtils.compose(diff1.annotationDefault, diff2.annotationDefault, Object::equals);
    composed.visibleAnnotableParameterCount = ListDiffUtils.compose(diff1.visibleAnnotableParameterCount, diff2.visibleAnnotableParameterCount, Integer::equals);
    composed.visibleParameterAnnotations = KeyedListDiffUtils.compose(
            diff1.visibleParameterAnnotations,
            diff2.visibleParameterAnnotations,
            (l1, l2) -> ListDiffUtils.compose(l1, l2, AnnotationNodeHelper::equals),
            ListDiffUtils::patch,
            ListDiffUtils::invert
    );
    composed.invisibleAnnotableParameterCount = ListDiffUtils.compose(diff1.invisibleAnnotableParameterCount, diff2.invisibleAnnotableParameterCount, Integer::equals);
    composed.invisibleParameterAnnotations = KeyedListDiffUtils.compose(
            diff1.invisibleParameterAnnotations,
            diff2.invisibleParameterAnnotations,
            (l1, l2) -> ListDiffUtils.compose(l1, l2, AnnotationNodeHelper::equals),
            ListDiffUtils::patch,
            ListDiffUtils::invert
    );
    composed.instructions = InsnListDiffUtils.compose(
            diff1.instructions,
            diff2.instructions
    );
    composed.lineNumbers = ListDiffUtils.compose(
            diff1.lineNumbers,
            diff2.lineNumbers,
            AbstractInsnNodeHelper::equals
    );
    composed.tryCatchBlocks = ListDiffUtils.compose(diff1.tryCatchBlocks, diff2.tryCatchBlocks, TryCatchBlockNodeHelper::equals);
    composed.maxStack = ListDiffUtils.compose(diff1.maxStack, diff2.maxStack, Integer::equals);
    composed.maxLocals = ListDiffUtils.compose(diff1.maxLocals, diff2.maxLocals, Integer::equals);
    composed.localVariables = ListDiffUtils.compose(diff1.localVariables, diff2.localVariables, Object::equals);
    composed.visibleLocalVariableAnnotations = ListDiffUtils.compose(diff1.visibleLocalVariableAnnotations, diff2.visibleLocalVariableAnnotations, LocalVariableAnnotationNode::equals);
    composed.invisibleLocalVariableAnnotations = ListDiffUtils.compose(diff1.invisibleLocalVariableAnnotations, diff2.invisibleLocalVariableAnnotations, LocalVariableAnnotationNode::equals);
    return composed;
  }

  public static Pair<MethodDiff, MethodDiff> commute(MethodDiff diff1, MethodDiff diff2) throws ConflictException {
    if (diff1 == null || diff2 == null) {
      return Pair.of(diff2, diff1);
    }

    MethodDiff diff2Prime = new MethodDiff();
    MethodDiff diff1Prime = new MethodDiff();

    Pair<ListDiff<Integer>, ListDiff<Integer>> access = ListDiffUtils.commute(diff1.access, diff2.access, Integer::equals);
    diff2Prime.access = access.first;
    diff1Prime.access = access.second;

    Pair<ListDiff<String>, ListDiff<String>> name = ListDiffUtils.commute(diff1.name, diff2.name, String::equals);
    diff2Prime.name = name.first;
    diff1Prime.name = name.second;

    Pair<ListDiff<String>, ListDiff<String>> desc = ListDiffUtils.commute(diff1.desc, diff2.desc, String::equals);
    diff2Prime.desc = desc.first;
    diff1Prime.desc = desc.second;

    Pair<ListDiff<String>, ListDiff<String>> signature = ListDiffUtils.commute(diff1.signature, diff2.signature, String::equals);
    diff2Prime.signature = signature.first;
    diff1Prime.signature = signature.second;

    Pair<ListDiff<String>, ListDiff<String>> exceptions = ListDiffUtils.commute(diff1.exceptions, diff2.exceptions, String::equals);
    diff2Prime.exceptions = exceptions.first;
    diff1Prime.exceptions = exceptions.second;

    Pair<ListDiff<ParameterNode>, ListDiff<ParameterNode>> parameters = ListDiffUtils.commute(diff1.parameters, diff2.parameters, ParameterNodeHelper::equals);
    diff2Prime.parameters = parameters.first;
    diff1Prime.parameters = parameters.second;

    Pair<ListDiff<AnnotationNode>, ListDiff<AnnotationNode>> visibleAnnotations = ListDiffUtils.commute(
            diff1.visibleAnnotations,
            diff2.visibleAnnotations,
            AnnotationNodeHelper::equals
    );
    diff2Prime.visibleAnnotations = visibleAnnotations.first;
    diff1Prime.visibleAnnotations = visibleAnnotations.second;

    Pair<ListDiff<AnnotationNode>, ListDiff<AnnotationNode>> invisibleAnnotations = ListDiffUtils.commute(
            diff1.invisibleAnnotations,
            diff2.invisibleAnnotations,
            AnnotationNodeHelper::equals
    );
    diff2Prime.invisibleAnnotations = invisibleAnnotations.first;
    diff1Prime.invisibleAnnotations = invisibleAnnotations.second;

    Pair<ListDiff<TypeAnnotationNode>, ListDiff<TypeAnnotationNode>> visibleTypeAnnotations = ListDiffUtils.commute(
            diff1.visibleTypeAnnotations,
            diff2.visibleTypeAnnotations,
            AnnotationNodeHelper::equals
    );
    diff2Prime.visibleTypeAnnotations = visibleTypeAnnotations.first;
    diff1Prime.visibleTypeAnnotations = visibleTypeAnnotations.second;

    Pair<ListDiff<TypeAnnotationNode>, ListDiff<TypeAnnotationNode>> invisibleTypeAnnotations = ListDiffUtils.commute(
            diff1.invisibleTypeAnnotations,
            diff2.invisibleTypeAnnotations,
            AnnotationNodeHelper::equals
    );
    diff2Prime.invisibleTypeAnnotations = invisibleTypeAnnotations.first;
    diff1Prime.invisibleTypeAnnotations = invisibleTypeAnnotations.second;

    Pair<ListDiff<Object>, ListDiff<Object>> annotationDefault = ListDiffUtils.commute(diff1.annotationDefault, diff2.annotationDefault, Object::equals);
    diff2Prime.annotationDefault = annotationDefault.first;
    diff1Prime.annotationDefault = annotationDefault.second;

    Pair<ListDiff<Integer>, ListDiff<Integer>> visibleAnnotableParameterCount = ListDiffUtils.commute(diff1.visibleAnnotableParameterCount, diff2.visibleAnnotableParameterCount, Integer::equals);
    diff2Prime.visibleAnnotableParameterCount = visibleAnnotableParameterCount.first;
    diff1Prime.visibleAnnotableParameterCount = visibleAnnotableParameterCount.second;

    Pair<KeyedListDiff<Integer, List<AnnotationNode>, ListDiff<AnnotationNode>>, KeyedListDiff<Integer, List<AnnotationNode>, ListDiff<AnnotationNode>>> visibleParameterAnnotations = KeyedListDiffUtils.commute(
            diff1.visibleParameterAnnotations,
            diff2.visibleParameterAnnotations,
            (d1, d2) -> ListDiffUtils.commute(d1, d2, AnnotationNodeHelper::equals),
            (l1, l2) -> ListDiffUtils.diff(l1, l2, AnnotationNodeHelper::equals)
    );
    diff2Prime.visibleParameterAnnotations = visibleParameterAnnotations.first;
    diff1Prime.visibleParameterAnnotations = visibleParameterAnnotations.second;

    Pair<ListDiff<Integer>, ListDiff<Integer>> invisibleAnnotableParameterCount = ListDiffUtils.commute(diff1.invisibleAnnotableParameterCount, diff2.invisibleAnnotableParameterCount, Integer::equals);
    diff2Prime.invisibleAnnotableParameterCount = invisibleAnnotableParameterCount.first;
    diff1Prime.invisibleAnnotableParameterCount = invisibleAnnotableParameterCount.second;

    Pair<KeyedListDiff<Integer, List<AnnotationNode>, ListDiff<AnnotationNode>>, KeyedListDiff<Integer, List<AnnotationNode>, ListDiff<AnnotationNode>>> invisibleParameterAnnotations = KeyedListDiffUtils.commute(
            diff1.invisibleParameterAnnotations,
            diff2.invisibleParameterAnnotations,
            (d1, d2) -> ListDiffUtils.commute(d1, d2, AnnotationNodeHelper::equals),
            (l1, l2) -> ListDiffUtils.diff(l1, l2, AnnotationNodeHelper::equals)
    );
    diff2Prime.invisibleParameterAnnotations = invisibleParameterAnnotations.first;
    diff1Prime.invisibleParameterAnnotations = invisibleParameterAnnotations.second;

    Triplet<InsnListDiff, InsnListDiff, UnionFind<LabelNode>> normalized = InsnListDiffUtils.normalizeLabels(
            diff1.instructions,
            diff2.instructions
    );
    Pair<InsnListDiff, InsnListDiff> instructions = InsnListDiffUtils.commute(
            normalized.first,
            normalized.second
    );
    diff2Prime.instructions = instructions.first;
    diff1Prime.instructions = instructions.second;

    Pair<ListDiff<LineNumberNode>, ListDiff<LineNumberNode>> lineNumbers = ListDiffUtils.commute(
            ListDiffUtils.mapOperands(
                    diff1.lineNumbers,
                    ln -> (LineNumberNode) AbstractInsnNodeHelper.mapLabelTargets(ln, normalized.third::find)
            ),
            ListDiffUtils.mapOperands(
                    diff2.lineNumbers,
                    ln -> (LineNumberNode) AbstractInsnNodeHelper.mapLabelTargets(ln, normalized.third::find)
            ),
            AbstractInsnNodeHelper::equals
    );
    diff2Prime.lineNumbers = lineNumbers.first;
    diff1Prime.lineNumbers = lineNumbers.second;

    Pair<ListDiff<TryCatchBlockNode>, ListDiff<TryCatchBlockNode>> tryCatchBlocks = ListDiffUtils.commute(
            ListDiffUtils.mapOperands(
                    diff1.tryCatchBlocks,
                    tcb -> TryCatchBlockNodeHelper.mapLabels(tcb, normalized.third::find)
            ),
            ListDiffUtils.mapOperands(
                    diff2.tryCatchBlocks,
                    tcb -> TryCatchBlockNodeHelper.mapLabels(tcb, normalized.third::find)
            ),
            TryCatchBlockNodeHelper::equals
    );
    diff2Prime.tryCatchBlocks = tryCatchBlocks.first;
    diff1Prime.tryCatchBlocks = tryCatchBlocks.second;

    Pair<ListDiff<Integer>, ListDiff<Integer>> maxStack = ListDiffUtils.commute(diff1.maxStack, diff2.maxStack, Integer::equals);
    diff2Prime.maxStack = maxStack.first;
    diff1Prime.maxStack = maxStack.second;

    Pair<ListDiff<Integer>, ListDiff<Integer>> maxLocals = ListDiffUtils.commute(diff1.maxLocals, diff2.maxLocals, Integer::equals);
    diff2Prime.maxLocals = maxLocals.first;
    diff1Prime.maxLocals = maxLocals.second;

    Pair<ListDiff<LocalVariableNode>, ListDiff<LocalVariableNode>> localVariables = ListDiffUtils.commute(
            ListDiffUtils.mapOperands(
                    diff1.localVariables,
                    lv -> LocalVariableNodeHelper.mapLabels(lv, normalized.third::find)
            ),
            ListDiffUtils.mapOperands(
                    diff2.localVariables,
                    lv -> LocalVariableNodeHelper.mapLabels(lv, normalized.third::find)
            ),
            LocalVariableNodeHelper::equals
    );
    diff2Prime.localVariables = localVariables.first;
    diff1Prime.localVariables = localVariables.second;

    Pair<ListDiff<LocalVariableAnnotationNode>, ListDiff<LocalVariableAnnotationNode>> visibleLocalVariableAnnotations = ListDiffUtils.commute(
            ListDiffUtils.mapOperands(
                    diff1.visibleLocalVariableAnnotations,
                    lva -> AnnotationNodeHelper.mapLabels(lva, normalized.third::find)
            ),
            ListDiffUtils.mapOperands(
                    diff2.visibleLocalVariableAnnotations,
                    lva -> AnnotationNodeHelper.mapLabels(lva, normalized.third::find)
            ),
            LocalVariableAnnotationNode::equals
    );
    diff2Prime.visibleLocalVariableAnnotations = visibleLocalVariableAnnotations.first;
    diff1Prime.visibleLocalVariableAnnotations = visibleLocalVariableAnnotations.second;

    Pair<ListDiff<LocalVariableAnnotationNode>, ListDiff<LocalVariableAnnotationNode>> invisibleLocalVariableAnnotations = ListDiffUtils.commute(
            ListDiffUtils.mapOperands(
                    diff1.invisibleLocalVariableAnnotations,
                    lva -> AnnotationNodeHelper.mapLabels(lva, normalized.third::find)
            ),
            ListDiffUtils.mapOperands(
                    diff2.invisibleLocalVariableAnnotations,
                    lva -> AnnotationNodeHelper.mapLabels(lva, normalized.third::find)
            ),
            LocalVariableAnnotationNode::equals
    );
    diff2Prime.invisibleLocalVariableAnnotations = invisibleLocalVariableAnnotations.first;
    diff1Prime.invisibleLocalVariableAnnotations = invisibleLocalVariableAnnotations.second;

    return Pair.of(diff2Prime, diff1Prime);
  }

  public static void write(MethodDiff diff, CustomDataOutput out) throws IOException {
    Function<LabelNode, Integer> labelToIndex = l -> ((IndexedLabelNode) l).index;

    out.writeBoolean(diff == null);

    if (diff == null) {
      return;
    }

    ListDiffUtils.write(diff.access, out, (element, stream) -> stream.writeInt(element));
    ListDiffUtils.write(diff.name, out, (element, stream) -> stream.writeUTF(element));
    ListDiffUtils.write(diff.desc, out, (element, stream) -> stream.writeUTF(element));
    ListDiffUtils.write(diff.signature, out, (element, stream) -> stream.writeUTF(element));
    ListDiffUtils.write(diff.exceptions, out, (element, stream) -> stream.writeUTF(element));
    ListDiffUtils.write(diff.parameters, out, ParameterNodeHelper::write);
    ListDiffUtils.write(diff.visibleAnnotations, out, AnnotationNodeHelper::write);
    ListDiffUtils.write(diff.invisibleAnnotations, out, AnnotationNodeHelper::write);
    ListDiffUtils.write(diff.visibleTypeAnnotations, out, AnnotationNodeHelper::write);
    ListDiffUtils.write(diff.invisibleTypeAnnotations, out, AnnotationNodeHelper::write);
    ListDiffUtils.write(diff.annotationDefault, out, AnnotationNodeHelper::writeValue);
    ListDiffUtils.write(diff.visibleAnnotableParameterCount, out, (element, stream) -> stream.writeInt(element));
    KeyedListDiffUtils.write(
            diff.visibleParameterAnnotations,
            out,
            (element, stream) -> stream.writeInt(element),
            (element, stream) -> ListHelper.write(element, stream, AnnotationNodeHelper::write),
            (element, stream) -> ListDiffUtils.write(element, stream, AnnotationNodeHelper::write)
    );
    ListDiffUtils.write(diff.invisibleAnnotableParameterCount, out, (element, stream) -> stream.writeInt(element));
    KeyedListDiffUtils.write(
            diff.invisibleParameterAnnotations,
            out,
            (element, stream) -> stream.writeInt(element),
            (element, stream) -> ListHelper.write(element, stream, AnnotationNodeHelper::write),
            (element, stream) -> ListDiffUtils.write(element, stream, AnnotationNodeHelper::write)
    );
    InsnListDiffUtils.write(
            diff.instructions,
            out,
            labelToIndex
    );
    ListDiffUtils.write(
            diff.lineNumbers,
            out,
            (element, stream) -> AbstractInsnNodeHelper.write(element, stream, labelToIndex)
    );
    ListDiffUtils.write(
            diff.tryCatchBlocks,
            out,
            (element, stream) -> TryCatchBlockNodeHelper.write(element, stream, labelToIndex)
    );
    ListDiffUtils.write(
            diff.maxStack,
            out,
            (element, stream) -> stream.writeInt(element)
    );
    ListDiffUtils.write(
            diff.maxLocals,
            out,
            (element, stream) -> stream.writeInt(element)
    );
    ListDiffUtils.write(
            diff.localVariables,
            out,
            (element, stream) -> LocalVariableNodeHelper.write(element, stream, labelToIndex)
    );
    ListDiffUtils.write(
            diff.visibleLocalVariableAnnotations,
            out,
            (element, stream) -> AnnotationNodeHelper.write(element, stream, labelToIndex)
    );
    ListDiffUtils.write(
            diff.invisibleLocalVariableAnnotations,
            out,
            (element, stream) -> AnnotationNodeHelper.write(element, stream, labelToIndex)
    );
  }

  public static MethodDiff read(CustomDataInput in) throws IOException {
    Map<Integer, IndexedLabelNode> labelMap = new HashMap<>();
    Function<Integer, LabelNode> indexToLabel = i -> labelMap.computeIfAbsent(i, IndexedLabelNode::new);

    if (in.readBoolean()) {
      return null;
    }

    MethodDiff diff = new MethodDiff();
    diff.access = ListDiffUtils.read(in, DataInput::readInt);
    diff.name = ListDiffUtils.read(in, DataInput::readUTF);
    diff.desc = ListDiffUtils.read(in, DataInput::readUTF);
    diff.signature = ListDiffUtils.read(in, DataInput::readUTF);
    diff.exceptions = ListDiffUtils.read(in, DataInput::readUTF);
    diff.parameters = ListDiffUtils.read(in, ParameterNodeHelper::read);
    diff.visibleAnnotations = ListDiffUtils.read(in, AnnotationNodeHelper::readAnnotationNode);
    diff.invisibleAnnotations = ListDiffUtils.read(in, AnnotationNodeHelper::readAnnotationNode);
    diff.visibleTypeAnnotations = ListDiffUtils.read(in, AnnotationNodeHelper::readTypeAnnotationNode);
    diff.invisibleTypeAnnotations = ListDiffUtils.read(in, AnnotationNodeHelper::readTypeAnnotationNode);
    diff.annotationDefault = ListDiffUtils.read(in, AnnotationNodeHelper::readValue);
    diff.visibleAnnotableParameterCount = ListDiffUtils.read(in, DataInput::readInt);
    diff.visibleParameterAnnotations = KeyedListDiffUtils.read(
            in,
            DataInput::readInt,
            stream -> ListHelper.read(stream, AnnotationNodeHelper::readAnnotationNode),
            stream -> ListDiffUtils.read(stream, AnnotationNodeHelper::readAnnotationNode)
    );
    diff.invisibleAnnotableParameterCount = ListDiffUtils.read(in, DataInput::readInt);
    diff.invisibleParameterAnnotations = KeyedListDiffUtils.read(
            in,
            DataInput::readInt,
            stream -> ListHelper.read(stream, AnnotationNodeHelper::readAnnotationNode),
            stream -> ListDiffUtils.read(stream, AnnotationNodeHelper::readAnnotationNode)
    );
    diff.instructions = InsnListDiffUtils.read(in, indexToLabel);
    diff.lineNumbers = ListDiffUtils.read(in, stream -> (LineNumberNode) AbstractInsnNodeHelper.read(stream, indexToLabel));
    diff.tryCatchBlocks = ListDiffUtils.read(in, stream -> TryCatchBlockNodeHelper.read(stream, indexToLabel));
    diff.maxStack = ListDiffUtils.read(in, DataInput::readInt);
    diff.maxLocals = ListDiffUtils.read(in, DataInput::readInt);
    diff.localVariables = ListDiffUtils.read(in, stream -> LocalVariableNodeHelper.read(stream, indexToLabel));
    diff.visibleLocalVariableAnnotations = ListDiffUtils.read(in, stream -> AnnotationNodeHelper.readLocalVariableAnnotationNode(stream, indexToLabel));
    diff.invisibleLocalVariableAnnotations = ListDiffUtils.read(in, stream -> AnnotationNodeHelper.readLocalVariableAnnotationNode(stream, indexToLabel));

    return diff;
  }

  public static int distance(MethodDiff diff) {
    if (diff == null) {
      return 0;
    }

    return ListDiffUtils.distance(diff.access)
            + ListDiffUtils.distance(diff.name)
            + ListDiffUtils.distance(diff.desc)
            + ListDiffUtils.distance(diff.signature)
            + ListDiffUtils.distance(diff.exceptions)
            + ListDiffUtils.distance(diff.parameters)
            + ListDiffUtils.distance(diff.visibleAnnotations)
            + ListDiffUtils.distance(diff.invisibleAnnotations)
            + ListDiffUtils.distance(diff.visibleTypeAnnotations)
            + ListDiffUtils.distance(diff.invisibleTypeAnnotations)
            + ListDiffUtils.distance(diff.annotationDefault)
            + ListDiffUtils.distance(diff.visibleAnnotableParameterCount)
            + KeyedListDiffUtils.distance(diff.visibleParameterAnnotations, ListDiffUtils::distance)
            + ListDiffUtils.distance(diff.invisibleAnnotableParameterCount)
            + KeyedListDiffUtils.distance(diff.invisibleParameterAnnotations, ListDiffUtils::distance)
            + InsnListDiffUtils.distance(diff.instructions)
            + ListDiffUtils.distance(diff.lineNumbers)
            + ListDiffUtils.distance(diff.tryCatchBlocks)
            + ListDiffUtils.distance(diff.maxStack)
            + ListDiffUtils.distance(diff.maxLocals)
            + ListDiffUtils.distance(diff.localVariables)
            + ListDiffUtils.distance(diff.visibleLocalVariableAnnotations)
            + ListDiffUtils.distance(diff.invisibleLocalVariableAnnotations);
  }
}
