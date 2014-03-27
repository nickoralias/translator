
Readme

Initial Configuration for Patches (  run these commands once to forever patch xtc.lang's JavaExternalAnalyzer to run with our program) 
	
	1.) Put all java files in xtc/src/oop
	 
	
	The Following Commands are Only run Once 
		2.) cd src/oop
		3.) make patchxtc 
		4.) cd ../..
		5.) make clean 
		6.) make 
		7.) cd src/oop 

Running the Translator (Always done in src/oop directory / Also with the assumption that you done a source setup.sh in xtc's root directory) 

	8.) java oop.Translator -translate Tests/Test#.java ( ex. java oop.Translator -translate Tests/Test1.java )

Compiling the Generated C++ Code (Always done in src/oop/cplusplusfiles)

	9.) cd cplusplusfiles 
          10.) g++ Method_Bod.cc main.cc java_lang.cc 

Running the C++ Code (Always done in src/oop/cplusplusfiles)
	
	11.) ./a.out 

Debugging / Extra Printout

	12.) If you would like to see under the hood, simply use the “-debug” command to see extra printouts like so:
		
		java oop.Translator -debug -translate Tests/Test#.java

----------------------------------------------------------x---------------------------------------------------------------------------

Note about Test Inputs 
1.) Homemade Test Cases are prefixed with either 100 or 00 such as Test0082.java or Test1004.java. Homemade Test cases have at the top a comment as such 
		
		/* Test Input Description **/ 
		// What is This Test Input trying to Test ? More Specifically what features like Polymorphism, Method Chaining, or Virtual 		  Method Dispatch …  are being emphasized 	
		/*Test Input Description **/ 

2.) Most of these Test inputs are either original ones that Professor  Wies gave us or tweaks of the ones that we given to us. However it is VERY IMPORTANT THAT YOU read the Test Input Descriptions for the Homemade Test cases & run our project on ALL THE TEST CASES THAT WE HAVE GIVEN YOU IN TESTS FOLDER you will see that we have the following features covered 

Features Covered 
1.) Virtual Method Dispatch 
2.) Everything about Single Dimensional Arrays / Calling Methods on the objects stored in the Arrays 
3.) Symbol Table Logic finding local variables / fields
4.) Method Chaining 
5.) Command Line Arguments 
6.) Separate Compilation( Header.h Method_Bod.cc main.cc) 
7.) Initializer Lists versus assignment to minimize Compilation times 
8.) For Statements, While Statements, If Statements 
9.) Constructors invoking Super Constructors 
10.) Overloading of Method Calls 
11.) Static Variables 
12) Calls to super in a sub class 

13.) Polymorphic Declarations/ Assignments 
14.) Correct Usage of Smart Pointers 


About the Tweaks 

The way I handled Overloading was to append the type of each argument to the method each one delimited by an "_". Therefore When looking at the expression statement when a method is being called in the main method declaration of the slightly modified Java AST  I would look at the node Arguments and for each one append the string like if i had in the Argument Node a  NewClassExpression within in the QualifiedIdentifier id append that child . If it was a Cast Expression again i would find the  type and append it. However the problem with passing  just a primary identifier was that Id had no way to retrieve the reference type in Expression Statement's node Arguments. So by changing the primary Identifier to a new Class Expression or adding a redundant cast I was able to append to the method name the type of that arguement. Nonetheless run the test inputs given

