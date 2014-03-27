 /* Test Input Description */  

 // Mostly Converned with Single Dimensional Arrays 

 /* Test Input Description */  

class Goel { 


	public Goel() { 
		
	}
	public void printArrayContents() { 

	System.out.println("Ankit Goel Cross Road "); 

	}

	public void printVillianCharacter() { 

		System.out.println("The Mighty Frieza"); 

	}


}


class Ankit extends Goel { 






	public void printVillianCharacter() { 

		System.out.println("Majin Buu"); 

	}





}

public class Test0011 { 


	public static void main ( String [] args) { 

		Goel[] v1 = new Goel[10]; 

		v1[0] = new Goel(); 

		v1[0].printArrayContents(); 

		v1[0].printVillianCharacter(); 

//		Ankit a = new Ankit(); 
		//v1[1] = new Goel(); 
		
		v1[1] = new Ankit(); 
		v1[1].printVillianCharacter(); 

		
	}



}