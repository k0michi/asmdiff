package com.koyomiji.asmweaver.benchmark;

import com.koyomiji.asmweaver.InsnListDiff;
import com.koyomiji.asmweaver.InsnListDiffUtils;
import com.koyomiji.asmweaver.InsnListListAdapter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime) // 平均実行時間を計測
//@OutputTimeUnit(TimeUnit.MILLISECONDS) // ナノ秒単位で表示
@State(Scope.Thread) // スレッドごとに状態を保持
@Warmup(iterations = 3, time = 1) // ウォームアップ（JIT最適化を待つ）
@Measurement(iterations = 5, time = 1) // 本計測
@Fork(1) // 別のJVMプロセスで実行（干渉防止）
public class DiffBenchmark {
  private InsnList list1;
  private InsnList list2;
  private InsnList list3;
  private InsnList list4;
  @Param({"1000", "10000"})
  public int size;

  @Setup
  public void setup() {
    list1 = new InsnList();
    list2 = new InsnList();
    for (int i = 0; i < size; i++) {
      list1.add(new InsnNode(Opcodes.NOP));
    }

    list3 = new InsnList();
    for (int i = 0; i < size; i++) {
      LabelNode label = new LabelNode();
      list3.add(label);
      list3.add(new JumpInsnNode(Opcodes.IFEQ, label));
    }

    list4 = new InsnList();
    for (int i = 0; i < size; i++) {
      LabelNode label = new LabelNode();
      list4.add(label);
    }

    for (int i = 0; i < size; i++) {
      list4.add(new JumpInsnNode(Opcodes.IFEQ, (LabelNode) list4.get(size - 1 - i)));
    }
  }

  @Benchmark
  public void benchmark_diff_deleteOnly(Blackhole bh) {
    InsnListDiff diff = InsnListDiffUtils.diff(
            new InsnListListAdapter(list1),
            (insn) -> -1,
            new InsnListListAdapter(list2),
            (insn) -> -1
    );
    bh.consume(diff);
  }

  @Benchmark
  public void benchmark_diff_matchLabels(Blackhole bh) {
    InsnListDiff diff = InsnListDiffUtils.diff(
            new InsnListListAdapter(list3),
            (insn) -> -1,
            new InsnListListAdapter(list3),
            (insn) -> -1
    );
    bh.consume(diff);
  }

  @Benchmark
  public void benchmark_diff_matchNestedLabels(Blackhole bh) {
    InsnListDiff diff = InsnListDiffUtils.diff(
            new InsnListListAdapter(list4),
            (insn) -> -1,
            new InsnListListAdapter(list4),
            (insn) -> -1
    );
    bh.consume(diff);
  }
}