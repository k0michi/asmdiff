package com.koyomiji.asmweaver.analysis;

import com.koyomiji.asmweaver.util.UnionFind;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.*;

import java.util.HashMap;
import java.util.Map;

public class DefUseChainAnalyzer {
  private final Analyzer<SourceValue> sourceAnalyzer;
  private final Analyzer<BasicValue> basicAnalyzer;

  public DefUseChainAnalyzer() {
    this.sourceAnalyzer = new Analyzer<>(new SourceInterpreter());
    this.basicAnalyzer = new Analyzer<>(new BasicInterpreter());
  }

  public UnionFind<DefUse> analyze(String owner, MethodNode methodNode) throws AnalyzerException {
    Map<AbstractInsnNode, Integer> insnIndices = new HashMap<>();
    InsnList instructions = methodNode.instructions;
    for (int i = 0; i < instructions.size(); i++) {
      insnIndices.put(instructions.get(i), i);
    }

    // 1. SourceInterpreter (経路・定義元の追跡用)
//    Analyzer<SourceValue> sourceAnalyzer = new Analyzer<>(new SourceInterpreter());
    Frame<SourceValue>[] sourceFrames = sourceAnalyzer.analyze(owner, methodNode);

    // 2. BasicInterpreter (型情報の追跡用) を追加で走らせる
//    Analyzer<BasicValue> basicAnalyzer = new Analyzer<>(new BasicInterpreter());
    Frame<BasicValue>[] basicFrames = basicAnalyzer.analyze(owner, methodNode);

    UnionFind<DefUse> uf = new UnionFind<>();

    for (int i = 0; i < sourceFrames.length; i++) {
      Frame<SourceValue> sFrame = sourceFrames[i];
      Frame<BasicValue> bFrame = basicFrames[i];

      if (sFrame == null || bFrame == null) {
        continue;
      }

      for (int v = 0; v < sFrame.getLocals(); v++) {
        SourceValue sv = sFrame.getLocal(v);
        BasicValue bv = bFrame.getLocal(v);

        // 初期化されていないスロットのスキップ
        if (sv == null || bv == null) {
          continue;
        }

        // 【重要】型が矛盾・未初期化の場合は除外する
        // これにより、if-elseの合流で「死んだint」と「死んだString」が
        // 無理やりUnionされるのを防ぎます。
//        if (bv == BasicValue.UNINITIALIZED_VALUE) {
//          continue;
//        }

        if (sv.insns == null || sv.insns.isEmpty()) {
          // 引数の処理（必要に応じて復活させてください）
          // VarNode paramHub = new VarNode(-1, v);
          // uf.addNode(paramHub);
          // uf.union(currentNode, paramHub);
        } else {
          if (!(instructions.get(i) instanceof VarInsnNode)) {
            continue; // ロード/ストア以外の命令は無視
          }

          // 現在の位置とスロットのノードを作成
          DefUse currentNode = new DefUse(i, methodNode.instructions.get(i), v);
          uf.addNode(currentNode);

          for (AbstractInsnNode defInsn : sv.insns) {
            Integer defIndex = insnIndices.get(defInsn);
            if (defIndex != null) {
              DefUse defNode = new DefUse(defIndex, methodNode.instructions.get(defIndex), v);
              uf.addNode(defNode);
              uf.union(currentNode, defNode);
            }
          }
        }
      }
    }

    return uf;
  }
}
