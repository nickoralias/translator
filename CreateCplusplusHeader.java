package oop;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import xtc.lang.JavaFiveParser;
import xtc.lang.CParser;
import xtc.lang.JavaPrinter;
import xtc.lang.CPrinter;
import java.util.regex.*; 
import xtc.parser.ParseException;
import xtc.parser.Result;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;
import java.util.*; 
import xtc.util.Tool;

import oop.GetDependencies;
import oop.InheritanceHandler;
import oop.ASTConverter;
import oop.CPPrinter;
import java.io.*; 




/* Created by Ankit Goel**/
public class CreateCplusplusHeader extends xtc.util.Tool { 
    
    final GNode classTree;
    
    GNode createCplusplusHeader; 

    
    
    public CreateCplusplusHeader(GNode n) { 
		
        classTree  = n; 
        
            /*Debug**/ 
        // p1.println(classTree.size());
        final PrintWriter p1; 
        final GNode[] containsRelevantClasses;
        final ArrayList<String> stringClasses = new ArrayList<String>();
        final ArrayList<Integer> countClasses = new ArrayList<Integer>();
        final ArrayList<Integer> countChildren = new ArrayList<Integer>();
        final GNode stac = GNode.create("Holder"); 
        
        File headerFile; 
        
        File newDirectory;
        
        try { 
            
            newDirectory = new File("cplusplusfiles"); 
            newDirectory.mkdir(); 
            headerFile = new File("cplusplusfiles", "Header.h"); 
            headerFile.createNewFile(); 
            p1 = new PrintWriter(headerFile); 
            
                /*Remove comments below to Debug**/
            // p1.println(classTree.size());
            // p1.println(containsRelevantClasses.size());
            // p1.println(countClasses);
            //  p1.println(countChildren);
            
            p1.println("// Ankit Goel's Header"); 
            p1.println("#pragma once");
            p1.println();
            p1.println("#include <stdint.h>");
            p1.println("#include <string>"); 
            p1.println(); 
            p1.println("using namespace java::lang;");
            p1.println("// Foward Declarations "); 
            p1.println();
                        
                        /* Remove comments below to Debug**/
            //  String s = classTree.getNode(1).getNode(0).getNode(0).getName();
            //  p1.println(s);
            //  p1.flush();
            //  p1.close();
            
            new Visitor() {
                int counter22 = 0; 

                public void visitClass(GNode n ) { 
                    counter22++; 
                                    
                    countChildren.add(n.size()); 
                    countClasses.add(counter22);
                

                    visit(n); 
                }
            
                public void visitClassHeaderDeclaration(GNode n) { 
                
                    stac.add(n);
                    
                    /*Remove comments below to Debug**/
                    //p1.println(n.getNode(0).getName()); 
                    //containsRelevantClasses[counter22] = (GNode) n;
                
                }
            
                public void visit(Node n) {
                    for (Object o : n) if (o instanceof Node) dispatch((Node) o);
                }
            
            }.dispatch(classTree);
        
           /*Remove comments below to Debug**/ 
           // p1.println(stac.size()); 
          
            String globalVariableArrayCheck;
           for ( int b = 4; b < stac.size(); b++) { 
            
                createCplusplusHeader = (GNode)stac.getNode(b); 
                GNode vMethods = (GNode) createCplusplusHeader.getNode(0);
                //p1.println(createCplusplusHeader.getName());
                /*Remove comments below to Debug**/
                // p1.println(vMethods.getName()); 
                // We need a mapping of methodName to accessibility
                
                final HashMap<String,String> mNAccess = new HashMap<String,String>(); 
            
                /*Remove comments below to Debug**/
                // We need a mapping to methodName to whetherItsStatic
                // p1.println(mNAccess);
                // p1.println(mNAccess.get("goel")); 
            
               final GNode constructorPrinter = GNode.create("ConstructorPrinter");
            
               
            final ArrayList<Integer> getCounter = new ArrayList<Integer>(); 
            
            new Visitor() {
                int counter2 = 0; 
                public void visitVirtualMethodDeclaration(GNode n ) { 
                    counter2++; 
                    getCounter.add(counter2); 
                   
                }
                
                public void visit(Node n) {
                    for (Object o : n) if (o instanceof Node) dispatch((Node) o);
                }
                
            }.dispatch(vMethods);


            
            int startHere = getCounter.get(getCounter.size()-1) + 1; 
            
            /*Remove comments below to Debug**/
            // p1.println(startHere);
            
           final String className = vMethods.getNode(startHere).getNode(4).getNode(0).getString(0); 
            
          final  String plainClassName = className.substring(2, className.length()); 
           
               final GNode ARRAYTRACKER = GNode.create("ArrayTracker");
               
               // Need another dispatch for constructor heeader node    
               new Visitor() {
                   public void visitConstructorHeader(GNode n ) { 
                       
                       // Add each node 
                       if(n.getString(0).equals(plainClassName))
                           constructorPrinter.add(n);
                       
                   }
                   
                   
                   public void visitCustomArrayDeclaration(GNode n) { 
                       ARRAYTRACKER.add(n);
                   
                   }
                   
                   public void visit(Node n) {
                       for (Object o : n) if (o instanceof Node) dispatch((Node) o);
                   }
                   
               }.dispatch(createCplusplusHeader);
               // p1.println(constructorPrinter.size());
               // Find out when the virtual method declarations ends 
               
            /*Remove comments below to Debug**/
            // p1.println(getCounter); 
            
            p1.println("struct __" + plainClassName + ";");
            p1.println();
            p1.println("struct __" + plainClassName + "_VT;");
            p1.println();
            p1.println("typedef __rt::Ptr<" + "__" + plainClassName + "> " + plainClassName + ";");
            p1.println();
            p1.println("struct __" + plainClassName + " { " );
            p1.println();
            p1.println("    // The data layout for java.lang.plainClassName");  
            p1.println("      " + "__" + plainClassName + "_VT* __vptr;"); 
               
             
               
            // Get the Field Decl in this 
               
               final GNode fields = GNode.create("Fields");
               new Visitor() {
                   public void visitDataLayoutDeclaration(GNode n) { 
                       visit(n); 
                       
                       
                   }
                   
                   public void visitDataFieldList(GNode n) { 
                       
                       
                       fields.add(n); 
                       
                   } 
                   
                 
                   
                   public void visit(Node n) {
                       for (Object o : n) if (o instanceof Node) dispatch((Node) o);
                   }
                
               }.dispatch(createCplusplusHeader);
              // p1.println(fields.size());
                
               final ArrayList<String> privateOrpublic = new ArrayList<String>();
               final ArrayList<String> PrimitiveType = new ArrayList<String>(); 
               final ArrayList<String> Decl = new ArrayList<String>();
               
               new Visitor() {
                   public void visitDataFieldList(GNode n) { 
                       visit(n); 
                       
                       
                   }
                   
                   public void visitFieldDeclaration(GNode n) { 
                      
                       //if (n.getNode(0).size() > 0 ) 
                       //privateOrpublic.add(n.getNode(0).getNode(0).getString(0));
                       /*
                       if (n.getNode(0).getNode(0).getString(0).equals("static") && n.getNode(1).getNode(0).getString(0).equals("int") )
                           PrimitiveType.add("static const int32_t "); 
                       **/
                       if ( n.getNode(1).getNode(0).getString(0).equals("int") && n.toString().contains("static"))
                           PrimitiveType.add(" static int32_t"); 
                       else 
                           PrimitiveType.add(n.getNode(1).getNode(0).getString(0)); 
                      
                       
                          Decl.add(n.getNode(2).getNode(0).getString(0)); 

                          
                        /*
                      else 
                          Decl.add(n.getNode(2).getNode(0).getString(0) + "  = " +  n.getNode(2).getNode(0).getNode(2).getString(0)); 
                       **/
                   } 
                   
                   
                   
                   public void visit(Node n) {
                       for (Object o : n) if (o instanceof Node) dispatch((Node) o);
                   }
                   
               }.dispatch(fields);
               
               // Print out the Data Fields 
               for ( int c = 0; c < Decl.size(); c++) { 
                   
                   
                   p1.println();
                   p1.println( "     " + PrimitiveType.get(c) + " " + Decl.get(c) + ";");
                   
               }
               
                           
               List<String> typeOfParameter = new ArrayList<String>(); 
               List<String> DECLARATOR  = new ArrayList<String>();
               
               
               
               
               //p1.println(constructorPrinter);
               
               for ( int ccd = 0; ccd < constructorPrinter.size(); ccd++) {   
               
                   // There is more than one parameter 
                   if(constructorPrinter.getNode(ccd).getNode(1).size() >= 1) { 
                       
                       
                       GNode GETPARAMS = GNode.create("GETPARAMS"); 
                   
                       
                       GETPARAMS.add(constructorPrinter.getNode(ccd).getNode(1));
                       //p1.println(GETPARAMS);
                       // Now Go through the Formal Parameters 
                       
                       for ( int dcc = 0; dcc < GETPARAMS.size(); dcc++) { 
                           

                           
                           typeOfParameter.add(GETPARAMS.getNode(0).getNode(dcc).getNode(1).getNode(0).getString(0));
                          
                          
                           DECLARATOR.add(GETPARAMS.getNode(0).getNode(dcc).getString(3));
                           
                           
                       }
                   }  
                   
                   else {
                       
                       p1.println();
                       
                       p1.println("      " + className + "();");
                   
                   
                   }
                   
               }
               
               if ( DECLARATOR.size() >= 1 && typeOfParameter.size() >= 1) { 
                   p1.print(className + "( ");
                   for ( int goela = 0; goela < typeOfParameter.size(); goela++) { 
                      
                       if(goela != typeOfParameter.size()-1)
                           p1.print( typeOfParameter.get(goela) + "   " + DECLARATOR.get(goela) + ",");
                       else 
                           p1.print( typeOfParameter.get(goela) + "   " + DECLARATOR.get(goela) + ");");
                   }
                   
               }
               
               
            p1.println();
               
               if ( (constructorPrinter.size() == 0)) {  
               p1.println();
               p1.println("     "   +  "// The Constructor"); 
               p1.println();
               p1.println("          " +  "__" + plainClassName + "(); "); 
               }
               
               
            //Find Instance Methods of the class Basically go through vtMethodPointersList and 
            //  go through its children and check if the qualified identifier is the same as the clas name 
            
            // Store the names in a Arraylist type
            final  List<String> names = new ArrayList<String>(); 
            
            List<String> types2 = new ArrayList<String>();
            
            final List<Integer> indexx = new ArrayList<Integer>();
            
            final HashMap<Integer, String> checkForOtherSuperClass = new HashMap<Integer, String>(); 
               
            //final HashMap<String,String> checkForPredefinedMethods = new HashMap<String,String>(); 
            // Basically You need to consider this fact there will be however so many Constructors and you need to keep a tally to start it like that and    
            // Ignore those indices    
               
               
             final  List<Integer> constuctorIndex = new ArrayList<Integer>();  
               final  GNode constructors = GNode.create("CONSTRUCTOR"); 
               final String constructorNameGetter = plainClassName; 
             //  p1.println(constructors.size());
               //p1.println(constructorNameGetter);
            // Lets find out which methods are unique 
               
            new Visitor() {
                public int counter = 0; 
                public void visitVTConstructorDeclaration(GNode n) {
                    visit(n);
                }
                
                public void visitvtMethodPointersList(GNode n) {
                    
                    visit(n);
                    
                }
                public void visitvtMethodPointer(GNode n) { 
                    
                    counter++;
                    
                
                    
                    if( !(n.getNode(1).getString(1).equals("__Object")) && !(n.getString(0).equals("main_String")) ) { 
                        
                     
                       
                        //  constructorIndex.add(counter);
                        
                        
                        //  p1.println(n.getString(0)); 
                        
                        indexx.add(counter);
                        names.add(n.getString(0)); 
                        // There needs to be a check for the other than __Object && __SuperClass 
                        checkForOtherSuperClass.put(counter, n.getNode(1).getString(1)); 
                       
                      //  checkForPredefinedMethods.put(n.getString(0), n.getNode(1).getString(1)); 
                    
                    
                    
                    }   
                    else {
                        
                        checkForOtherSuperClass.put(counter, n.getNode(1).getString(1)); 
                        // constructors.add(n); 
                    }
                    
                }
                
                public void visit(Node n) {
                    for (Object o : n) if (o instanceof Node) dispatch((Node) o);
                }
                
            }.dispatch(vMethods);
               
               
            
              // p1.println(names);
             //  p1.println("Constructors" + constructors.size()); 
             //  p1.println(checkForOtherSuperClass);
            // System.out.println("ARRAY CONTENTS" + names); 
            // Now lets get the type of the method and store it in Types arraylist
            
            // Visit the Method Declarations of the Java AST and store the types in order into an array. Then 
            // store the corresponding names of the methods in another array then do matching to determine the 
            // the types of the method 
           // p1.println(checkForOtherSuperClass);
          
            for ( int i = 0; i < indexx.size(); i++ ) { 
                
                if( vMethods.getNode(indexx.get(i)).getGeneric(0) != null ) {   
                   
                    if(vMethods.getNode(indexx.get(i)).getNode(0).getName().equals("Type")) {
                      
                        // I think there only needs to be one check for a bool value the rest translate as is
                          if(vMethods.getNode(indexx.get(i)).getNode(0).getNode(0).getString(0).equals("boolean"))
                             types2.add("bool"); 
                          else
                             types2.add(vMethods.getNode(indexx.get(i)).getNode(0).getNode(0).getString(0));

                    }
                   
                    else 
                        types2.add("void"); 
                
                }
              
                else { 
                    
                    types2.add(" ");
               
                }
                
            }
            //p1.println(types2); 
           // p1.println(names); 
            // params are appended to the methods name 
            List<String> specialnames = new ArrayList<String>(); 
            
            // A single method name which is a string could essential map to however many Strings 
            Map<String, String> parameters = new  HashMap<String,String>(); 
            
            // Remove anything after _ in the method these are the parameters that are appended to it 
            for ( int i = 0; i < names.size(); i++ ) { 
                
                Pattern p = Pattern.compile("_");    
                
                Matcher m = p.matcher(names.get(i)); 
                
                if(m.find()) { 
                   // p1.println("FOUND"); 
                    
                    // p1.println(m.start());
                      
                      // ****** Changed 
                      //  specialnames.add(names.get(i).substring(0,m.start()));   

                      specialnames.add(names.get(i)); 
                     // Money.add(names.get(i)); 


                    parameters.put(specialnames.get(i), names.get(i).substring(m.start(),names.get(i).length())); 
                }
                else {
                    
                    specialnames.add(names.get(i)); 
                    // The hashmap needs to be consistent 
                    parameters.put(names.get(i), "ZeroParams"); 
                }
                
            } 
            //p1.println(names); 
            // p1.println(parameters);
            //p1.println(types2);
            //p1.println(parameters); 
            // Now print the instance methods using the types and names 
            p1.println("    // The instance methods of java.lang.plainClassName"); 
              // p1.println(specialnames);

            // Constructor Initializeer 
            p1.println("    static " + plainClassName + " init_Construct( "  + plainClassName + "); "); 



            for ( int i = 0; i < types2.size(); i++) { 
                
                
                    if ( parameters.get(specialnames.get(i)).equals("ZeroParams") && !(specialnames.get(i).equals(plainClassName)))
                        p1.println("    "  + "static " + types2.get(i) + " " + specialnames.get(i) + "( " + plainClassName + ")" + ";" ); 
                
                
                else { 
                        
                    if (  !(specialnames.get(i).equals(plainClassName)) ) {  
                    
                        ArrayList<Integer> getTheParameters = new ArrayList<Integer>(); 

                        p1.print("    "  + "static " + types2.get(i) + " " + specialnames.get(i) + "( " + plainClassName + " , " ); 
                    
                        Pattern pp = Pattern.compile("_");    
                    
                        Matcher mm = pp.matcher(parameters.get(specialnames.get(i))); 
                    
                        while (mm.find()) { 
                        
                            getTheParameters.add(mm.start());
                        
                        }
                    
                    
                        for ( int cc = 0; cc < getTheParameters.size(); cc++ ) { 
                        
                        
                            if ( cc != getTheParameters.size()-1) {
                            
                                p1.print( parameters.get(specialnames.get(i)).substring(getTheParameters.get(cc) + 1, getTheParameters.get(cc+1)) + " , "); 
                                
                            
                            }
                            else {
                                int length = parameters.get(specialnames.get(i)).length();
                                p1.print( parameters.get(specialnames.get(i)).substring(getTheParameters.get(cc) + 1, length) + ");");
                            
                            }
                        
                        }
                    
                
                        p1.println();
                    
                    }
                }
            }
            
            p1.println();
            p1.println("    // The Function returning the class Object representing java.lang.plainClassName " ); 
            p1.println("    static Class __class(); "); 
            p1.println("    static void init(  " + "__" + plainClassName+ "*" + "  );"); 
            p1.println();

              // Changes for Command line arguements
              if ( plainClassName.contains("Test")) { 

                p1.println("    static void main(__rt::Ptr<__rt::Array<String> > args);"); 

              }
            p1.println("    static __" + plainClassName + "_" + "VT " + "__vtable;"); 
            p1.println();
            p1.println(" };"); 
            p1.println();
            
            // Now print the Constructor taking into account which ones are implemented by the given class
            p1.println("struct __" + plainClassName + "_" + "VT" + "{");
            p1.println("    Class __isa;");
            
            // Introduce some logic to differentiate between new methods and predefined inherited method from Object
            List<String> arr1 = new ArrayList<String>(); 
            arr1.add("hashcode"); 
            arr1.add("equals"); 
            arr1.add("getClass"); 
            arr1.add("toString");
            arr1.add("getName");
            arr1.add("getSuperClass"); 
            arr1.add("isInstance"); 
            
           // Basically iterate through map and add any methods that have a type not equal to the 
            
            // You need to add the inherited types 
            p1.println("    void (*__delete)(__" + plainClassName + "*);");
            p1.println("    int32_t (*hashCode)(" + plainClassName + ");");
            p1.println("    bool (*equals)(" + plainClassName + " , " + "Object);"); 
            p1.println("    Class (*getClass)(" + plainClassName + ");"); 
            p1.println("    String (*toString) (" + plainClassName + ");"); 
            
            for ( int i = 0; i < names.size(); i++ ) { 
                
                if ( !(arr1.contains(names.get(i)))) {
                    
                    p1.println();
                    
                    
                    ArrayList<Integer> getTheParameters = new ArrayList<Integer>(); 
                    
                    
                    
                    
                    if ( parameters.get(specialnames.get(i)).equals("ZeroParams") && !(specialnames.get(i).equals(plainClassName)))
                        p1.println("    " + types2.get(i) + " (*" + specialnames.get(i) + ") (" + plainClassName  + ");" );
                    
                    else { 
                    
                        if ( !(specialnames.get(i).equals(plainClassName)) ) {    
                        
                            Pattern pp = Pattern.compile("_");    
                    
                            Matcher mm = pp.matcher(parameters.get(specialnames.get(i))); 
                    
                            while (mm.find()) { 
                                           
                                getTheParameters.add(mm.start());
                                
                            }
                                p1.print("    " + types2.get(i) + " (*" + specialnames.get(i) + ") (" + plainClassName + " , " );

                    
                        for ( int cc = 0; cc < getTheParameters.size(); cc++ ) { 
                            
                            
                            if ( cc != getTheParameters.size()-1) {
                                
                                p1.print( parameters.get(specialnames.get(i)).substring(getTheParameters.get(cc) + 1, getTheParameters.get(cc+1)) + " , "); 
                            
                            
                            }
                            else {
                                int length = parameters.get(specialnames.get(i)).length();
                                p1.print( parameters.get(specialnames.get(i)).substring(getTheParameters.get(cc) + 1, length) + ");");
                            
                            }
                            
                        }
                        
                        }
                        
                    //p1.println(getTheParameters);
                    }                                    
                }
            } 
           
            p1.println(); 
            p1.println(); 
            // Now the constructor initilization inlined in the header 
            p1.println("    __" + plainClassName + "_VT()");
            p1.println("    : __isa(__" + plainClassName + "::__class()),");
            
            List<String> getImplementation = new ArrayList<String>();
               
               int COUNT = 0; 
            
               
               
               // Are there any instance methods 
               for ( int i = 0; i < specialnames.size(); i++ ) {    
                   if ( !(specialnames.equals(plainClassName)))
                   
                       COUNT++;
               
               }
              // p1.println(COUNT);
               for ( int i = 0; i < arr1.size(); i++) { 
                
                   if( names.contains(arr1.get(i)) ) { 
                        



                       // Suppose a super class defines an Object method then this is wrong  so add an additional check
                    //if(checkForPredefinedMethods.get(arr1.get(i)).equals("__" + plainClassName))
                       getImplementation.add(plainClassName); 
                    //else    
                     //  getImplementation.add(checkForPredefinedMethods.get(arr1.get(i))); 


                   }   
                
                   else {
                    
                    getImplementation.add("Object"); 
                  
                  }
            }
              // Remove comment to Debug 
              //p1.println(getImplementation); 
              
               p1.println("    __delete(&__rt::__delete<__" + plainClassName + ">),");
            
            // Hardcoded May Need to Change in the final Phase 
            if ( getImplementation.get(0).equals("Object")) 
                p1.println("      hashCode((int32_t(*)(" + plainClassName + "))" + "&__" + getImplementation.get(0) + "::hashCode),"); 
            else 
                p1.println("      hashCode(&__" + plainClassName + "::hashCode),"); 
            
            
           
            if (getImplementation.get(1).equals("Object")) 
                p1.println("      equals((bool(*)(" + plainClassName + " , Object)) &__" + getImplementation.get(1) + "::equals), "); 
            
            else 
                p1.println("      equals(&__" + plainClassName + "::equals"); 
            
            if (getImplementation.get(2).equals("Object"))
                p1.println("      getClass((Class(*)(" + plainClassName + ")) &__" + getImplementation.get(2) + "::getClass), "); 
            
            else 
                p1.println("      getClass(&__" + plainClassName + ")");
            
            // Remember to Take care of the comma issue 
               if (getImplementation.get(3).equals("Object") /*|| !(getImplementation.get(3).equals("__" + plainClassName))**/) {
                   
                   if(COUNT != 0) 
                       p1.println("      toString((String(*)(" + plainClassName + ")) &__" + getImplementation.get(3) + "::toString), ");
                   else
                       p1.println("      toString((String(*)(" + plainClassName + ")) &__" + getImplementation.get(3) + "::toString) { ");

                    
               }    
                   
                   
            else {
                int x = names.size();
                boolean bat = false; 
                if((x==1) && (names.get(0).equals("toString")))
                    bat = true; 
                if(COUNT != 0 && !(bat) ) 
                    p1.println("      toString(&__" + getImplementation.get(3) + "::toString),"); 
                
                
                else
                    p1.println("      toString(&__" + getImplementation.get(3) + "::toString) {");
                
            }    
              //p1.println(names);
               
               
               
               
               
            //    
               
               
               for ( int uniqueNames = 0; uniqueNames < names.size(); uniqueNames++) {    
               
                   // Remove Unnecessary Methods
                   if(arr1.contains(names.get(uniqueNames))) {
                       names.remove(uniqueNames);
                       specialnames.remove(uniqueNames);
                       types2.remove(uniqueNames); 
                   }    
               }
               
             //p1.println(types2); 
            // p1.println(names);   
            // ADD Remaining Methods to implementation 
            for ( int i = 0; i < names.size(); i++) { 
                                  
                    if(!(arr1.contains(specialnames.get(i))) && checkForOtherSuperClass.get(i+6).equals(className) && (i==names.size()-1) && !(specialnames.get(i).equals(plainClassName))) { 
                        // Remember to Fix this later 
                        
                                p1.println();
                                p1.println("      " + specialnames.get(i) + "(&__" + plainClassName + "::" + specialnames.get(i) + ") {"); 
                    
                    
                    }
                    
                    
                    // Finally Add Parameters Here 
                    else {
                        if ( parameters.get(specialnames.get(i)).equals("ZeroParams") && !(specialnames.get(i).equals(plainClassName)) && !(arr1.contains(specialnames.get(i)))){

                            if(i!=names.size()-1)
                            p1.println("      " + specialnames.get(i) + "((" + types2.get(i) + "(*)" + "(" + plainClassName + "))" + "&" + checkForOtherSuperClass.get(i+6) + "::"  + specialnames.get(i) + "),"); 
                            
                            
                            else 
                               p1.println("      " + specialnames.get(i) + "((" + types2.get(i) + "(*)" + "(" + plainClassName + "))" + "&" + checkForOtherSuperClass.get(i+6) + "::"  + specialnames.get(i) + ") {"); 
                        
                            
                            p1.println();
                        
                        }
                        else { 
                            
                            if(!(specialnames.get(i).equals(plainClassName)) && !(arr1.contains(specialnames.get(i)))  ) { 
                            
                            ArrayList<Integer> getTheParameters = new ArrayList<Integer>();
                            
                            p1.print("      "   + specialnames.get(i) + "((" + types2.get(i) + "(*)" + "(" + plainClassName + " , ");
                            
                            Pattern pp = Pattern.compile("_");    
                            
                            Matcher mm = pp.matcher(parameters.get(specialnames.get(i))); 
                            
                            while (mm.find()) { 
                                
                                getTheParameters.add(mm.start());
                                
                            }
                            
                            for ( int cc = 0; cc < getTheParameters.size(); cc++ ) { 
                                
                                
                                if ( cc != getTheParameters.size()-1) {
                                    
                                    p1.print( parameters.get(specialnames.get(i)).substring(getTheParameters.get(cc) + 1, getTheParameters.get(cc+1)) + " , "); 
                                    
                                    
                                }
                                else {
                                    int length = parameters.get(specialnames.get(i)).length();
                                    p1.print( parameters.get(specialnames.get(i)).substring(getTheParameters.get(cc) + 1, length));
                                    if(i!=names.size()-1)
                                        p1.print("))" + "&" + checkForOtherSuperClass.get(i+6) + "::" + specialnames.get(i) + "),");
                                    else 
                                        p1.print("))" + "&" + checkForOtherSuperClass.get(i+6) + "::" + specialnames.get(i) + ") {");

                                    p1.println();
                                }
                                
                            }
                            
                            
                            //p1.println(getTheParameters);
                        }                                    

                        }  
                }
                        
                        
                    }       
                        
                
            p1.println("    }");
            p1.println("};"); 
            p1.println();
            p1.println();
            p1.println();
              /*Remove comments below to Debug**/
               
               
                
               

           } 
                //p1.println(names);
            
           
           
            p1.flush();
            p1.close();
        } 
           
           
    
        catch ( Exception e) { 
        }
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
        
        if (runtime.test("printJavaCode")) {
            new CPrinter(runtime.console()).dispatch(node);
            runtime.console().flush();
        }

    }
	     }
        

	
	
