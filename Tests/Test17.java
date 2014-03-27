class A {
  A self2;
  
  public A() {
    self2 = this;
  }

  public A self() { return self2; }

}

public class Test17 {
  public static void main(String[] args) {
    A a = new A();
    System.out.println(a.self().toString());
  }
}