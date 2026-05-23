package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.io.CustomDataInput;
import com.koyomiji.asmweaver.io.CustomDataOutput;
import com.koyomiji.asmweaver.io.DataStreamHelper;
import com.koyomiji.asmweaver.util.HashCodeBuilder;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeAnnotationNode;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class TryCatchBlockNodeHelper {
  public static boolean equals(TryCatchBlockNode node1, TryCatchBlockNode node2, BiPredicate<LabelNode, LabelNode> labelEquals) {
    if (node1 == node2) {
      return true;
    }

    if (node1 == null || node2 == null) {
      return false;
    }

    return labelEquals.test(node1.start, node2.start)
            && labelEquals.test(node1.end, node2.end)
            && labelEquals.test(node1.handler, node2.handler)
            && Objects.equals(node1.type, node2.type)
            && ListHelper.equalsNullToEmpty(node1.visibleTypeAnnotations, node2.visibleTypeAnnotations, AnnotationNodeHelper::equals)
            && ListHelper.equalsNullToEmpty(node1.invisibleTypeAnnotations, node2.invisibleTypeAnnotations, AnnotationNodeHelper::equals);
  }

  public static boolean equals(TryCatchBlockNode node1, TryCatchBlockNode node2) {
    return equals(node1, node2, Objects::equals);
  }

  public static int hashCode(TryCatchBlockNode node) {
    if (node == null) {
      return 0;
    }

    return new HashCodeBuilder()
            .append(node.start)
            .append(node.end)
            .append(node.handler)
            .append(node.type)
            .append(ListHelper.hashCodeNullToEmpty(node.visibleTypeAnnotations, AnnotationNodeHelper::hashCode))
            .append(ListHelper.hashCodeNullToEmpty(node.invisibleTypeAnnotations, AnnotationNodeHelper::hashCode))
            .build();
  }

  public static TryCatchBlockNode mapLabels(TryCatchBlockNode node, Function<LabelNode, LabelNode> labelMap) {
    if (node == null) {
      return null;
    }

    TryCatchBlockNode mapped = new TryCatchBlockNode(
            labelMap.apply(node.start),
            labelMap.apply(node.end),
            labelMap.apply(node.handler),
            node.type
    );
    mapped.visibleTypeAnnotations = node.visibleTypeAnnotations;
    mapped.invisibleTypeAnnotations = node.invisibleTypeAnnotations;
    return mapped;
  }

  public static void write(TryCatchBlockNode node, CustomDataOutput out, Function<LabelNode, Integer> labelToIndex) throws IOException {
    out.writeInt(labelToIndex.apply(node.start));
    out.writeInt(labelToIndex.apply(node.end));
    out.writeInt(labelToIndex.apply(node.handler));
    DataStreamHelper.writeUTFNullable(out, node.type);
    ListHelper.write(
            ListHelper.nullToEmpty(node.visibleTypeAnnotations),
            out,
            (a, out2) -> AnnotationNodeHelper.write(a, out2, labelToIndex)
    );
    ListHelper.write(
            ListHelper.nullToEmpty(node.invisibleTypeAnnotations),
            out,
            (a, out2) -> AnnotationNodeHelper.write(a, out2, labelToIndex)
    );
  }

  public static TryCatchBlockNode read(CustomDataInput in, Function<Integer, LabelNode> indexToLabel) throws IOException {
    LabelNode start = indexToLabel.apply(in.readInt());
    LabelNode end = indexToLabel.apply(in.readInt());
    LabelNode handler = indexToLabel.apply(in.readInt());
    String type = DataStreamHelper.readUTFNullable(in);
    List<TypeAnnotationNode> visibleTypeAnnotations = ListHelper.read(
            in,
            AnnotationNodeHelper::readTypeAnnotationNode
    );
    List<TypeAnnotationNode> invisibleTypeAnnotations = ListHelper.read(
            in,
            AnnotationNodeHelper::readTypeAnnotationNode
    );
    TryCatchBlockNode tryCatchBlockNode = new TryCatchBlockNode(start, end, handler, type);
    tryCatchBlockNode.visibleTypeAnnotations = visibleTypeAnnotations;
    tryCatchBlockNode.invisibleTypeAnnotations = invisibleTypeAnnotations;
    return tryCatchBlockNode;
  }
}
