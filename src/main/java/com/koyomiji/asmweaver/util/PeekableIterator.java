package com.koyomiji.asmweaver.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class PeekableIterator<E> implements Iterator<E> {
  private final Iterator<? extends E> iterator;
  private E nextElement;
  private boolean hasNext;

  public PeekableIterator(Iterator<? extends E> iterator) {
    this.iterator = iterator;
    advance();
  }

  private void advance() {
    if (iterator.hasNext()) {
      nextElement = iterator.next();
      hasNext = true;
    } else {
      nextElement = null;
      hasNext = false;
    }
  }

  public E peek() {
    if (!hasNext) {
      throw new NoSuchElementException();
    }
    return nextElement;
  }

  @Override
  public boolean hasNext() {
    return hasNext;
  }

  @Override
  public E next() {
    if (!hasNext) {
      throw new NoSuchElementException();
    }
    E result = nextElement;
    advance();
    return result;
  }
}
