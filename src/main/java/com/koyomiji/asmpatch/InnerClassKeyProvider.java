package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.InnerClassNode;

import java.util.Objects;

public class InnerClassKeyProvider implements IKeyProvider<InnerClassNode, String> {
  @Override
  public String getKey(InnerClassNode value) {
    return value.name;
  }

  @Override
  public boolean compareValues(InnerClassNode value1, InnerClassNode value2) {
    return Objects.equals(value1.name, value2.name);
  }

  @Override
  public boolean compareKeys(String key1, String key2) {
    return Objects.equals(key1, key2);
  }

  @Override
  public boolean compareValueAndKey(InnerClassNode value, String key) {
    return Objects.equals(value.name, key);
  }
}
