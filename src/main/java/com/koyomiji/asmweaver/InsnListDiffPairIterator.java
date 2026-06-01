package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.PeekableIterator;
import com.koyomiji.asmweaver.util.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class InsnListDiffPairIterator implements Iterator<Pair<InsnListDiff.Operation, InsnListDiff.Operation>> {
  @Nullable private final PeekableIterator<InsnListDiff.Operation> itP;
  @Nullable private final PeekableIterator<InsnListDiff.Operation> itQ;

  public InsnListDiffPairIterator(@Nullable InsnListDiff p, @Nullable InsnListDiff q) {
    this.itP = p == null ? null : new PeekableIterator<>(p.operations.iterator());
    this.itQ = q == null ? null : new PeekableIterator<>(q.operations.iterator());
  }

  @Override
  public boolean hasNext() {
    return (itP != null && itP.hasNext()) || (itQ != null && itQ.hasNext());
  }

  @Override
  public Pair<InsnListDiff.Operation, InsnListDiff.Operation> next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }

    InsnListDiff.Operation.Type pPeekType = peekType(itP);
    InsnListDiff.Operation.Type qPeekType = peekType(itQ);

    // 1. P が DELETE の場合：Q側の入力からはそのノードが消えているため、Qは進めない
    //    itP == null 時は MATCH 扱いのため DELETE にならず、この分岐には入らない
    if (pPeekType == InsnListDiff.Operation.Type.DELETE) {
      return Pair.of(itP.next(), null);
    }

    // 2. Q が INSERT の場合：Pの出力とは無関係にQが勝手に挿入したノードなので、Pは進めない
    //    itQ == null 時は MATCH 扱いのため INSERT にならず、この分岐には入らない
    if (qPeekType == InsnListDiff.Operation.Type.INSERT) {
      return Pair.of(null, itQ.next());
    }

    // 3. 1対1対応（MATCH同士、または P-INSERT と Q-MATCH）
    if (itP == null) {
      // P は全 MATCH。Q の実要素を消費し、P 側は Q の operand・mode から仮想 MATCH を生成
      InsnListDiff.Operation qOp = itQ.next();
      InsnListDiff.Operation pMatch = new InsnListDiff.Operation(
              InsnListDiff.Operation.Type.MATCH, qOp.mode, qOp.operand);
      return Pair.of(pMatch, qOp);
    }

    if (itQ == null) {
      // Q は全 MATCH。P の実要素を消費し、Q 側は P の operand・mode から仮想 MATCH を生成
      InsnListDiff.Operation pOp = itP.next();
      InsnListDiff.Operation qMatch = new InsnListDiff.Operation(
              InsnListDiff.Operation.Type.MATCH, pOp.mode, pOp.operand);
      return Pair.of(pOp, qMatch);
    }

    // 両方実在する通常ケース
    if (itP.hasNext() && itQ.hasNext()) {
      return Pair.of(itP.next(), itQ.next());
    }

    // 4. 不整合
    if (itP.hasNext()) {
      throw new IllegalDiffException("p has remaining operations after q is exhausted");
    } else {
      throw new IllegalDiffException("q has remaining operations after p is exhausted");
    }
  }

  @Nullable
  private static InsnListDiff.Operation.Type peekType(
          @Nullable PeekableIterator<InsnListDiff.Operation> it) {
    if (it == null || !it.hasNext()) return null;
    return it.peek().type;
  }
}
