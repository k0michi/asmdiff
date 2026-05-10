package com.koyomiji.asmweaver.analysis;

import com.koyomiji.asmweaver.util.UnionFind;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefUseChainAnalyzer {
  private final Analyzer<SourceValue> sourceAnalyzer;
  private List<List<Integer>> predecessors;

  public DefUseChainAnalyzer() {
    // Analyzerの初期化とエッジ情報の収集
    this.sourceAnalyzer = new Analyzer<>(new SourceInterpreter()) {
      @Override
      protected void newControlFlowEdge(int insnIndex, int successorIndex) {
        ensurePredecessorCapacity(successorIndex);
        predecessors.get(successorIndex).add(insnIndex);
      }

      @Override
      protected boolean newControlFlowExceptionEdge(int insnIndex, int successorIndex) {
        ensurePredecessorCapacity(successorIndex);
        predecessors.get(successorIndex).add(insnIndex);
        return true;
      }
    };
  }

  private void ensurePredecessorCapacity(int index) {
    while (predecessors.size() <= index) {
      predecessors.add(new ArrayList<>());
    }
  }

  public UnionFind<DefUse> analyze(String owner, MethodNode methodNode) throws AnalyzerException {
    this.predecessors = new ArrayList<>();
    int nInsn = methodNode.instructions.size();

    // 1. 解析の実行
    Frame<SourceValue>[] frames = sourceAnalyzer.analyze(owner, methodNode);

    UnionFind<DefUse> uf = new UnionFind<>();

    // 2. パラメタ（引数）の範囲を計算
    int paramSize = calculateParamSize(methodNode);

    // 3. パラメタ専用の処理: 全命令地点と仮想インデックス -1 を結合
    for (int v = 0; v < paramSize; v++) {
      DefUse paramVirtualNode = new DefUse(-1, null, v);
      uf.addNode(paramVirtualNode);

      for (int i = 0; i < nInsn; i++) {
        if (frames[i] == null) continue;
        DefUse node = new DefUse(i, methodNode.instructions.get(i), v);
        uf.addNode(node);
        uf.union(node, paramVirtualNode);
      }
    }

    // 4. パラメタ以外の変数に対して DFS で Use -> Def を遡る
    Set<String> visited = new HashSet<>();
    for (int i = 0; i < nInsn; i++) {
      AbstractInsnNode insn = methodNode.instructions.get(i);
      if (frames[i] == null) continue;

      if (isUseInsn(insn)) {
        int varIdx = getVarIndex(insn);

        // パラメタ範囲外の変数のみ DFS を実行
        if (varIdx >= paramSize) {
          SourceValue sv = frames[i].getLocal(varIdx);
          if (sv != null) {
            traceBack(i, varIdx, sv, uf, visited, methodNode.instructions);
          }
        }
      }
    }
    return uf;
  }

  /**
   * static かどうかと引数型を考慮して、パラメタが占有するローカル変数スロット数を計算する
   */
  private int calculateParamSize(MethodNode methodNode) {
    int size = 0;
    // static でない場合は index 0 は 'this'
    if ((methodNode.access & Opcodes.ACC_STATIC) == 0) {
      size++;
    }
    // 引数リストの型を解析 (long, double は 2 スロット消費)
    Type[] argumentTypes = Type.getArgumentTypes(methodNode.desc);
    for (Type t : argumentTypes) {
      size += t.getSize();
    }
    return size;
  }

  // --- 以下、isUseInsn, getVarIndex, traceBack は前回の実装をベースに使用 ---

  private boolean isUseInsn(AbstractInsnNode insn) {
    int opcode = insn.getOpcode();
    return (opcode >= Opcodes.ILOAD && opcode <= Opcodes.ALOAD) ||
            opcode == Opcodes.RET || opcode == Opcodes.IINC;
  }

  private int getVarIndex(AbstractInsnNode insn) {
    if (insn instanceof VarInsnNode) return ((VarInsnNode) insn).var;
    if (insn instanceof IincInsnNode) return ((IincInsnNode) insn).var;
    throw new IllegalArgumentException("Unknown var insn");
  }

  private void traceBack(int currentInsnIdx, int varIdx, SourceValue sv, UnionFind<DefUse> uf,
                         Set<String> visited, InsnList instructions) {
    String key = currentInsnIdx + ":" + varIdx + ":" + System.identityHashCode(sv);
    if (!visited.add(key)) return;

    DefUse currentNode = new DefUse(currentInsnIdx, instructions.get(currentInsnIdx), varIdx);
    uf.addNode(currentNode);

    // 定義元に到達
    if (sv.insns != null && sv.insns.contains(instructions.get(currentInsnIdx))) {
      return;
    }

    // 逆方向に伝播
    if (currentInsnIdx < predecessors.size()) {
      for (int predIdx : predecessors.get(currentInsnIdx)) {
        DefUse predNode = new DefUse(predIdx, instructions.get(predIdx), varIdx);
        uf.addNode(predNode);
        uf.union(currentNode, predNode);
        traceBack(predIdx, varIdx, sv, uf, visited, instructions);
      }
    }
  }
}