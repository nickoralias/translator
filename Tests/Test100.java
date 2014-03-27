// Overriding 
class NewYorkUniversity{

   public void classes(){
      System.out.println("OOPS class");
   }
}

class Courant extends NewYorkUniversity{

   public void classes(){
      System.out.println("Java Class");
   }
}

public class Test100{

   public static void main(String args[]){
      NewYorkUniversity nyu = new NewYorkUniversity();
      NewYorkUniversity nyuCs = new Courant();

      nyu.classes();
     nyuCs.classes();
   }
}