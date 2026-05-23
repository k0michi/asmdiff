package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.PeekableIterator;
import com.koyomiji.asmweaver.util.tuple.Pair;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class InsnListDiffPairIterator implements Iterator<Pair<InsnListDiff.Operation, InsnListDiff.Operation>> {
  private final PeekableIterator<InsnListDiff.Operation> itP;
  private final PeekableIterator<InsnListDiff.Operation> itQ;

  public InsnListDiffPairIterator(Iterator<InsnListDiff.Operation> p, Iterator<InsnListDiff.Operation> q) {
    this.itP = new PeekableIterator<>(p);
    this.itQ = new PeekableIterator<>(q);
  }

  @Override
  public boolean hasNext() {
    return itP.hasNext() || itQ.hasNext();
  }

  @Override
  public Pair<InsnListDiff.Operation, InsnListDiff.Operation> next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }

    // 1. P が DELETE の場合：Q側の入力からはそのノードが消えているため、Qは進めない (null, P-Delete)
    if (itP.hasNext() && itP.peek().type == InsnListDiff.Operation.Type.DELETE) {
      return Pair.of(itP.next(), null);
    }

    // 2. Q が INSERT の場合：Pの出力とは無関係にQが勝手に挿入したノードなので、Pは進めない (Q-Insert, null)
    if (itQ.hasNext() && itQ.peek().type == InsnListDiff.Operation.Type.INSERT) {
      return Pair.of(null, itQ.next());
    }

    // 3. 両方に要素があり、上記の特殊条件を満たさない場合：1対1の対応（MATCH同士、または P-Insert と Q-Base）
    if (itP.hasNext() && itQ.hasNext()) {
      return Pair.of(itP.next(), itQ.next());
    }

    // 4. どちらかが途中で尽きた（不整合）場合のハンドリング
    if (itP.hasNext()) {
      // PにMATCH/INSERTが残っているがQが空
      throw new IllegalDiffException("p has remaining operations after q is exhausted");
    } else {
      // QにMATCH/DELETEが残っているがPが空
      throw new IllegalDiffException("q has remaining operations after p is exhausted");
    }
  }
}
