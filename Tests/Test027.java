


class A {
  int i;
  public A(int i) {
    this.i = i;
  }

  public int get() {
    return i;
  }
}

public class Test027 {
  public static void main(String[] args) {
    A[] as = new A[10];
    
    for(int i = 0; i < as.length; i++) {
      as[i] = new A(i);
    }

    for (int i = 0; i < as.length; i++) { 

      System.out.println(as[i].get());  


    }
   
  }
}
