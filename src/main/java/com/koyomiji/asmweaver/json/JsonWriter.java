package com.koyomiji.asmweaver.json;

import java.io.IOException;
import java.io.Writer;

public class JsonWriter extends JsonVisitor {
  private final Appendable out;
  private final boolean isArray;
  private final boolean isRoot;
  private int elementCount = 0; // トップレベルの要素数チェック用
  private boolean first = true;

  // 外部から呼ぶメインのコンストラクタ
  public JsonWriter(Appendable out) {
    this(out, false, true);
  }

  // 内部的な再帰用コンストラクタ
  private JsonWriter(Appendable out, boolean isArray, boolean isRoot) {
    this.out = out;
    this.isArray = isArray;
    this.isRoot = isRoot;
  }

  @Override
  public void visitEnd() {
    // 1. ルート要素が一度も visit されずに終了しようとしたら例外
    if (isRoot && elementCount == 0) {
      throw new IllegalStateException("JSON standard requires exactly one root element, but none were provided.");
    }

    // 2. Delegate があればそちらの visitEnd も呼ぶ (JsonVisitor の振る舞い)
    super.visitEnd();
  }

  private void prepare(String key) {
    try {
      if (isRoot && ++elementCount > 1) {
        throw new IllegalStateException("JSON standard allows only one root element.");
      }

      if (!first) {
        out.append(",");
      }
      first = false;

      if (!isArray && !isRoot && key != null) {
        out.append("\"").append(escape(key)).append("\":");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visitString(String key, String value) {
    prepare(key);
    try {
      if (value == null) {
        out.append("null");
      } else {
        out.append("\"").append(escape(value)).append("\"");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    super.visitString(key, value);
  }

  @Override
  public void visitNumber(String key, Number value) {
    prepare(key);
    try {
      out.append(value == null ? "null" : value.toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    super.visitNumber(key, value);
  }

  @Override
  public void visitBoolean(String key, Boolean value) {
    prepare(key);
    try {
      out.append(String.valueOf(value));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    super.visitBoolean(key, value);
  }

  @Override
  public void visitNull(String key) {
    prepare(key);
    try {
      out.append("null");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    super.visitNull(key);
  }

  @Override
  public JsonVisitor visitObject(String key) {
    prepare(key);
    try {
      out.append("{");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    // 新しいWriter(Objectスコープ)を返し、delegateも繋ぐ
    return new JsonWriter(out, false, false) {
      @Override
      public void visitEnd() {
        try {
          out.append("}");
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        super.visitEnd();
      }
    };
  }

  @Override
  public JsonVisitor visitArray(String key) {
    prepare(key);
    try {
      out.append("[");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    // 新しいWriter(Arrayスコープ)を返し、delegateも繋ぐ
    return new JsonWriter(out, true, false) {
      @Override
      public void visitEnd() {
        try {
          out.append("]");
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        super.visitEnd();
      }
    };
  }

  // 文字列のエスケープ処理
  private String escape(String s) {
    return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\f", "\\f")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
  }
}