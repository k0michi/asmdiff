package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.CustomDataInput;
import com.koyomiji.asmweaver.io.CustomDataOutput;
import com.koyomiji.asmweaver.io.DataStreamHelper;
import com.koyomiji.asmweaver.util.tuple.Triplet;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;

import java.io.IOException;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class LocalVariableNodeHelper {
  public static boolean equals(LocalVariableNode a, LocalVariableNode b, BiPredicate<LabelNode, LabelNode> labelEquals, BiPredicate<Triplet<LabelNode, LabelNode, Integer>, Triplet<LabelNode, LabelNode, Integer>> localEquals) {
    if (a == b) {
      return true;
    }

    if (a == null || b == null) {
      return false;
    }

    if (a.getClass() != b.getClass()) {
      return false;
    }

    return Objects.equals(a.name, b.name)
            && Objects.equals(a.desc, b.desc)
            && Objects.equals(a.signature, b.signature)
            && labelEquals.test(a.start, b.start)
            && labelEquals.test(a.end, b.end)
            && localEquals.test(Triplet.of(a.start, a.end, a.index), Triplet.of(b.start, b.end, b.index));
  }

  public static boolean equals(LocalVariableNode a, LocalVariableNode b) {
    return equals(a, b, Objects::equals, Objects::equals);
  }

  public static boolean equals(LocalVariableNode a, LocalVariableNode b, BiPredicate<LabelNode, LabelNode> labelEquals) {
    return equals(a, b, labelEquals, Objects::equals);
  }

  public static int hashCode(LocalVariableNode node) {
    if (node == null) {
      return 0;
    }

    return Objects.hash(node.name, node.desc, node.signature, node.start, node.end, node.index);
  }

  public static void write(LocalVariableNode node, CustomDataOutput out, Function<LabelNode, Integer> labelToIndex) throws IOException {
    out.writeUTF(node.name);
    out.writeUTF(node.desc);
    DataStreamHelper.writeUTFNullable(out, node.signature);
    out.writeInt(labelToIndex.apply(node.start));
    out.writeInt(labelToIndex.apply(node.end));
    out.writeInt(node.index);
  }

  public static LocalVariableNode read(CustomDataInput in, Function<Integer, LabelNode> indexToLabel) throws IOException {
    String name = in.readUTF();
    String desc = in.readUTF();
    String signature = DataStreamHelper.readUTFNullable(in);
    LabelNode start = indexToLabel.apply(in.readInt());
    LabelNode end = indexToLabel.apply(in.readInt());
    int index = in.readInt();
    return new LocalVariableNode(name, desc, signature, start, end, index);
  }
}
