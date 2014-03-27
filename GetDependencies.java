package oop;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import xtc.lang.JavaFiveParser;
import xtc.lang.JavaPrinter;

import xtc.parser.ParseException;
import xtc.parser.Result;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;

import xtc.util.Tool;

/**
 * Finds where scopes begin and end.
 */
public class GetDependencies extends Tool {
    
    
    public GetDependencies() {
        // Nothing to do.
    }
    
    public String getName() {
        return "";
    }
    
    public String getCopy() {
        return "Team";
    }
    
    public void init() {
        super.init();
        
      
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
    
    public GNode parse(Reader in, File file) throws IOException, ParseException {
        JavaFiveParser parser =
        new JavaFiveParser(in, file.toString(), (int)file.length());
        Result result = parser.pCompilationUnit(0);
        return (GNode)parser.value(result);
    }


    String[] packageStatement = new String[10]; 
    String[] files = new String[100];
    int startfiles = 0;
    public void process(Node node) {
	
	
	new Visitor() {
	    
        
            
        public GNode[] visitImportDeclaration(GNode n) throws IOException, ParseException{
		// runtime.console().p("This is an import statement.").p(n.getNode(1).toString()).pln().flush();
		
		String fileToImport = "";
		for (int i = 0; i < 3; i++){
		    fileToImport += "/" + ((String)(n.getNode(1).get(i)));
		    System.out.println(n.getNode(1).get(i)); 
		    
		}
		fileToImport += ".java";
		files[startfiles] = fileToImport;
		
		runtime.console().p(files[startfiles]).pln().flush();
		startfiles++;
		visit(n);
		
		return getFile(); 
		
	    }
        /*
         * @param n Get Depenedencies from the given package using the node. all the reference types that 
            become available after being specified in a certain package can now be used 
         * 
         */
	    public void visitPackageDeclaration(GNode n) {
            runtime.console().p((String)n.getNode(1).get(0) + "   " + (String)n.getNode(1).get(1) + "  " + (String)n.getNode(1).get(2) ).pln().flush();

            packageStatement[0] = ((String)n.getNode(1).get(0)); 
            packageStatement[1] = ((String)n.getNode(1).get(1)); 
            packageStatement[2] = ((String)n.getNode(1).get(2)); 

            visit(n); 
		
	    }
	    
        
      
	    public void visit(Node n) {
		for (Object o : n) if (o instanceof Node) dispatch((Node) o);
	    }
            
	}.dispatch(node);
    }
    
    
    



    public GNode[] getFile() throws IOException, ParseException  {
	GNode tree[] = new GNode[500];
	
	for(int i = 1; i < startfiles; i++) {
	    {
		
				        
	//	System.out.println("Dependency file: " + ((String)System.getProperty("user.dir")) + "/src" + files[i]);
	        	File file = locate(((String)System.getProperty("user.dir")) + "/src" + files[i]);
						Reader in = runtime.getReader(file);
						tree[i] = parse(in, file);
									
				
				}
			}
			return tree;
	    } 
      
    public static void main(String[] args) {
	System.out.println();
       System.out.println("NOTE: This does not deal with import declarations that end in *");
        new GetDependencies().run(args);
		
    }
    
}
