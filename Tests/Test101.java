
// Overriding 
class NewYorkUniversity101{

   public void classes(){
      System.out.println("OOPS class");
   }
}

class Courant101 extends NewYorkUniversity101{

   public void classes(){
      super.classes();	
      System.out.println("Java Class");
   }
}

public class Test101{

   public static void main(String args[]){
      NewYorkUniversity101 nyuCs = new Courant101();
      nyuCs.classes();
   }
}