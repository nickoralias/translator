class A {
  
  int i; 

  public A(int i) { 
    this.i = i; 
  }

  public int get() { 
    return i; 
  }
  
  
  public String toString() { 

    return "A"; 
  }
  
}

public class Test028 {
  public static void main(String[] args) {
    A[] as = new A[10];
     
    System.out.println(as.getClass().toString());
  }
}
