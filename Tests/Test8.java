class A {
  String a;
  
  public A() {
    a = "A";
    System.out.println(a); 
  }
  
}

class B extends A {
  String b;

  public B() {
    b = "B";
    a = "B";
    System.out.println(a);
  }
}


public class Test8 {
  public static void main(String[] args) {
    B b = new B();
  }
}