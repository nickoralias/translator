// Methods


public class Test105 {
public static void main(String arg[]){
	int a= 10;
	
	int b=20;
	
	swap(a,b);

	 System.out.println("a="+a+" "+"b= "+b);
	
	
}
static void swap(int a, int b){
	
	int temp;
	
	temp=a;
	a=b;

	b=temp;
	
	System.out.println("a="+a+" "+"b= "+b);
	
	
}

}
