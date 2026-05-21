package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.analysis.DefUse;
import com.koyomiji.asmweaver.analysis.DefUseChainAnalyzer;
import com.koyomiji.asmweaver.io.CustomDataInput;
import com.koyomiji.asmweaver.io.CustomDataOutput;
import com.koyomiji.asmweaver.util.UnionFind;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.io.DataInput;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    Map<LabelNode, LabelNode> labelMap = InsnListDiffUtils.extractLabelMap(
            new InsnListListAdapter(node1.instructions),
            new InsnListListAdapter(node2.instructions),
            diff.instructions
    );
//    List<Pair<Integer, Integer>> locals1 = new ArrayList<>();
//    List<Pair<Integer, Integer>> locals2 = new ArrayList<>();

    diff.tryCatchBlocks = ListDiffUtils.diff(
            node1.tryCatchBlocks,
            node2.tryCatchBlocks,
            (a, b) -> TryCatchBlockNodeHelper.equals(a, b, (lA, lB) -> labelMap.get(lA) == lB)
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
            ListHelper.nullToEmpty(node1.localVariables),
            ListHelper.nullToEmpty(node2.localVariables),
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

  public static MethodNode patch(MethodNode node, MethodDiff diff) {
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

    HashMap<LabelNode, LabelNode> labelMap = new HashMap<>();
    patched.instructions = InsnListHelper.fromList(InsnListDiffUtils.patch(
            new InsnListListAdapter(node.instructions),
            diff.instructions,
            labelMap
    ));

    patched.tryCatchBlocks = ListDiffUtils.patch(
            node.tryCatchBlocks,
            diff.tryCatchBlocks
    );

    for (TryCatchBlockNode tryCatchBlock : patched.tryCatchBlocks) {
      labelMap.putIfAbsent(tryCatchBlock.start, new LabelNode());
      tryCatchBlock.start = labelMap.get(tryCatchBlock.start);
      labelMap.putIfAbsent(tryCatchBlock.end, new LabelNode());
      tryCatchBlock.end = labelMap.get(tryCatchBlock.end);
      labelMap.putIfAbsent(tryCatchBlock.handler, new LabelNode());
      tryCatchBlock.handler = labelMap.get(tryCatchBlock.handler);
    }

    patched.localVariables = ListDiffUtils.patch(
            node.localVariables,
            diff.localVariables
    );

    for (LocalVariableNode localVariable : patched.localVariables) {
      labelMap.putIfAbsent(localVariable.start, new LabelNode());
      localVariable.start = labelMap.get(localVariable.start);
      labelMap.putIfAbsent(localVariable.end, new LabelNode());
      localVariable.end = labelMap.get(localVariable.end);
    }

    patched.visibleLocalVariableAnnotations = ListDiffUtils.patch(
            node.visibleLocalVariableAnnotations,
            diff.visibleLocalVariableAnnotations
    );

    for (LocalVariableAnnotationNode localVarAnn : patched.visibleLocalVariableAnnotations) {
      for (int i = 0; i < localVarAnn.start.size(); i++) {
        labelMap.putIfAbsent(localVarAnn.start.get(i), new LabelNode());
        localVarAnn.start.set(i, labelMap.get(localVarAnn.start.get(i)));
      }

      for (int i = 0; i < localVarAnn.end.size(); i++) {
        labelMap.putIfAbsent(localVarAnn.end.get(i), new LabelNode());
        localVarAnn.end.set(i, labelMap.get(localVarAnn.end.get(i)));
      }
    }

    patched.invisibleLocalVariableAnnotations = ListDiffUtils.patch(
            node.invisibleLocalVariableAnnotations,
            diff.invisibleLocalVariableAnnotations
    );

    for (LocalVariableAnnotationNode localVarAnn : patched.invisibleLocalVariableAnnotations) {
      for (int i = 0; i < localVarAnn.start.size(); i++) {
        labelMap.putIfAbsent(localVarAnn.start.get(i), new LabelNode());
        localVarAnn.start.set(i, labelMap.get(localVarAnn.start.get(i)));
      }

      for (int i = 0; i < localVarAnn.end.size(); i++) {
        labelMap.putIfAbsent(localVarAnn.end.get(i), new LabelNode());
        localVarAnn.end.set(i, labelMap.get(localVarAnn.end.get(i)));
      }
    }

    return patched;
  }

  private static List<List<AnnotationNode>> normalizeAnnotations(List<AnnotationNode>[] annotations) {
    return ListHelper.map(
            ListHelper.ofNullableArray(annotations),
            ListHelper::nullToEmpty
    );
  }

  public static void write(MethodDiff diff, CustomDataOutput out, Function<LabelNode, Integer> labelToIndex) throws IOException {
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
            AnnotationNodeHelper::write
    );
    ListDiffUtils.write(
            diff.invisibleLocalVariableAnnotations,
            out,
            AnnotationNodeHelper::write
    );
  }

  public static MethodDiff read(CustomDataInput in, Function<Integer, LabelNode> labelToIndex) throws IOException {
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
    diff.instructions = InsnListDiffUtils.read(in, labelToIndex);
    diff.tryCatchBlocks = ListDiffUtils.read(in, stream -> TryCatchBlockNodeHelper.read(stream, labelToIndex));
    diff.maxStack = ListDiffUtils.read(in, DataInput::readInt);
    diff.maxLocals = ListDiffUtils.read(in, DataInput::readInt);
    diff.localVariables = ListDiffUtils.read(in, stream -> LocalVariableNodeHelper.read(stream, labelToIndex));
    diff.visibleLocalVariableAnnotations = ListDiffUtils.read(in, stream -> AnnotationNodeHelper.readLocalVariableAnnotationNode(stream, labelToIndex));
    diff.invisibleLocalVariableAnnotations = ListDiffUtils.read(in, stream -> AnnotationNodeHelper.readLocalVariableAnnotationNode(stream, labelToIndex));

    return diff;
  }
}
