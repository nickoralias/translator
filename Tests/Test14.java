class A {
  A some;

  public void printOther(A other) {
    System.out.println(other.toString());
    //System.out.println(other)
  }
  
}

public class Test14 {
  public static void main(String[] args) {
    
    A a = new A();
    
    A other = a.some;
    for ( int i = 0; i < 4; i++) { 

      System.out.println("AnkitGoel" + "goel"); 
    }
    a.printOther(other); // throws NullPointerException
  	
  }
}