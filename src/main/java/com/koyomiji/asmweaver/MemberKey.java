package com.koyomiji.asmweaver;

import java.util.Objects;

public class MemberKey {
  public String name;
  public String desc;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MemberKey memberKey = (MemberKey) o;
    return Objects.equals(name, memberKey.name) && Objects.equals(desc, memberKey.desc);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, desc);
  }
}
