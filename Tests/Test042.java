class A {
  A m() { System.out.println("A.m()"); return new A(); }
  A m(A a) { System.out.println("A.m(A)"); return a; }
}

class B extends A {
  A m() { System.out.println("B.m()"); return new A(); }
  B m(B b) { System.out.println("B.m(B)"); return b; }
  A m(A a) { System.out.println("B.m(A)"); return a; }
}

public class Test042 {
  public static void main(String[] args) {
    A b = new A();
    b.m(new A()).m().m();
    B z = new B();
    z.m( new B()).m();
    z.m((A) z).m();
  }
}
