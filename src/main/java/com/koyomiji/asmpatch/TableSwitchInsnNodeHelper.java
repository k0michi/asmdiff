package com.koyomiji.asmpatch;

import org.objectweb.asm.tree.TableSwitchInsnNode;

public class TableSwitchInsnNodeHelper {
    public static boolean equals(TableSwitchInsnNode a, TableSwitchInsnNode b) {
      if (!AbstractInsnNodeHelper.equals(a, b)) {
        return false;
      }

      if (a.min != b.min) {
        return false;
      }

      if (a.max != b.max) {
        return false;
      }

      if (!LabelNodeHelper.equals(a.dflt, b.dflt)) {
        return false;
      }

      if (!ListHelper.equals(a.labels, b.labels, LabelNodeHelper::equals)) {
        return false;
      }

      return true;
    }
}
