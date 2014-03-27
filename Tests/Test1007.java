//Has a relation 
 class Goel{
 	String s; 
	Goel() { 
		s = "Goel Sahn";
		System.out.println(s); 
	}
		
}
 
 
class C extends Goel {
	 	String c; 
		C() { 
			c = "IN c"; 
			System.out.println(c); 
		}

		public String m() { 

			return "C"; 
		}
		
	} 

public class Test1007 {
	public static void main(String args[]){
		C myC = new C();

		int i = 2; 

		if ( i == 2 ) { 

			System.out.println(myC.m()); 
		}
		
	}

}
