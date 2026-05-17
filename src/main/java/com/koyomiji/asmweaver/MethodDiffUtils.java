package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.analysis.DefUse;
import com.koyomiji.asmweaver.analysis.DefUseChainAnalyzer;
import com.koyomiji.asmweaver.util.UnionFind;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
      List<List<AnnotationNode>> vpa1 = ListHelper.ofNullableArray(node1.visibleParameterAnnotations);
      List<List<AnnotationNode>> vpa2 = ListHelper.ofNullableArray(node2.visibleParameterAnnotations);
      Map<AnnotationNode, Integer> vpaIndexMap = new HashMap<>();

      for (int i = 0; i < vpa1.size(); i++) {
        for (AnnotationNode an : vpa1.get(i)) {
          vpaIndexMap.put(an, i);
        }
      }

      for (int i = 0; i < vpa2.size(); i++) {
        for (AnnotationNode an : vpa2.get(i)) {
          vpaIndexMap.put(an, i);
        }
      }

      diff.visibleParameterAnnotations = KeyedListDiffUtils.diff(vpa1, vpa2, vpaIndexMap::get, (l1, l2) -> ListDiffUtils.diff(l1, l2, AnnotationNodeHelper::equals));
    }

    diff.invisibleAnnotableParameterCount = ListDiffUtils.diff(ListHelper.ofNullable(node1.invisibleAnnotableParameterCount), ListHelper.ofNullable(node2.invisibleAnnotableParameterCount), Integer::equals);

    {
      List<List<AnnotationNode>> ipa1 = ListHelper.ofNullableArray(node1.invisibleParameterAnnotations);
      List<List<AnnotationNode>> ipa2 = ListHelper.ofNullableArray(node2.invisibleParameterAnnotations);

      Map<AnnotationNode, Integer> ipaIndexMap = new HashMap<>();

      for (int i = 0; i < ipa1.size(); i++) {
        for (AnnotationNode an : ipa1.get(i)) {
          ipaIndexMap.put(an, i);
        }
      }

      for (int i = 0; i < ipa2.size(); i++) {
        for (AnnotationNode an : ipa2.get(i)) {
          ipaIndexMap.put(an, i);
        }
      }

      diff.invisibleParameterAnnotations = KeyedListDiffUtils.diff(ipa1, ipa2, ipaIndexMap::get, (l1, l2) -> ListDiffUtils.diff(l1, l2, AnnotationNodeHelper::equals));
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

    diff.instructions = InsnListDiffUtils.diff(
            new InsnListListAdapter(node1.instructions),
            duChains1::get,
            new InsnListListAdapter(node2.instructions),
            duChains2::get
    );

//    Map<LabelNode, Integer> labelIndices1 = new HashMap<>();
//
//    for (int i = 0; i < node1.instructions.size(); i++) {
//      AbstractInsnNode insn = node1.instructions.get(i);
//      if (insn instanceof LabelNode) {
//        labelIndices1.put((LabelNode) insn, i);
//      }
//    }
//
//    Map<LabelNode, Integer> labelIndices2 = new HashMap<>();
//
//    for (int i = 0; i < node2.instructions.size(); i++) {
//      AbstractInsnNode insn = node2.instructions.get(i);
//      if (insn instanceof LabelNode) {
//        labelIndices2.put((LabelNode) insn, i);
//      }
//    }

    Map<LabelNode, LabelNode> labelMap = InsnListDiffUtils.extractLabelMap(node1.instructions, node2.instructions, diff.instructions);
//    List<Pair<Integer, Integer>> locals1 = new ArrayList<>();
//    List<Pair<Integer, Integer>> locals2 = new ArrayList<>();

    diff.tryCatchBlocks = ListDiffUtils.diff(
            node1.tryCatchBlocks,
            node2.tryCatchBlocks,
            (a, b) -> TryCatchBlockNodeHelper.equals(a, b, (lA, lB) -> labelMap.get(lA) == lB)
    );

    diff.localVariables = ListDiffUtils.diff(
            node1.localVariables,
            node2.localVariables,
            (a, b) ->
                    LocalVariableNodeHelper.equals(a, b,
                            (lA, lB) -> labelMap.get(lA) == lB,
                            // FIXME: this is exact local match
                            (tA, tB) -> labelMap.get(tA.first) == tB.first && labelMap.get(tA.second) == tB.second && Objects.equals(tA.third, tB.third)
                    )
    );

    diff.visibleLocalVariableAnnotations = ListDiffUtils.diff(
            ListHelper.nullToEmpty(node1.visibleLocalVariableAnnotations),
            ListHelper.nullToEmpty(node2.visibleLocalVariableAnnotations),
            (a, b) ->
                    AnnotationNodeHelper.equals(a, b,
                            (lA, lB) -> labelMap.get(lA) == lB,
                            // FIXME: this is exact local match
                            (tA, tB) -> labelMap.get(tA.first) == tB.first && labelMap.get(tA.second) == tB.second && Objects.equals(tA.third, tB.third)
                    )
    );

    diff.invisibleLocalVariableAnnotations = ListDiffUtils.diff(
            ListHelper.nullToEmpty(node1.invisibleLocalVariableAnnotations),
            ListHelper.nullToEmpty(node2.invisibleLocalVariableAnnotations),
            (a, b) ->
                    AnnotationNodeHelper.equals(a, b,
                            (lA, lB) -> labelMap.get(lA) == lB,
                            // FIXME: this is exact local match
                            (tA, tB) -> labelMap.get(tA.first) == tB.first && labelMap.get(tA.second) == tB.second && Objects.equals(tA.third, tB.third)
                    )
    );

    return diff;
  }
}
