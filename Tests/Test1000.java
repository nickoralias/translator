

 /* Test Input Description */  

    // Mostly Concerned with Overloading 

 /* Test Input Description */  

class Goel {
  void m(Object o1, Goel o2) { System.out.println("Goel.m(Object, Goel)"); }
  void m(Object a1, Object o2) { System.out.println("Goel.m(Object, Object)"); }
  void m(Goel o1, Goel a2) { System.out.println("Goel.m(Goel, Goel)"); }
   void m(Goel o1, Object a2) { System.out.println("Goel.m(Goel, Object)"); }
}

class Ankit extends Goel {
  void m(Object o1, Object o2) { System.out.println("Ankit.m(Object, Object)"); }
  void m(Ankit a1, Ankit o2) { System.out.println("Ankit.m(Ankit, Ankit)"); }
  void m(Ankit o1, Goel a2) { System.out.println("Ankit.m(Ankit, Goel)"); }
    void m(Ankit o1, Object a2) { System.out.println("Ankit.m(Ankit, Object)"); }

}

public class Test1000 {
  public static void main(String[] args) {
    Ankit b = new Ankit();
    b.m(new Ankit(), (Object) b);
    b.m( new Goel(), new Object());
    b.m((Object) b, new Object());
  }
}
