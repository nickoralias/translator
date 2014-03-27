
 /* Test Input Description */  
// Polymorphism in Constructors and Virtual Method Dispatch 
 /* Test Input Description */  

class AG1 { 


	String AGmember; 
	public AG1() { 

		AGmember = "AG1 Constructor"; 
		System.out.println(AGmember); 
	}



 	public void m() { 

 		System.out.println("AG1.m()"); 

 	}



}


class AG2 extends AG1 { 


String Ag2Member; 

public AG2() {
	
	Ag2Member = "AG2 Constructor"; 
	System.out.println(Ag2Member); 

}

	public void m() { 

	System.out.println("AG2.m()"); 

	}



}






public class Test10003 { 



	public static void main ( String [] args ) { 

	AG1 a = new AG1(); 

	AG1 b = new AG2(); 

	


	a.m();

	b.m();




	}



}