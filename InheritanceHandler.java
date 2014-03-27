package oop;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Printer;
import xtc.tree.Visitor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Iterator;

//initialization of inheritance tree w/ java.lang
//tree processing
//method processing
//data layout processing
//constructor processing
//getter/helper methods


public class InheritanceHandler extends Visitor {
		
	GNode classTree; //will hold the whole thang
	GNode currentHeaderNode; //global GNode holding the current version of the header (data layout + vtable)
    GNode classStaticVars; //this class's static variables
    String className; //global reference to the name of the class we're currently dispatching through
    Printer console;
	
	
	
	
	///////////************** CONSTRUCTOR
	
	public InheritanceHandler(Node[] astArray, Printer console)
	{
		this.console = console;
		
		initializeGivenClasses(); //this will hardcode the Object, Class, String, and Array classes to be beginning of the tree (correctly)
							      // every other class will have to extend one of these data layouts/classes
		for(int i = 0; i < astArray.length; i++)  //for each dependency AST, dispatch
		{
			if(astArray[i] != null)  //don't care about null ASTs
			{
				this.dispatch(astArray[i]); //will visit everything in the tree, now to override the visit____ methods fuck.
			}
		}
		
        //printClassTree(); //debug!
	}
	
	
	
	
	
	
	
	
	
	
	
	///////////************** TREE/CLASS PROCESSING
	
	public void visit(Node n) 
	{
		for( Object o : n ) {
			if (o instanceof Node) dispatch((Node)o);
		}
	}

	
	
	
    public void visitClassDeclaration(GNode n) {
		className = n.get(1).toString();  //for the record, get(#) goes down the tree according to the # index and then returns that node
		GNode newClassNode = GNode.create("Class"); //create Class node
		newClassNode.setProperty("name", className); //set the name
        GNode parent = null; 
		classStaticVars = GNode.create("StaticVarsList"); //this will get set in visitFieldDeclaration when we visit(n) later
		
		if(n.get(3) != null) { // if this class extends another class
			//String extendedClass = (String)(n.getNode(3).getNode(0).getNode(0).get(0)); // String name of Parent Class
            String parentClass = n.getNode(3).getNode(0).getNode(0).getString(0);  //check to make sure this does the same thing as the previous line
			parent = getClass(parentClass); //define the parent node
		}
		else { 
			parent = getClass("Object"); // Doesn't extend define parent as Object
		}
		//if(DEBUG) System.out.println( "Visiting new class: " + className + " extends " + parent.getProperty("name") );
		//Building the vtables and data layout via Parents layout
		currentHeaderNode = inheritParentHeader((GNode)parent.getNode(0)); //inherit parent header
		visit(n); //visit every child of this class this will fill in the rest of the header
		GNode customArrays = findArrays(n);
		currentHeaderNode.addNode(customArrays);
		newClassNode.add(currentHeaderNode); //add the header to the newClassNode
		newClassNode.setProperty( "parentClassNode", parent); //set property 'parentClassNode' to the parent GNode from before, can get list of each nodes properties at any time, it's in the Node class's methods
		parent.addNode(newClassNode); //add child as the child of the parent node 
    }
	
    
    
    
    
    
    
    
	///////////************** INITIALIZERS
    
  
	//hardcodes the given base classes
	//Object, Class, String, and Array
    //need to provide the data layout for Object, and need to finish Templates for ARRAY
	public void initializeGivenClasses(){
		GNode object = GNode.create("Class");
		object.setProperty("name", "Object");
		GNode classHeaderDeclaration = GNode.create("ClassHeaderDeclaration");
		classHeaderDeclaration.add(initializeVirtualTable()); //need to write these methods
		classHeaderDeclaration.add(initializeDataLayout()); //need to write these methods
		object.add(classHeaderDeclaration);

		GNode string = GNode.create("Class");
		string.setProperty("name", "String");
		string.setProperty("parentClassNode", object);
		className = "String";
		string.add( inheritParentHeader( classHeaderDeclaration ) ); //need to write this method as well

		GNode classNode = GNode.create("Class");
		classNode.setProperty("name", "Class");
		classNode.setProperty("parentClassNode", object);
		className = "Class";
		classNode.add( inheritParentHeader( classHeaderDeclaration ) );

		GNode array = GNode.create("Class");
		array.setProperty("name", "Array");
		array.setProperty("parentClassNode", object);
		className = "Array";
		array.add( inheritParentHeader( classHeaderDeclaration ) );
		
		//actually add the nodes to our class tree after all the info
		//has been filled in correctly
		classTree = object;
		classTree.add(string);
		classTree.add(classNode); 
		classTree.add(array);
		//do i need to do integer?
	}
	
	//creates Object's virtual table, each method is a child of VTableDeclaration
	//uses various helper methods for readability and my sanity
	GNode initializeVirtualTable(){
		GNode virtualTable = GNode.create("VTableDeclaration");
		//children in order:
		//0 add __isa
		//1 add __delete/destructor
		//2 add hashcode
		//3 add equals
		//4 add getClass
		//5 add toString
		//6 add constructor
		virtualTable.add( createDataFieldDeclaration( "Class", "__isa" ) ); 
		virtualTable.add( createVTableVirtualMethodDeclaration("void", "__delete", new String[]{"__Object*"}) ); 
		virtualTable.add( createVTableVirtualMethodDeclaration( "int32_t", "hashCode", new String[]{"Object"} ));      
		virtualTable.add( createVTableVirtualMethodDeclaration( "bool", "equals", new String[]{"Object","Object"} ));
		virtualTable.add( createVTableVirtualMethodDeclaration( "Class", "getClass", new String[]{"Object"} ));      
		virtualTable.add( createVTableVirtualMethodDeclaration( "String", "toString", new String[]{"Object"} )); 
		virtualTable.add( initializeVTableConstructor(virtualTable));
		return virtualTable;
	}
	
	//creates Object's data layout, each data field is a child of DataLayoutDeclaration
	//uses various helper methods for readdability and my sanity
	GNode initializeDataLayout(){
		GNode dataLayout = GNode.create("DataLayoutDeclaration");
		//0 add vtable pointer
		//1 add node containing list of data fields
		//2 add node to hold constructors
		//3 add node containing list of methods
		//4 not sure what goes in this child
		
		//create node to hold constructor
		GNode constructorList =  GNode.create("ConstructorHeaderList");
		constructorList.add(0, className);
		
		//create node holding a list of method names
		GNode objMethHeaders = GNode.create( "MethodHeaderList" ); //simple node to contain method headers, now add static methods
		objMethHeaders.add( createStaticMethodHeader( "int32_t", "hashCode", new String[]{"Object"} ) ); //int32_t (*hashCode)(Object);
		objMethHeaders.add( createStaticMethodHeader( "bool", "equals", new String[]{"Object","Object"} ) ); //bool (*equals)(Object, Object);
		objMethHeaders.add( createStaticMethodHeader( "Class", "getClass", new String[]{"Object"} ) ); //Class (*getClass)(Object);
		objMethHeaders.add( createStaticMethodHeader( "String", "toString", new String[]{"Object"} ) ); //String (*toString)(Object);
		
		//append all the children to the DataLayoutDeclaration GNode
		dataLayout.add( createDataFieldDeclaration( "__Object_VT*", "__vptr" ) ); //0th child
		dataLayout.add( GNode.create( "DataFieldList" ) ); //1st child
		dataLayout.add( constructorList); //2nd child
		dataLayout.add( objMethHeaders ); //3rd child
		dataLayout.add( createStaticDataFieldDeclaration( "__Object_VT", "__vtable" ) ); //4th child
		return dataLayout;
	}
	
	//creates Objects constructor in the vTable
	//constructors in general are always held as the last slot in the VTable
	//children --> modifiers --> null --> name --> formal params --> method ptr list --> code block
	GNode initializeVTableConstructor( GNode vTable ) {
		GNode constructor = GNode.create("VTConstructorDeclaration");
		
		constructor.add( GNode.create( "Modifiers" ) ); //empty modifiers
		
		constructor.add( null ); //
	    
		constructor.add( "__Object_VT" ); //name of constructor
		
		constructor.add( GNode.create( "FormalParameters" ).add(null) ); //no parameters
		
		final GNode methodPtrList = GNode.create( "vtMethodPointersList" );
		methodPtrList.add( GNode.create( "ClassISAPointer" ).add( "__Object" ) ); //hard coded __isa(__Object...) pointer
		
		//visit all the virtual method declarations in the vtable and make an appropriate pointer in the constructor
		new Visitor() { 
			public void visit( Node n ) {
				for( Object o : n ) if (o instanceof GNode ) dispatch((GNode)o);
			}
			public void visitVirtualMethodDeclaration( GNode n ) {
				GNode newPtr = GNode.create( "vtMethodPointer" );
				// 0  method name
				// 1  __Object
				// 2 - params
				newPtr.add( n.get(1) ); //0th method name
				newPtr.add( createTypeGNode( "__Object" ) ); //1st target object
				newPtr.add( GNode.create( "FormalParameters" ) ); //2nd form parameters, but there are none so unneeded?
				methodPtrList.add( newPtr ); //append this method to the methodptrlist
			}
		}.dispatch(vTable);
		
		
		constructor.add( methodPtrList ); // 3rd method pointer list
		constructor.add( GNode.create( "Block" ) ); // 4th | empty code block is needed to complete the constructor, all real initialization is using the default constructor
		return constructor;
    }
	
	
	
	
	
	
	
	
	
	
	
	///////////************** FIELD HANDLING
	
	/*
	 * Handles any field declaration and then adds it to the data layout
	 * according to what type of declaration it is. If it's static we simply
	 * add it to the classStaticVars list. If it is not we check for overriding
	 * and handle that. Otherwise, it's a normal field and we simply add it in 
	 * a standard fashion to the data layout etc.
	 */
	public void visitFieldDeclaration(GNode n)
	{
		/* 
		 * create new a data layout for that field declaration 
		 * override to determine if field is in layout or not, override index...
		 * and all found fields into data layout
		 * just create DATA LAYOUT
        **/
		
		//if variable is static then we add it to class static vars
		//other wise we add it correctly to the virtual layout etc
		if ( isStatic( (GNode)n.getNode(0) ) )
		{
			classStaticVars.add( (GNode)n.getNode(2) ); //add it to class static vars
			n.getNode(2).getNode(0).set(1, null); //ensures location is null 
		}
		
		//set overriddenMethodIndex 
		int overridenFieldIndex = indexOfOverridingField(n, (GNode)currentHeaderNode.getNode(1).getNode(1) );
		
		if ( overridenFieldIndex >= 0 ) 
		{
			currentHeaderNode.getNode(1).getNode(1).set(overridenFieldIndex, n); //if field needs to be overriden, use .set() to override field at specified index
		}
		else 
		{	
			currentHeaderNode.getNode(1).getNode(1).add(n); //if field won't be overriden, add as regular field using .add()
		}
	}
        
		

	
	
	
	
	
	///////////************** (VIRTUAL) METHOD HANDLING
	
	public void visitMethodDeclaration(GNode n){
		/*everytime we visit a declaration
		 * create new a virtual method signature
		 * (case of overriding)overwrite the old virtualMethodPointer with the new one
		 * (all other times) creat the virtualMethodPointer and append it to the VirtualTable
		 * also make sure to edit the vtables constructor accordingly
        **/
		
		
        
                
        String methodName = n.getString(3);
		
		if(methodName == "main"){
			return; //ignore main methods
		}
			
		
		String betterMethodName = appendParamsToMethodName(n);
		n.set(3, betterMethodName); //overwrite name with better name that includes params in the name itself
		
		//--Adding '__this' to PARAMATERS node as a 'FormalParameter' GNode
		GNode thisFormParam = GNode.create("FormalParameter");
		thisFormParam.add(null); //no modifiers to set
		thisFormParam.add(createTypeGNode(className)); //set type of parameter
		thisFormParam.add(null); //nothing to set
		thisFormParam.add("__this"); //set name of parameter
		thisFormParam.add(null); //nothing to set
		n.set(4, GNode.ensureVariable((GNode)n.getNode(4)).add(0,thisFormParam) ); //go to PARAMETERS node, add the thisFormParam node we just made at 0th child
		
		
		//--Creating new VirtualMethodDeclaration node to put in VTable
		GNode vMethodSig = GNode.create("VirtualMethodDeclaration");
		vMethodSig.add(n.get(2)); //return type
		vMethodSig.add(betterMethodName); //method name
		GNode formalParameters = deepCopy((GNode)n.getNode(4));
		for( int i = 0; i < formalParameters.size(); i++ ) {
			formalParameters.set(i, formalParameters.getNode(i).getNode(1) ); // this kills the parameter name
		}
		
		vMethodSig.add(formalParameters);
		//--Assessing Override Status and Adding VirtualMethodDeclaration to VTable accordingly
		
		int overridenMethodIndex = indexOfOverridingMethod(n, (GNode)currentHeaderNode.getNode(0) ); //give it the MethodDeclaration node and current version of VTable
		
		//overrides inherited method
		if( overridenMethodIndex != (-1) ) 
		{
			currentHeaderNode.getNode(0).set(overridenMethodIndex, vMethodSig); // replace the inherited method sig with new one
			
			//adding method's pointer to the constructor
			
			int constructorSlot = currentHeaderNode.getNode(0).size()-1;
			GNode vtConstructorPtrList = (GNode)currentHeaderNode.getNode(0).getNode(constructorSlot).getNode(4); //4 is where the pointers are held
			GNode newPtr = GNode.create( "vtMethodPointer" ); //create a new vMethodPointer
			newPtr.add(betterMethodName); //append name to pointer
			newPtr.add(createTypeGNode( "__"+className)); //className is the caller class
			newPtr.add(GNode.create( "FormalParameters")); //append params
			vtConstructorPtrList.set(overridenMethodIndex, newPtr); //replace the inherited ptr with newPtr
			
		}
		//is a new method without overriding
		else 
		{
			int newMethodIndex = (currentHeaderNode.getNode(0).size()-1); //append new method to VTable just before the constructor (which is last aka index+1)
			currentHeaderNode.getNode(0).add(newMethodIndex, vMethodSig); //append to VTable
			
			//add new pointer to the constructor
			int constructorSlot = currentHeaderNode.getNode(0).size()-1;
			GNode vtConstructorPtrList = (GNode)currentHeaderNode.getNode(0).getNode(constructorSlot).getNode(4); //4 is where the pointers are held
			GNode newPtr = GNode.create( "vtMethodPointer" ); //create a new vMethodPointer
			newPtr.add(betterMethodName); //append name to pointer
			newPtr.add(createTypeGNode( "__"+className)); //className is the caller class
			newPtr.add(GNode.create( "FormalParameters")); //append params
			vtConstructorPtrList.add( newPtr ); //add new ptr at the end of ptr list
			
			//add new method to this class's data layout/list of methods
			GNode thisDataLayoutsMethodList = (GNode)currentHeaderNode.getNode(1).getNode(3);
			GNode method = GNode.create( "StaticMethodHeader" );
			method.add(n.get(2)); //append return type of method
			method.add(betterMethodName); //append method's name
			method.add(formalParameters); //append method's parameters
			thisDataLayoutsMethodList.add(method); //add this method to the method list
		}
            
     
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	///////////************** CONSTRUCTOR HANDLING
	
	public void visitConstructorDeclaration(GNode n){
		/**
		 * creates the constructor's header
		 * then adds it to the constructor list held in DataLayout
		 */
		
		//n.set(5, n.getNode(5).add(classStaticVars) );
		GNode constructorNode = GNode.create("ConstructorHeader");
		constructorNode.add( n.get(2) ); //name of constructor
		constructorNode.add( n.get(3) ); //append formal params
		currentHeaderNode.add(constructorNode);//put the constructor on the Vtable
		
		 
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	///////////************** GETTERS
	
	
	public String getSuperclassName(String name) {
		GNode sooper = getSuperclass(name);
		
		if( sooper == null) {
			return "Object";
		}
		
		return (String)sooper.getProperty("name");
    }
	
	
	
	/*given the subclass as a string, give me the super class's GNode*/
	public GNode getSuperclass(String sc) 
	{
		GNode child = getClass(sc);
		
		//if(DEBUG) System.out.println("got class node for " + sc);
		// can't be passed primitive type
		if (child.getProperty("name").equals("Object")) return null;
			
		return (GNode)child.getProperty("parentClassNode");
	}
	
	
	/*given the subclasses node, get it's name, and then make call to other getSuperclass method, return the Superclass's GNode*/
	public GNode getSuperclass(GNode n) 
	{
		return getSuperclass( n.getStringProperty("name") );
	}

	
	/*given the subclass as a string, return if it has a superclass*/
	public boolean hasSuperclass(String sc) 
	{
		if (getSuperclass(sc) != null) 
		{
			return true;
		}
		else
			return false;
	}
	
	
	// Returns the name of a class
    // @param n A Class node 
    // @return its name
    public String getName(GNode n) {
		return n.getStringProperty("name");
		
    }
	
	public GNode getClassTree(){
		return classTree;
	}
	
	//given a classDec node give me the class name
	public String getClassName(GNode n) 
	{
		return n.getStringProperty("name");
    }
    
    
	public GNode getVTable(String cN) {
		GNode className = getClass(cN);
		
		if( className == null ) {
		    System.out.println( "getVTable: vtable not found for class " + cN );
			return null;
		}
		if( className.size() == 0 || className.getNode(0) == null ) 
			System.out.println( "getVTable: failed retrieve for cN: " + cN );
		GNode classVT = (GNode)(className.getNode(0).getNode(0));
		return classVT;
    }

    
    public GNode getDataLayout(String cN) {
		GNode className = getClass(cN);
		
		GNode classData = (GNode) (className.getNode(0).getNode(1));
		
		return classData;
    }
    
    // Returns a Class node from the classTree
    // @param sc name of the Class desired
    // @return the appropriate class node
    public GNode getClass(String sc) {
		
		// Declared final to be accessible from inner Visitor classes
		final String s = sc;
		
		return (GNode)( new Visitor() {
			
			public GNode visitClass(GNode n) {
			    
				// Found the class
				if( getName(n).equals(s) ) {
					return n;
				}
			    
				// Keep Searching
				for( Object o : n) {
					if (o instanceof Node) {
						GNode returnValue = (GNode)dispatch((GNode)o);
						if( returnValue != null ) return returnValue;
					}
				}
				return null;
			}
			
			public void visit(GNode n) { // override visit for GNodes
				for( Object o : n) {
					if (o instanceof Node) dispatch((GNode)o);
				}
			}
			
	    }.dispatch(classTree));
    }
    
    
    /**
     * 
     * Return's a specific node from a Virtual Table
     * @param vtn
     * @param m
     * @return a specific GNode from the virtual table
     */
    public GNode getVTMethod(GNode vtn, String m) {
		//	System.out.println("--- Enter CLP");
		
		// Declared final to be accessible from inner Visitor
		final String mName = m;
		//	System.out.println("--- Searching for method " + mName);
		
		GNode returnThis = (GNode)( new Visitor () {
			
			public GNode visitVirtualMethodDeclaration(GNode n) {
				
				//		    System.out.println("\t--- At VirtualMethodDeclaration node:"
				//				       + n.getString(1));
				
				if( mName.equals(n.getString(1)) ) {
					// Found the node
					//			System.out.println("/t--- Returning VirtualMethodDeclaration node " + n.getString(1));
					//			System.out.println("/t--- that node is" + n.toString());
					
					return n;
				}
				
				// Keep Searching
				for( Object o : n) {
					if (o instanceof Node) {
						GNode returnValue = (GNode)dispatch((GNode)o);
						if( returnValue != null ) return returnValue;
					}
				}
				
				return null;
				
			}
			
			public GNode visit(GNode n) { // override visit for GNodes
				
				// Keep Searching
				for( Object o : n) {
					if (o instanceof Node) {
						GNode returnValue = (GNode)dispatch((GNode)o);
						if( returnValue != null ) return returnValue;
					}
				}
				
				return null;
				
			}
			
	    }.dispatch(vtn));
		
		
		//	if(null == returnThis)
		//	    System.out.println("/t--- null :-(");
		
		return returnThis;
		
    }
	
	
	
	
	
    
    
    
    
	
	///////////************** HELPER METHODS
    
    
    GNode inheritParentHeader(GNode parentHeader){
    	/**
    	 * create a deepcopy of the parent header
    	 * utilizing global className, edit the parent's header
    	 * when editing is done, we have the new child's header
    	 * 
    	 * we have to change __isA in the pointer in the constructor
    	 * fixing the class caller name in each virtual method in the vtable
    	 * 
    	 * overwriting/changing the vtable pointer in the 0th child slot in our copy of the datalayout
    	 * overwriting/changing the constructor list so that it contains className's constructor and clears it of its parent's constructors
    	 * editing the static method list so that the correct class name is held in the "this"
    	 * 
    	 * overwriting the last child of the datalayout so that it's  __className_VT and __vTable
    	 */
    	
    	GNode childHeader = deepCopy(parentHeader);
    	GNode childVirtualTable = (GNode) childHeader.getNode(0);
    	
    	int sizeOfTable = childVirtualTable.size();
    	int constructor = (sizeOfTable-1);
    	
    	childVirtualTable.getNode(constructor).getNode(4).getNode(0).set(0, "__"+className ); //changing the Class __isa pointer in the constructor;
    	//what is get node(4) method pointer list
    	//waht is get node(0) is the method name
    	// set the __isA into __className
    	
    	
    	//now handle all the methods in the method pointer list the first and last method
    	for( int i = 1; i < constructor; i++ ) 
    	{ //start at one to ignore Class __isa, end at size-1 to ignore constructor
			GNode thisVirtualMethod = (GNode)childVirtualTable.getNode(i); //get a method
			//console.format(thisVirtualMethod).flush();
			thisVirtualMethod.getNode(2).set(0, ("__"+className+"*"));
			//console.format(thisVirtualMethod).flush();
		}
		
    	
    	GNode vTableConstructorPointerList = (GNode)childVirtualTable.getNode(constructor).getNode(4); //method list in constructor
    	
		for( int i = 1; i < vTableConstructorPointerList.size(); i++ ) { // start at one to ignore __isa
			GNode thisPointer = (GNode)vTableConstructorPointerList.getNode(i);
			GNode caster = GNode.create("PointerCast");
			caster.add( childVirtualTable.getNode(i).getNode(0) ); //return value
			caster.add( childVirtualTable.getNode(i).getNode(2) ); //parameters
			
			if( thisPointer.size() >= 4 ) {
				thisPointer.set(3, caster);
			}
			else {
				thisPointer.add( caster );
			}
		}
		
		GNode childDataLayout = (GNode)childHeader.getNode(1);
		childDataLayout.set(0, createDataFieldDeclaration( "__"+className+"_VT*", "__vptr" )); //setting the right vtable pointer name
		
		GNode constructorList = GNode.create("ConstructorHeaderList");
		constructorList.add(0, className);
		
		childDataLayout.set(2, constructorList);//clear out the constructor list
		GNode statMethList = (GNode)childDataLayout.getNode(3);
		for( Object o : statMethList ) { //changing the 'this' parameter types in the static data layout methods
			//console.format((GNode)o).flush();
			((GNode)o).getNode(2).set(0, className); //ugh is that ugly or what?
		}
		
		childDataLayout.set(4, createStaticDataFieldDeclaration( "__"+className+"_VT", "__vtable" ));
		return childHeader;
    }
	
    
    //n is the method declaration node
    //currentVTable is the current Vtable...
    //returns -1 if the method does not override any of the methods in currentVTable
    int indexOfOverridingMethod(GNode  n, GNode currentVTable) {
		String methodName = n.get(3).toString(); //get method name

		for(int i = 1; i < currentVTable.size()-1; i++)  //start at one to ignore __isa declaration... conclude at size-1 to ignore constructor declaration
		{ 
			if(methodName.equals(currentVTable.getNode(i).get(1).toString())) 
			{
				return i; //return index into VTable where method that needs to be overridden lies
			}
		}
		return -1; //method does not override anything in currentVTable
    }
	
    
    //same as method version
    int indexOfOverridingField(GNode newField, GNode currentDataLayout) {
		String fieldName = newField.getNode(2).getNode(0).get(0).toString();
		
		for(int i = 0; i < currentDataLayout.size(); i++) //iterate through every field
		{
			if(fieldName.equals(currentDataLayout.getNode(i).getNode(2).getNode(0).get(0).toString())){ //equals the current existing field
				return i; //return index into DataLayout where the field that need to be overriden lies
			}
		}
		return -1; //field does not override an existing field in DataLayout
    }
    
    
    //returns wheter or not a certain method is static
    //also does this work? why is this a for loop? shouldnt this just take a field node as a param and then do a quick get(x) to check the node
    //which should hold whether Static = true or false
    boolean isStatic(GNode currentNode){
    	for(Object o : currentNode) if (((GNode)o).get(0).equals("static")) return true;
    	return false;
    }
    
    
    //creates a VirtualMethodDeclaration for the VTable
    //children --> return type --> method name --> formal paramaters tree
    GNode createVTableVirtualMethodDeclaration(String name, String returnType, String[] parameters){
    	GNode virtualMethod = GNode.create("VirtualMethodDeclaration");
    	GNode formalParameters = GNode.create("FormalParameters");
    	for(String param : parameters)
    	{
    		formalParameters.add(param);
    	} 
    	virtualMethod.add(createTypeGNode(returnType)); //0 return
    	virtualMethod.add(name); //1 method name
    	virtualMethod.add(formalParameters);//2 formal parameters	
    	return virtualMethod;
    }
    
    
    //same as above method, just slightly modified for static method
    GNode createStaticMethodHeader(String name, String returnType, String[] parameters){
    	GNode virtualStaticMethod = GNode.create("StaticMethodHeader");
    	GNode formalParameters = GNode.create("FormalParameters");
    	for(String param : parameters)
    	{
    		formalParameters.add(param);
    	}
    	virtualStaticMethod.add(createTypeGNode(returnType)); //0 return
    	virtualStaticMethod.add(name); //1 method name
    	virtualStaticMethod.add(formalParameters);//2 formal parameters	
    	return virtualStaticMethod;
    }
    
    //creates a DataFieldDeclaration for the DataLayout
    //children --> modifiers --> type node --> declarators
    GNode createDataFieldDeclaration(String type, String name){
    	GNode fieldDeclaration = GNode.create("FieldDeclaration"); //create fieldDec
    	
    	fieldDeclaration.add(GNode.create("Modifiers")); //0th child add blank Modifiers 
    	
    	GNode typeNode = createTypeGNode(type);
    	fieldDeclaration.add(typeNode);  //1st child add type
    	
    	
		GNode declr = GNode.create("Declarator");
		declr.add( name );
		declr.add( null ); 
		declr.add( null ); //need to fill in these nulls for printer compatibility
		
		GNode declrs = GNode.create("Declarators"); 
		declrs.add( declr ); //append declarators to this node
		
		
		fieldDeclaration.add( declrs ); //2nd child adds declarators
		
		return fieldDeclaration;
    	
    }
    
    //same as DataFieldDeclaration() but makes sure the static modifier gets in there (we leave Modifiers blank in the other method)
    GNode createStaticDataFieldDeclaration(String type, String name){
    	
    	GNode staticFieldDeclaration = GNode.create("FieldDeclaration"); //create fieldDec
    	
    	
    	GNode mod = GNode.create("Modifier");
    	mod.add("static");
    	
    	GNode mods = GNode.create("Modifiers");
    	mods.add(mod);
    	
    	staticFieldDeclaration.add(mod);
    	
    	
    	GNode typeNode = createTypeGNode(type); 
    	staticFieldDeclaration.add(typeNode); 
    	
    	
		GNode declr = GNode.create("Declarator");
		declr.add( name );
		declr.add( null ); 
		declr.add( null ); //need to fill in these nulls for printer compatibility
		
		GNode declrs = GNode.create("Declarators"); 
		declrs.add( declr ); //append declarators to this node
		
		
		staticFieldDeclaration.add( declrs ); //add decs to fielddec node
		
		return staticFieldDeclaration;
    }
    
    
    //creates a basic Type GNode
    GNode createTypeGNode(String type){
    	GNode typeNode = GNode.create("Type");
    	
    	GNode specifier;
    	if( type.equals("int") || type.equals("float") || type.equals("boolean") || type.equals("byte") || type.equals("short") || type.equals("long") || type.equals("double") || type.equals("char") ) { //testing for primitive, obvs needs to be expanded
			specifier = GNode.create( "PrimitiveType" );
		} else {
			specifier = GNode.create( "QualifiedIdentifier" ); //separating 'qualified' class names from primitives just seems safer for now...
		}
    	typeNode.add(specifier); //1st child
    	typeNode.add(type); //0th child?
    	typeNode.add(null); //2nd child null
    	
    	return typeNode;
    	
    }
    
    
    public String appendParamsToMethodName(GNode methodNode) {
		String name = methodNode.getString(3);
		GNode parametersBlock = (GNode)methodNode.getNode(4);
		int numParams = parametersBlock.size();
		String paramsNames[] = new String[numParams];
		for( int i = 0; i < numParams; i++) {
			GNode thisTypeNode = (GNode)parametersBlock.getNode(i).getNode(1);
			paramsNames[i] = thisTypeNode.getNode(0).getString(0);
		}
		for( String s : paramsNames ) {
			name += "_" + s;
		}
		return name;
	}
    
    
    //creates a deep (NOT SHALLOW) copy of node n
    static GNode deepCopy(GNode n) {
		GNode deepCopy = (GNode) new Visitor() {
			public Object visit(GNode n) {
				GNode deepCopy = GNode.ensureVariable(GNode.create(n));
				while( deepCopy.size() > 0 ) {
					deepCopy.remove(0);
				}
				for( Object o : n ) {
					if( o instanceof GNode ) {
						deepCopy.add( visit((GNode)o) );
					}
					else deepCopy.add( o ); //arbitrary objects don't need to be copied because they would just be replaced
				}
				return deepCopy;
			}
	    }.dispatch(n);
	    
		return deepCopy;
    }
    
    
    ///////////************** ARRAY SPECIALIZATION/TEMPLATE HANDLING ///////////************** ARRAY SPECIALIZATION/TEMPLATE HANDLING ///////////************** ARRAY SPECIALIZATION/TEMPLATE HANDLING
    
    
    GNode customClasses = GNode.create("CustomClasses");
    GNode templateNodes = GNode.create("ArrayTemplates");
    // Need to add these in proper namespaces
    GNode primStructs = GNode.create("Declaration");
    GNode primTypes = GNode.create("PrimitiveTYPES");
    boolean isArray = false;
    int dim;
    String qID;
    
    
    /*
     * This bad ass mofo finds every array and creates the AST for
     * the array template specialization code. We have some global
     * variables to help us do this more easily (they are declard above)
     * 
     * @return returns a
     */
    public GNode findArrays(GNode n) 
    {
		
    	final GNode classDeclaration = n;

		new Visitor() 
		{
			/*
			 * This is our meat.
			 * We visit the Field and recurse through it's children to determine whether or not
			 * this field is actually an array.
			 * 
			 * Once we know it is an array, we grab all the info we need in order to print out
			 * the array template specialization code for that type of array. We visit all arrays
			 * and then declare a struct for each individual TYPE of array we find, not each acutal array.
			 */
		    public void visitFieldDeclaration(GNode n) 
		    {
				
				// Immediately visit down to see if it's an array.
				visit(n);
							
				if(isArray) 
				{
				    // FU Grimm! short a2[] = new short[2];
				    normalizeArray(n);
		
				    GNode newArrayExpression = (GNode) n.getNode(2).getNode(0).getNode(2);
		
				    // get Dimensions
				    String dims = newArrayExpression.getNode(1).getNode(0).getString(0);
				    dim = Integer.parseInt(dims);
		
				    // get Type/qID
				    qID = newArrayExpression.getNode(0).getString(0);
		
				    //get all the prim struct/type stuff
				    if(isPrimitive(qID) && !"int".equals(qID)) 
				    {
				
						// Add nodes for TYPE and struct declarations
						GNode primStruct = GNode.create("PrimitiveStruct");
						String type =  qID.substring(0,1).toUpperCase() + qID.substring(1).toLowerCase();
						
						primStruct.add(createTypeGNode(type));
						primStructs.add(primStruct);
			
			
						GNode primType = GNode.create("PrimitiveTYPE");
						primType.add(createTypeGNode(type));
						String lc = type.toLowerCase();
						primType.add(createTypeGNode(lc));
						primTypes.add(primType);
			
			
				    }
				    
				    //quickly make a node to hold the parent's type
				    GNode parent = GNode.create("ParentType");
				    
					//grab the parent's type info
				    //primitive types dont have super classes so ignore those
				    if(dim > 1 || isCustomType(classDeclaration, qID)) 
				    {	
						if(!isPrimitive(qID)) 
						{
						    String pID = getSuperclassName(qID);
						    parent.add(createTypeGNode(pID));
						}
				    } 
				    
				    
				    //create a nice component node
				    GNode component = GNode.create("ComponentType");
				    component.add(createTypeGNode(qID));
					
				    // FIXME: Add ['s to denote dimensions?
				    GNode templateNode = GNode.create("ArrayTemplate");
				    
				    
				    if(isPrimitive(qID)) //if primitive
				    {
				    	templateNode.add(component); // that's all you need to memset
				    }
				    else //if not primitive then we've got ourselves a custom class!
				    {
						// Customize __class()
						GNode customClass = GNode.create("CustomClass");
						customClass.add(parent);
						customClass.add(component);
						customClasses.add(customClass);
						    
						// Specialize Template
						// Note: templateNodes is NOT already in the tree
						templateNode.add(parent);
						templateNode.add(component);
			
				    }
				    templateNodes.add(templateNode);
		
				    // reset boolean when done.
				    isArray = false;
				    
				} // end isArray
	       		
		    } // end visitFieldDeclaration
			
		    /*
		     * Quickly sets the boolean isArray to true
		     * This gets called in visitFieldDeclaration when we call visit(n)
		     * If we never hit an array expression the field is a normal field
		     * so we don't do anything. If it's an array we enter the array handler.
		     */
		    public void visitNewArrayExpression(GNode n) 
		    {
		    	isArray = true;
		    }
		    
		    /*
		     * Standard GNode visit
		     */
		    public void visit(GNode n) 
		    {
		    	for( Object o : n) 
		    	{
		    		if (o instanceof Node) dispatch((GNode)o);
		    	}
		    }
				
		}.dispatch(n);
		
		
		//if we dont have any customClasses set it to null
		if(customClasses == null)
		{
				customClasses = null;
		}
		
		
		//add all the custom classes as children of the Declaration node
		GNode customs = GNode.create("CustomArrayDeclaration", customClasses);
		return customs;
		
    } // end findArrays
    
    
    
    
    /*
	 * Checks if the name (string) held in "Type" node is Object, String, or Class
	 * @param s String A String holding the type
	 * @return b boolean false when the name is "String", "Object" or "Class"
	 */
	public boolean isCustomName(String s) 
	{
		if("String".equals(s) || "Object".equals(s) || "Class".equals(s)) 
		{
			return false;
		}
		
		return true;
	}
	
	/*
	 * Checks whether the type of a certain node is custom (!= to Object, Class, String, etc)
	 * @param n GNode a classDeclaration node
	 * @param s String a string contianing the name of the primary identifier
	 * NOTE: This is an exact copy of a method by the same name that is in ASTConverter... 
	 * i guess i could make that method static and use it here but for right now this is way simpler
	 * and quicker to get up and running.
	 */
	boolean isCustomType(GNode n, String s) 
	{	
			final String p = s; //p = primary identifier
			
			GNode isCT = (GNode) (new Visitor() 
			{
				public GNode visitDeclarator(GNode n) 
				{
				    if( p.equals(n.getString(0)) ) // We found where the primary identifier is declared to get Type
				    {
				    	String type;
				    	if(n.getNode(2).hasName("Type")) 
				    	{
				    		type = n.getNode(2).getNode(2).getString(0);
				    	}
						else if(n.getNode(2).hasName("NewClassExpression")) 
						{
						    type = n.getNode(2).getNode(2).getString(0);
						}
						else if(n.getNode(2).hasName("NewArrayExpression")) 
						{
						    type = n.getNode(2).getNode(0).getString(0);
						}
						else //we still haven't found the goddamn type!!!
						{
						    // Going down one more level should return Type node if we've gotten this far and haven't found it yet
						    type = n.getNode(2).getNode(0).getNode(0).getString(0);
						}
				    	
				    	//quickly check whether the name of the type we've found is technically custom
				    	//if it is then we just return that node immediately, otherwise we'll exit, hit the visit, and keep searching until we do find one
				    	if(isCustomName(type))
				    	{
					    return n;
				    	}
				    }
				    
				    // Keep Searching
				    for( Object o : n) 
				    {
				    	if (o instanceof Node) 
				    	{
				    		GNode returnValue = (GNode)dispatch((GNode)o);
				    		if( returnValue != null ) //if we've hit the isCustomName conditional and it has passed, then we return that custom type node
				    			return returnValue; //returns the custom type node
				    	}
				    }
				    return null; //otherwise there is no custom type, return value is null
				}
	
	
				public GNode visit(GNode n) 
				{ 
				    // Keep Searching
				    for( Object o : n) 
				    {
						if (o instanceof Node) 
						{
						    GNode returnValue = (GNode)dispatch((GNode)o);
						    if( returnValue != null ) return returnValue;
						}
				    }  
				    return null;   
				}
				
			    }.dispatch(n));
	
			if(isCT != null) return true;
			
			return false;
	}
	
	/*
	 * Checks whether a certain Type (provided as a string)
	 * is considered primitive or not. returns true if the
	 * type is indeed primitive.
	 */
	public boolean isPrimitive(String sc) {
		// Should String really be here?
		if (sc.equals("int") || sc.equals("boolean") || sc.equals("byte") || sc.equals("short") || sc.equals("long") || sc.equals("float") || sc.equals("double") || sc.equals("char") || sc.equals("String") ) {
			return true;
		}
		else 
			return false;
    }
	
	/*
	 * Dimensions can be declared in multiple places in terms of java syntax
	 * so this just normalizes the syntax so that the dimensions
	 * are always found in the same place when we run findArrays(GNode n).
	 * 
	 * @param n a field declaration for an array
	 */
    public void normalizeArray(GNode n) 
    {
		if(null == n.getNode(1).get(1))
		{
		    // Must move Dimensions node
		    GNode dims = (GNode) n.getNode(2).getNode(0).getNode(1);
		    n.getNode(1).set(1, dims);
	
		    // now clear it out
		    n.getNode(2).getNode(0).set(1, null);
		}
    }
    
   
    
	///////////************** DEBUG
	
	
    /*someday this will print out the whole class tree once it's been made*/
	public void printClassTree() 
	{

		System.out.println("Class Tree:");
		
		new Visitor () 
		{
			public void visit(GNode n) 
			{
				for( Object o : n) 
				{
					if (o instanceof Node) dispatch((GNode)o);
				}
			}

			public void visitClass(GNode n) 
			{
				System.out.print("Class " + n.getStringProperty("name"));
				String superClass = getSuperclassName(n.getStringProperty("name"));
				System.out.println( " extends " + superClass/*(superClass==null?superClass:"")*/);
				visit(n);
			}
		}.dispatch(classTree);
		System.out.println();
    }
	
}