 /* Test Input Description */  

 	// Polymorphism in Constructors & Virtual Method Dispatch 
 /* Test Input Description */  

class AG { 

	String v1; 
	public AG() { 
		v1 = "AG Constructor"; 
		System.out.println(v1); 

	}


	public String myToString() { 

		return "AG"; 


	}

	



}


class GA extends AG { 
	String d; 
	public GA() { 
		d = "GA Constructor"; 
		System.out.println(d); 

	}







}


public class Test1002 { 


public static void main ( String [] args ) { 

	AG a = new GA(); 


	System.out.println(a.myToString()); 


}



}