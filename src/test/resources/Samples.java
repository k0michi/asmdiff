import java.lang.annotation.Repeatable;

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