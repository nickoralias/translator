 /* Test Input Description */  
 // For Loop combined with Arrays 

 /* Test Input Description */  


class Goel {
  
 

 
  
  
  public String to1String() { 

    return "Goel toSstring()"; 
  }
  
}

public class Test028 {
  public static void main(String[] args) {
    Goel[] as = new Goel[10];


    for ( int i = 0; i < as.length; i++) { 

      as[i] = new Goel(); 

    }

    for ( int i = 0; i < as.length; i++) { 
     
      System.out.println(as[i].toString());
    }

  }
}
