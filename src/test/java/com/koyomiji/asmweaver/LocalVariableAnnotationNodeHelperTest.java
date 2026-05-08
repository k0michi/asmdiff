package com.koyomiji.asmweaver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.TypeReference;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableAnnotationNode;

import static com.koyomiji.asmweaver.LabelNodes.l0;
import static com.koyomiji.asmweaver.LabelNodes.l1;

class LocalVariableAnnotationNodeHelperTest {
  @Test
  void test_equals_0() {
    LocalVariableAnnotationNode node1 = new LocalVariableAnnotationNode(
            TypeReference.CLASS_TYPE_PARAMETER,
            TypePath.fromString("*"),
            new LabelNode[]{l0},
            new LabelNode[]{l1},
            new int[]{0},
            "Ljava/lang/String;"
    );
    LocalVariableAnnotationNode node2 = new LocalVariableAnnotationNode(
            TypeReference.CLASS_TYPE_PARAMETER,
            TypePath.fromString("*"),
            new LabelNode[]{l0},
            new LabelNode[]{l1},
            new int[]{0},
            "Ljava/lang/String;"
    );
    Assertions.assertTrue(LocalVariableAnnotationNodeHelper.equals(node1, node2));
  }
}
