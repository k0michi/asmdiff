package com.koyomiji.asmweaver.json;

public class JsonVisitor {
  protected JsonVisitor delegate;

  protected JsonVisitor() {
    this(null);
  }

  protected JsonVisitor(JsonVisitor delegate) {
    this.delegate = delegate;
  }

  public JsonVisitor getDelegate() {
    return delegate;
  }

  public void visitString(String key, String value) {
    if (delegate != null) {
      delegate.visitString(key, value);
    }
  }

  public void visitNumber(String key, Number value) {
    if (delegate != null) {
      delegate.visitNumber(key, value);
    }
  }

  public void visitBoolean(String key, Boolean value) {
    if (delegate != null) {
      delegate.visitBoolean(key, value);
    }
  }

  public void visitNull(String key) {
    if (delegate != null) {
      delegate.visitNull(key);
    }
  }

  public JsonVisitor visitObject(String key) {
    if (delegate != null) {
      return delegate.visitObject(key);
    }

    return null;
  }

  public JsonVisitor visitArray(String key) {
    if (delegate != null) {
      return delegate.visitArray(key);
    }

    return null;
  }

  public void visitEnd() {
    if (delegate != null) {
      delegate.visitEnd();
    }
  }
}
