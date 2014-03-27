/* Test Description **/ 

// This is testing Polymorphism in Constructors, VirtuaL Method Dispatch, For Loops, Array Initilization and Array METHOD CALLS 

/* Test Description **/ 


class ThreeSAT { 


	String Description; 

	public ThreeSAT () { 
			 Description = "3SAT IS NP COMPLETE";


		System.out.println(Description); 

	}

	public void Reduction() { 

		System.out.println("Using a Tablaeu one can show that 3SAT reduces to ");

	}

	public ThreeSAT THREESATDescription() { 
		System.out.println(" A boolean Formula that is in Conjunctive Normal Form"); 
		return new ThreeSAT(); 

	}





}


class CLIQUE extends ThreeSAT { 



	public CLIQUE() { 
			 Description = "An instance of 3SAT CAN BE SHOWN TO BE POLYNOMIALLY REDUCIBLE TO clique"; 


		System.out.println(Description); 

	}

	public void Reduction() { 

		System.out.println("The Reduction from 3SAT to clique mimics the variables and clauses in the boolean equation by conencting all the true literals in each clasue."); 

	}

	public CLIQUE CliqueDescription() { 
		System.out.println("A clique is a group of nodes in which each node is connected to every other node in the group"); 
		return new CLIQUE(); 

	}

	public CLIQUE inverseGraph() { 
		System.out.println("A graph with a k clique and n nodes has an inverse graph with n -k nodes"); 
		return new CLIQUE(); 
	}	


}


public class Test0066 { 


	public static void main ( String [] args ) { 
		

		ThreeSAT  B = new ThreeSAT(); 

		B.Reduction(); 


		CLIQUE A  = new CLIQUE(); 

		A.Reduction(); 


	//	ThreeSAT aa = new CLIQUE(); 

	//	aa.Reduction(); 


		ThreeSAT [] a = new ThreeSAT[4]; 

		for ( int i = 0; i < a.length; i++) { 


				a[i] = new ThreeSAT(); 

		}



		CLIQUE [] b = new CLIQUE[4];

		for ( int i = 0; i < b.length; i++) { 

				b[i] = new CLIQUE(); 


		}

		//A.CliqueDescription().CliqueDescription().CliqueDescription().inverseGraph();







	}





}