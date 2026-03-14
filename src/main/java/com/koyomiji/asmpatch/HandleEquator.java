package com.koyomiji.asmpatch;

import org.objectweb.asm.Handle;

import java.util.Objects;

public class HandleEquator implements Equator<Handle> {
  @Override
  public boolean equals(Handle a, Handle b) {
    return Objects.equals(a.getTag(), b.getTag())
            && Objects.equals(a.getOwner(), b.getOwner())
            && Objects.equals(a.getName(), b.getName())
            && Objects.equals(a.getDesc(), b.getDesc())
            && Objects.equals(a.isInterface(), b.isInterface());
  }
}
