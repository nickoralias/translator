 /* Test Input Description */  
 // Simple Test for Virtual Method Dispatch 

 /* Test Input Description */  

class AG { 


	public String myToString() { 

		return "AG"; 


	}

	



}


class GA extends AG { 





	public String myToString() { 


		return "GA"; 

	}





}


public class Test1001 { 


public static void main ( String [] args ) { 

	AG a = new GA(); 


	System.out.println(a.myToString()); 


}



}