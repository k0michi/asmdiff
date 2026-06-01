package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.PeekableIterator;
import com.koyomiji.asmweaver.util.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class KeyedListDiffPairIterator<Key, Value, Diff> implements Iterator<Pair<KeyedListDiff.Operation<Key, Value, Diff>, KeyedListDiff.Operation<Key, Value, Diff>>> {
  @Nullable
  private final PeekableIterator<KeyedListDiff.Operation<Key, Value, Diff>> itP;
  @Nullable private final PeekableIterator<KeyedListDiff.Operation<Key, Value, Diff>> itQ;

  public KeyedListDiffPairIterator(
          @Nullable KeyedListDiff<Key, Value, Diff> p,
          @Nullable KeyedListDiff<Key, Value, Diff> q) {
    this.itP = p == null ? null : new PeekableIterator<>(p.operations.iterator());
    this.itQ = q == null ? null : new PeekableIterator<>(q.operations.iterator());
  }

  @Override
  public boolean hasNext() {
    return (itP != null && itP.hasNext()) || (itQ != null && itQ.hasNext());
  }

  @Override
  public Pair<KeyedListDiff.Operation<Key, Value, Diff>, KeyedListDiff.Operation<Key, Value, Diff>> next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }

    KeyedListDiff.Operation.Type pPeekType = peekType(itP);
    KeyedListDiff.Operation.Type qPeekType = peekType(itQ);

    // 1. P が DELETE の場合：Q側の入力からはそのノードが消えているため、Qは進めない
    if (pPeekType == KeyedListDiff.Operation.Type.DELETE) {
      return Pair.of(itP.next(), null);
    }

    // 2. Q が INSERT の場合：Pの出力とは無関係にQが勝手に挿入したノードなので、Pは進めない
    if (qPeekType == KeyedListDiff.Operation.Type.INSERT) {
      return Pair.of(null, itQ.next());
    }

    // 3. 1対1対応（MATCH同士、または P-INSERT と Q-MATCH）
    if (itP == null) {
      // P は全 MATCH。Q の実要素を消費し、P 側は Q の各フィールドから仮想 MATCH を生成
      KeyedListDiff.Operation<Key, Value, Diff> qOp = itQ.next();
      KeyedListDiff.Operation<Key, Value, Diff> pVirtualMatch = new KeyedListDiff.Operation<>(
              KeyedListDiff.Operation.Type.MATCH, qOp.mode, qOp.operandKey, null, null);
      return Pair.of(pVirtualMatch, qOp);
    }

    if (itQ == null) {
      // Q は全 MATCH。P の実要素を消費し、Q 側は P の各フィールドから仮想 MATCH を生成
      KeyedListDiff.Operation<Key, Value, Diff> pOp = itP.next();
      KeyedListDiff.Operation<Key, Value, Diff> qVirtualMatch = new KeyedListDiff.Operation<>(
              KeyedListDiff.Operation.Type.MATCH, pOp.mode, pOp.operandKey, null, null);
      return Pair.of(pOp, qVirtualMatch);
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
  private static <Key, Value, Diff> KeyedListDiff.Operation.Type peekType(
          @Nullable PeekableIterator<KeyedListDiff.Operation<Key, Value, Diff>> it) {
    if (it == null || !it.hasNext()) return null;
    return it.peek().type;
  }
}