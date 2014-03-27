package oop;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import xtc.lang.JavaFiveParser;
import xtc.lang.CParser;
import oop.JavaPrinter;
import xtc.lang.CPrinter;
import xtc.lang.JavaAstSimplifier;

import xtc.parser.ParseException;
import xtc.parser.Result;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;
import java.util.*; 
import xtc.util.Tool;
import xtc.util.SymbolTable;

import oop.GetDependencies;
import oop.InheritanceHandler;
import oop.SymbolTableHandler;
import oop.ASTConverter;
import oop.CPPrinter;
import java.io.*; 

/**
 * A translator from (a subset of) Java to (a subset of) C++.
 */
public class Translator extends xtc.util.Tool {

  /** global debug value, initialized to no debugging */
  public static boolean DEBUG = false;
    
    
  /** Create a new translator. */
  public Translator() {
    // Nothing to do.
  }

  public String getName() {
    return "Java to C++ Translator";
  }

  public String getCopy() {
    return "(C) 2013 Group Group";
  }

  public void init() {
    super.init();

    // Declare command line arguments.
    runtime.
      bool("printJavaAST", "printJavaAST", false, "Print Java AST.").
      bool("printJavaCode", "printJavaCode", false, "Print Java code.").
      bool("countMethods", "countMethods", false, "Count all Java methods.").
      bool("translate", "translate", false, "Translate from Java to C++.").
      bool("debug", "debug", false, "Extra debug printout parameter.").
      bool("findDependencies", "findDependencies", false, "Find all Dependencies of given Java file.");
  }

  public void prepare() {
    super.prepare();

    // Perform consistency checks on command line arguments.
  }

  public File locate(String name) throws IOException {
    File file = super.locate(name);
    if (Integer.MAX_VALUE < file.length()) {
      throw new IllegalArgumentException(file + ": file too large");
    }
    return file;
  }

  public Node parse(Reader in, File file) throws IOException, ParseException {
    JavaFiveParser parser =
      new JavaFiveParser(in, file.toString(), (int)file.length());
    Result result = parser.pCompilationUnit(0);
    return (Node)parser.value(result);
  }

  public Node Cparse(Reader in, File file) throws IOException, ParseException {
    CParser parser =
      new CParser(in, file.toString(), (int)file.length());
    Result result = parser.pTranslationUnit(0);
    return (Node)parser.value(result);
  }

  public void process(Node node) {
    if (runtime.test("printJavaAST")) {
      runtime.console().format(node).pln().flush();
    }
    
      
    if (runtime.test("debug")) {
        DEBUG = true;
    }
      

    if (runtime.test("printJavaCode")) {
      new CPrinter(runtime.console()).dispatch(node);
      runtime.console().flush();
    }

      
      // Create a hash map
     final  HashMap hm = new HashMap();
      // Put elements to the map  
      
      
    if (runtime.test("countMethods")) {
      new Visitor() {
        private int count = 0;

        public void visitCompilationUnit(GNode n) {
          visit(n);
          runtime.console().p("Number of methods: ").p(count).pln().flush();
        }

        public void visitMethodDeclaration(GNode n) {
          runtime.console().p("Name of node: ").p(n.getName()).pln();
          runtime.console().p("Name of method: ").p(n.getString(3)).pln();
          visit(n);
          count++;
        }

        public void visit(Node n) {
          for (Object o : n) if (o instanceof Node) dispatch((Node) o);
        }

      }.dispatch(node);
    }
      
 
    if (runtime.test("translate")){
        
        runtime.console().pln("Begin translation...\n").flush();
        
        /*
         * This finds and resolves all dependencies within the java file
         * It basically looks up all the addresses of every file, then 
         * quickly creates a java AST for each individual dependency.
         * We will translate each individual java AST in a little while.
         */
        runtime.console().pln("Finding and resolving dependencies...\n").flush();
        GNode[] astArray = new GNode[50]; //arbitrary size
        astArray[0] = (GNode) node; //0th spot is the original java file's AST
        GetDependencies dependencyHandler = new GetDependencies();
        try{
        	astArray = dependencyHandler.getFile(); //store the array of java ASTs
        }
        catch (IOException e){}
        catch (ParseException e){ }
        
		astArray[0] = (GNode)node; //just to ensure we have the original java ast in slot 0 still
          
		  
		/*
		 * This copies over every array so that we can always refer to 
		 * an array that contains copies of the original unsimplified
		 * java ASTs. This is needed so that inheritance handler works correctly.
		 * We utilize InheritanceHandler's STATIC method deepCopy(GNode n) to 
		 * make this happen correctly.
		 */
		GNode[] unsimplified = new GNode[50]; //create the blank array to hold unsimplified java ASTs
		
		for(int i = 0; i<unsimplified.length; i++){
			unsimplified[i] = InheritanceHandler.deepCopy(astArray[i]); //create a deep copy of each java AST and store it in unsimplified
		}
		
		
		
		/*
		 * This builds the symbol table using a simplified version of the
		 * original java AST. This is for use in AST Converter.
		 */
        runtime.console().pln("Building symbol table...\n").flush();
        new JavaAstSimplifier().dispatch((GNode)node);
        final SymbolTable table = new SymbolTable(); //empty symbol table
        new SymbolTableHandler(runtime, table).dispatch((GNode)node); //create the table
        if(DEBUG){
            runtime.console().pln("DEBUG: Printing symbol table...\n").flush();
            table.current().dump( runtime.console() ); //print dump the table into console
        }
        runtime.console().pln().pln().flush();
         
        
        
        
        
        /*
         * This creates the class hierarchy of data layouts and vtables.
         * We run it on the unsimplified array to avoid losing
         * our constructor header declarations!
         */
        runtime.console().pln("Building vtables and data-layouts for C++ ASTs...\n").flush();
        InheritanceHandler layout = new InheritanceHandler(unsimplified, runtime.console());
        if(DEBUG){
            runtime.console().pln("DEBUG: Printing out ASTs for all class Data Layouts and Virtual Tables...\n").flush();
            runtime.console().format(layout.getClassTree()).pln().pln().pln().flush();
        }
        
        
        
        
     
        

        /*
         * This creates our beautiful header.h file
         * ASTConverter will make sure we #include it in the .cc file.  
         */
        runtime.console().pln("Creating header file...\n").flush();
        GNode createCplusplusHeader = layout.getClassTree();
        CreateCplusplusHeader getHeader = new CreateCplusplusHeader(createCplusplusHeader); 
        runtime.console().pln("Header file can now be found in cplusplusfiles directory.\n").pln().pln().pln().flush();
        
        
        
        /*
         * This translates the simplified java AST into C++
         * The output of this will be printed in to the class.cc file!
         */
        runtime.console().pln("Translating body...\n").pln().pln().pln().flush();
        GNode [] ccAstArray = new GNode [50];        
        for(int i=0 ; i<astArray.length ; i++)
        {
        	if(astArray[i]!=null)
        	{
        		ASTConverter ccConverter = new ASTConverter(astArray[i], layout, table, runtime.console() );
        		ccConverter.createCCTree();
        		ccAstArray[i] = ccConverter.getCCTree();
        		if(DEBUG){
                    runtime.console().pln("DEBUG: Printing out hybrid Java/C++ AST...\n").flush();
                    runtime.console().format(ccAstArray[i]).pln().flush();
                }
        	}
        }
        
      
        /*
         * This is where we will create the .cc file with correct C++ syntax.
         * Final step woohoo!
         */
        runtime.console().pln("Siphoning output to .cc files...").pln().pln().pln().flush();
        if(DEBUG) {
            runtime.console().pln("DEBUG: Printing out method body AST..").flush();
            runtime.console().format(node).pln().pln().pln().flush();
        }
        new JavaPrinter(runtime.console()).dispatch(node);
        runtime.console().flush();
     
        
        
        runtime.console().pln("... the translation is now finished! Please check src/oop/cplusplusfiles for your translated files. \n").flush();
	
       
      
      }


  }

  /**
   * Run the translator with the specified command line arguments.
   *
   * @param args The command line arguments.
   */
  public static void main (String [] args)
  {
	  Translator translator = new Translator();
	  translator.run(args);
  }
}