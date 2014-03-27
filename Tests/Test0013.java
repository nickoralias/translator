 /* Test Input Description */  


// Test Input is mostly concerned with Overloading 


  /* Test Input Description */  


class Shape { 

		
	public void printVertices(String m) { 

		System.out.println("A shape with one vertices"); 

	}



	public void printVertices(String m, String d) {

		System.out.println("A shape with Two Vertices"); 


	}

	public void printVertices(String m , String d, String e) { 


		System.out.println("A shape with Three Vertices"); 

	}

	public void printVertices(String m, String d, String e , String f) { 


		System.out.println("A shape with Four Vertices"); 

	}




}


class Hexagon extends Shape { 


	public void printVertices(String m) { 

		System.out.println("A Hexagon has more than one vertices"); 

	}



	public void printVertices(String m, String d) {

		System.out.println("A hexagon has more than  Two Vertices"); 


	}

	public void printVertices(String m , String d, String e) { 


		System.out.println("A Hexagon with Three Vertices"); 

	}

	public void printVertices(String m, String d, String e , String f) { 


		System.out.println("A Hexagon has more than Four Vertices"); 

	}

	public void printHexagonVerticesSize() { 

		System.out.println("A hexagon has 6 vertices"); 


	}



}



public class Test0013 { 



	public static void main ( String [] args ) { 


		Shape s1 = new Shape();

		s1.printVertices("one"); 

		s1.printVertices("one", "two"); 

		s1.printVertices("one", "two", "three"); 

		s1.printVertices("one", "two", "three", "four");


		Shape s2 = new Hexagon(); 

		s2.printVertices("one"); 

		s2.printVertices("one", "two"); 

		s2.printVertices("one", "two", "three"); 

		s2.printVertices("one", "two", "three", "four");

		//s2.printHexagonVerticesSize(); 

		Hexagon v1 = new Hexagon();

		v1.printHexagonVerticesSize(); 

	}




}