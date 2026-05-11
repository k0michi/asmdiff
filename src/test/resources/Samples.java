import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

class C1 {
}

class C2 {
}

@A1
class C3 {
}

@A2(42)
class C4 {
}

@A3("a")
@A3("b")
@A3("c")
@A3("d")
@A3("e")
@A3("f")
@A3("g")
@A3("h")
@A3("i")
@A3("j")
@A3("k")
class C5 {
}

@A3("a")
@A3("b")
@A3("c")
@A3("d")
@A3("e")
@A3("f")
@A3("g")
@A3("h")
@A3("i")
@A3("j")
@A3("k")
@A3("l")
class C6 {
}

class C7 {
  int f;
}

class C8 {
  int g;
}

@interface A1 {
}

@interface A2 {
  int value();
}

@Repeatable(A8.class)
@interface A3 {
  String value();
}

@interface A4 {
  Class<?> value();
}

@interface A5 {
  int[] value();
}

@interface A6 {
  String[] value();
}

@interface A7 {
  A6[] value();
}

@interface A8 {
  A3[] value();
}

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.ANNOTATION_TYPE, ElementType.PACKAGE, ElementType.TYPE_PARAMETER, ElementType.TYPE_USE, ElementType.MODULE, ElementType.RECORD_COMPONENT})
@interface A9 {
}

record R1() {
}

record R2(int x) {
}

record R3(int x, int y) {
}

record R4(int y) {
}

class C9 {
  int f1;

  @A9 void m1() {
    @A9 int a;

    if (f1 > 0) {
      @A9 int b = 1;
      a=2;
      System.out.println(b);
      String f = "Hello";
      System.out.println(f);
    } else {
      a = 2;
      System.out.println("Non-positive");
    }

    System.out.println(a);
  }
}

class C10 {
  void m1() {
    System.out.println("Hello");
  }
}

class C11 {
  void m1() {
    System.out.println("World");
  }
}