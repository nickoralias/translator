
 /* Test Input Description */  

 		// Symbol Table Usage 
	 /* Test Input Description */  

class A { 
	
	String d; 
	

	public A(String f) { 

		d = f; 


	}

public String toString() { 


	return "A's to String Method"; 

}


public void myToString() { 


	System.out.println("A's myToString"); 

}

public String getd() {
	
	return d; 
}

public void setd(String e) { 
	d  = e; 
	
}

public void almostsetd(String e) { 
	String d; 

	d = e; 

}




}

public class Test1005 {
	

	public static void main ( String [] args) { 

		int i = 3; 
		i++; 
		--i; 
		i--; 
		
		while ( i < 10) { 

			System.out.println("AG" + "GA"); 
			i++; 

		}

		if ( i == 10 ) { 

			System.out.println("Reached 10"); 

		}


		A a = new A("AG");
		a.myToString();
		System.out.println(a.toString()); 


		a.almostsetd("goel"); 
		System.out.println(a.getd()); 

		a.setd("ankit"); 
		System.out.println(a.getd()); 

	}


}