package com.koyomiji.asmweaver;

import com.koyomiji.asmweaver.util.AutoIncrementBiHashMap;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.TypeReference;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ClassNodeHelperTest {
  static List<ClassNode> generateUnique() {
    List<ClassNode> list = new ArrayList<>();
    ClassNode temp = new ClassNode();
    temp.visit(0, 0, "A", null, "java/lang/Object", null);
    list.add(temp);

    temp = new ClassNode();
    temp.visit(1, 0, "A", null, "java/lang/Object", null);
    list.add(temp);

    temp = new ClassNode();
    temp.visit(0, 1, "A", null, "java/lang/Object", null);
    list.add(temp);

    temp = new ClassNode();
    temp.visit(0, 0, "B", null, "java/lang/Object", null);
    list.add(temp);

    temp = new ClassNode();
    temp.visit(1, 0, "A", "A", "java/lang/Object", null);
    list.add(temp);

    temp = new ClassNode();
    temp.visit(1, 0, "A", null, null, null);
    list.add(temp);

    temp = new ClassNode();
    temp.visit(1, 0, "A", null, "java/lang/Object", new String[]{"java/io/Serializable"});
    list.add(temp);

    temp = new ClassNode();
    temp.visit(0, 0, "A", null, "java/lang/Object", null);
    temp.sourceFile = "A";
    list.add(temp);

    temp = new ClassNode();
    temp.visit(0, 0, "A", null, "java/lang/Object", null);
    temp.sourceDebug = "A";
    list.add(temp);

    temp = new ClassNode();
    temp.visit(0, 0, "A", null, "java/lang/Object", null);
    temp.module = new ModuleNode("A", 0, "version");
    list.add(temp);

    temp = new ClassNode();
    temp.visit(0, 0, "A", null, "java/lang/Object", null);
    temp.outerClass = "A";
    list.add(temp);

    temp = new ClassNode();
    temp.visit(0, 0, "A", null, "java/lang/Object", null);
    temp.outerMethod = "a";
    list.add(temp);

    temp = new ClassNode();
    temp.visit(0, 0, "A", null, "java/lang/Object", null);
    temp.outerMethodDesc = "()V";
    list.add(temp);

    temp = new ClassNode();
    temp.visit(0, 0, "A", null, "java/lang/Object", null);
    temp.visibleAnnotations = List.of(new AnnotationNode("LA;"));
    list.add(temp);

    temp = new ClassNode();
    temp.visit(0, 0, "A", null, "java/lang/Object", null);
    temp.invisibleAnnotations = List.of(new AnnotationNode("LA;"));
    list.add(temp);

    temp = new ClassNode();
    temp.visit(0, 0, "A", null, "java/lang/Object", null);
    temp.visibleTypeAnnotations = List.of(
            new TypeAnnotationNode(
                    TypeReference.CLASS_TYPE_PARAMETER,
                    TypePath.fromString("*"),
                    "LA;"
            )
    );
    list.add(temp);

    temp = new ClassNode();
    temp.visit(0, 0, "A", null, "java/lang/Object", null);
    temp.invisibleTypeAnnotations = List.of(
            new TypeAnnotationNode(
                    TypeReference.CLASS_TYPE_PARAMETER,
                    TypePath.fromString("*"),
                    "LA;"
            )
    );
    list.add(temp);

    temp = new ClassNode();
    temp.visit(0, 0, "A", null, "java/lang/Object", null);
    temp.innerClasses = List.of(
            new InnerClassNode("A", "B", "C", 0)
    );
    list.add(temp);

    temp = new ClassNode();
    temp.visit(0, 0, "A", null, "java/lang/Object", null);
    temp.nestHostClass = "A";
    list.add(temp);

    temp = new ClassNode();
    temp.visit(0, 0, "A", null, "java/lang/Object", null);
    temp.nestMembers = List.of(
            "A"
    );
    list.add(temp);

    temp = new ClassNode();
    temp.visit(0, 0, "A", null, "java/lang/Object", null);
    temp.permittedSubclasses = List.of(
            "A"
    );
    list.add(temp);

    temp = new ClassNode();
    temp.visit(0, 0, "A", null, "java/lang/Object", null);
    temp.recordComponents = List.of(
            new RecordComponentNode("component", "I", null)
    );
    list.add(temp);

    temp = new ClassNode();
    temp.visit(0, 0, "A", null, "java/lang/Object", null);
    temp.fields = List.of(
            new FieldNode(0, "field", "I", null, null)
    );
    list.add(temp);

    temp = new ClassNode();
    temp.visit(0, 0, "A", null, "java/lang/Object", null);
    temp.methods = List.of(
            new MethodNode(0, "method", "I", null, null)
    );
    list.add(temp);

    return list;
  }

  @Test
  void test_equals() {
    TestUtils.verifyEquals(ClassNodeHelperTest::generateUnique, ClassNodeHelper::equalsNormalizeLabels);
  }

  @Test
  void test_hashCode() {
    TestUtils.verifyHashCode(ClassNodeHelperTest::generateUnique, ClassNodeHelper::hashCodeNormalizeLabels);
  }

  @Test
  void test_readWrite_roundTrip() throws IOException {
    AutoIncrementBiHashMap<LabelNode> labels = new AutoIncrementBiHashMap<>();

    TestUtils.verifyRoundTrip(
            ClassNodeHelperTest::generateUnique,
            (node, stream) -> ClassNodeHelper.write(node, stream, labels::get),
            stream -> ClassNodeHelper.read(stream, labels::getKey),
            ClassNodeHelper::equalsNormalizeLabels
    );
  }
}
