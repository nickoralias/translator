
 /* Test Input Description */  

 // Conditional Statements plus Virtual Method Dispatch 

 
 /* Test Input Description */  


class HardWare { 



	public void startTheMachine() { 

		System.out.println("Machine is On");

	}

	public void turnOffTheMachine() { 

		System.out.println("Machine is off"); 

	}



}


class Printer extends HardWare { 

	public void startTheMachine() { 

		System.out.println("Printer is on"); 

	}
	
	
	public void printPages() { 

		System.out.println("print the pages"); 

	}




}


public class Test1008 { 


	public static void main ( String [] args)  { 

			HardWare p1 = new HardWare();

			int B = 17; 

			p1.startTheMachine(); 

		//	p1.turnOffTheMachine(); 

			if ( B == 17) { 

				p1.turnOffTheMachine(); 

			}

			Printer p2 = new Printer(); 

			p2.startTheMachine(); 

			for ( int i = 0; i < B; i++ ) { 

				p2.printPages(); 

			}

	}



}