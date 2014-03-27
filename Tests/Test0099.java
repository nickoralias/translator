

/* Test Input Description **/ 

// THIS TEST INPUT DEMONSTRATES THE USE OF METHOD CHAINING, Virtual Method Dispatch, For loops, and Arrays


/* Test Input Description **/ 

class Zarbon { 




	public Zarbon loyalty() { 
		System.out.println("Loyalty is to Frieza"); 
		return new Zarbon(); 

	}

	public void Description() { 
		System.out.println("A beast "); 


	}



}


class Vegeta extends Zarbon { 




	public void Description() { 
		System.out.println("A Brave Warrior "); 


	}


}


public class Test0099 { 


	public static void main ( String [] args ) { 

			Zarbon za = new Vegeta(); 

			za.Description(); 


			Zarbon [] cc = new Zarbon[4]; 

			
			for ( int i = 0; i < cc.length; i++) { 

					cc[i] = new Zarbon(); 


			}
			
			

			for ( int i = 0; i < cc.length; i++) { 

					cc[i].Description(); 	

			}
			
			za.loyalty().loyalty().loyalty().loyalty(); 
			


	}




}