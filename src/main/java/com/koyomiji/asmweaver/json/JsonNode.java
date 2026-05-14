package com.koyomiji.asmweaver.json;

import com.koyomiji.asmweaver.util.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class JsonNode extends JsonVisitor {
  public Type type;
  public List<Pair<String, Object>> children = new ArrayList<>();

  public enum Type {
    OBJECT,
    ARRAY
  }

  public JsonNode() {
    this(Type.ARRAY);
  }

  public JsonNode(Type type) {
    this.type = type;
  }

  @Override
  public void visitString(String key, String value) {
    children.add(Pair.of(key, value));
  }

  @Override
  public void visitNumber(String key, Number value) {
    children.add(Pair.of(key, value));
  }

  @Override
  public void visitBoolean(String key, Boolean value) {
    children.add(Pair.of(key, value));
  }

  @Override
  public void visitNull(String key) {
    children.add(Pair.of(key, null));
  }

  @Override
  public JsonVisitor visitObject(String key) {
    JsonNode child = new JsonNode();
    children.add(Pair.of(key, child));
    return child;
  }

  @Override
  public JsonVisitor visitArray(String key) {
    JsonNode child = new JsonNode();
    children.add(Pair.of(key, child));
    return child;
  }

  public void accept(JsonVisitor v) {
    accept(null, v);
  }

  public void accept(String key, JsonVisitor v) {
    JsonVisitor next = (type == Type.OBJECT) ? v.visitObject(key) : v.visitArray(key);

    if (next != null) {
      for (Pair<String, Object> pair : children) {
        String k = pair.first;
        Object val = pair.second;

        if (val instanceof JsonNode) {
          ((JsonNode) val).accept(k, next);
        } else if (val instanceof String) {
          next.visitString(k, (String) val);
        } else if (val instanceof Number) {
          next.visitNumber(k, (Number) val);
        } else if (val instanceof Boolean) {
          next.visitBoolean(k, (Boolean) val);
        } else if (val == null) {
          next.visitNull(k);
        }
      }

      next.visitEnd();
    }
  }
}
