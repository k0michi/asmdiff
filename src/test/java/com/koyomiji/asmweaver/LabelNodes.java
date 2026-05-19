package com.koyomiji.asmweaver;

import org.objectweb.asm.tree.LabelNode;

public class LabelNodes {
  public static LabelNode l0 = new LabelNode();
  public static LabelNode l1 = new LabelNode();
  public static LabelNode l2 = new LabelNode();
  public static LabelNode l3 = new LabelNode();

  public static void reset() {
    l0 = new LabelNode();
    l1 = new LabelNode();
    l2 = new LabelNode();
    l3 = new LabelNode();
  }
}
