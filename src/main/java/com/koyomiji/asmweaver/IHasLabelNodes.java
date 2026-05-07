package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.LabelNode;

import java.util.List;

public interface IHasLabelNodes {
  public List<LabelNode> getLabels();
}
