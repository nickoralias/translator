class A {
  public A self;
  
  public A() {
    self = this;
  }
}

public class Test9 {
  public static void main(String[] args) {
    A a = new A();
    System.out.println(a.self.toString());
  }
}