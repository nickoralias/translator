// IS A Relation 

class NewYorkUniversity102{

}

class Courant102 extends NewYorkUniversity102{

}

class CSDept extends Courant102{

}


public class Test102{

   public static void main(String args[]){
      NewYorkUniversity102 nyu = new NewYorkUniversity102();
      Courant102 cims = new Courant102();
      CSDept cs = new CSDept();	
      System.out.println(cims instanceof NewYorkUniversity102);
      System.out.println(cs instanceof Courant102);
      System.out.println(cs instanceof CSDept);
	 
   }
}