package com.koyomiji.asmweaver.exp;

import com.koyomiji.asmweaver.util.AutoIncrementBiHashMap;
import org.objectweb.asm.tree.*;

import java.util.Comparator;

public class Sort {
  // aspaはmethods/fieldsの順番を維持しない
  public static void sortMembers(ClassNode classNode) {
    classNode.interfaces.sort(String::compareTo);

    classNode.fields.sort((a, b) -> Comparator.comparing((FieldNode f) -> f.name)
            .thenComparing(f -> f.desc)
            .compare(a, b));

    classNode.methods.sort((a, b) -> Comparator.comparing((MethodNode m) -> m.name)
            .thenComparing(m -> m.desc)
            .compare(a, b));

    if (classNode.innerClasses != null) {
      classNode.innerClasses.sort((a, b) -> Comparator.comparing((InnerClassNode c) -> c.name)
              .thenComparing(c -> c.outerName)
              .thenComparing(c -> c.innerName)
              .thenComparing(c -> c.access)
              .compare(a, b));
    }

    for (MethodNode method : classNode.methods) {
      if (method.exceptions != null) {
        method.exceptions.sort(String::compareTo);
      }

      AutoIncrementBiHashMap<LabelNode> labels = new AutoIncrementBiHashMap<>();

      for (AbstractInsnNode insn : method.instructions) {
        if (insn instanceof LabelNode) {
          labels.get((LabelNode) insn);
        }
      }

      method.tryCatchBlocks.sort(
              (a, b) -> Comparator.comparing((TryCatchBlockNode t) -> labels.get(t.start))
                      .thenComparing(t -> labels.get(t.end))
                      .thenComparing(t -> labels.get(t.handler))
                      .thenComparing(t -> t.type, Comparator.nullsFirst(String::compareTo))
                      .compare(a, b)
      );

      if (method.localVariables != null) {
        method.localVariables.sort(
                (a, b) -> Comparator.comparing((LocalVariableNode v) -> v.name)
                        .thenComparing(v -> v.desc)
                        .thenComparing((LocalVariableNode v) -> v.signature,  Comparator.nullsFirst(String::compareTo))
                        .thenComparing(v -> labels.get(v.start))
                        .thenComparing(v -> labels.get(v.end))
                        .thenComparing(v -> v.index)
                        .compare(a, b)
        );
      }
    }
  }
}
