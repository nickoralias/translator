//Has a relation 
 class A{
	
	
}
 class B{
 	String b; 
	B(){
		b = "In B"; 
		System.out.println(b);
	}
}
 
class C extends A{
	 B b = new B();
	
		
	} 

public class Test103 {
	public static void main(String args[]){
		C myC = new C();
		
	}

}
