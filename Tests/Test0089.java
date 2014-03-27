 /* Test Input Description */  
        // Mostly About Method Chaining 

 /* Test Input Description */  

class Ankit {
  Ankit m() { System.out.println("Ankit.m()"); return new Ankit(); }
  Ankit m(Ankit a) { System.out.println("Ankit.m(A)"); return a; }
}

class Goel extends Ankit {
  Ankit m() { System.out.println("Ankit.m()"); return new Ankit(); }
  Goel m(Goel b) { System.out.println("Goel.m(B)"); return b; }
  Ankit m(Ankit a) { System.out.println("Goel.m(Ankit)"); return a; }
}

public class Test0089 {
  public static void main(String[] args) {
      Ankit cc = new Ankit(); 

        cc.m().m().m().m().m();


        cc.m( new Ankit()).m().m().m().m().m();  



  }
}
