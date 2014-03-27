/* Test Description **/ 

// arrays and for loops 
/* Test Description **/ 
class SubsetSum { 
	
	String descriptor; 
	public SubsetSum() { 

			descriptor = "Tally a subset of items in a set and get their value according to some target";

			System.out.println(descriptor); 


	}



	

	public String retDescriptor() { 

		return "SubsetSum is polynomially reducible to 3SAT"; 

	}




}

public class Test00000 { 

public static void main ( String [] args ) { 

		SubsetSum [] z = new SubsetSum[5];


		for ( int i  = 0; i < z.length; i++) { 

			z[i] = new SubsetSum(); 


		}
		

		for ( int i = 0; i < z.length; i++) { 


			System.out.println(z[i].retDescriptor()); 

		}
	

}

}