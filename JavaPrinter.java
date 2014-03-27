/*
 * xtc - The eXTensible Compiler
 * Copyright (C) 2005-2011 Robert Grimm, New York University
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 * USA.
 */
package oop;

import java.util.Iterator;

import xtc.tree.Comment;
import xtc.tree.Node;
import xtc.tree.GNode;
import xtc.tree.Printer;
import xtc.tree.Token;
import xtc.tree.Visitor;
import java.io.File; 
import java.io.IOException;
import java.io.Reader; 
import java.io.*; 
import java.util.*; 
/**
 * A pretty printer for Java.
 *
 * <p />A note on operator precedence: This printer uses precedence
 * levels to control when to print parentheses around expressions.
 * The actual precedence values are the standard Java precedence
 * levels multiplied by ten.
 *
 * @author Robert Grimm, Stacey Kuznetsov, Martin Hirzel
 * @version $Revision: 1.71 $
 */
/* Altered Solely By Ankit Goel **/ 
public class JavaPrinter extends Visitor {

  /**
   * The base precedence level. This level corresponds to the
   * expression nonterminal.
   */
  public static final int PREC_BASE = 0;

  /**
   * The list precedence level.  This level corresponds to the
   * assignment expression nonterminal.
   */
  public static final int PREC_LIST = 10;

  /**
   * The constant precedence level.  This level corresponds to the
   * conditional expression nonterminal.
   */
  public static final int PREC_CONSTANT = 1;
  
  /** The flag for any statement besides an if or if-else statement. */
  public static final int STMT_ANY = 0;

  /** The flag for an if statement. */
  public static final int STMT_IF = 1;

  /** The flag for an if-else statement. */
  public static final int STMT_IF_ELSE = 2;

  /** The printer for this Java printer. */
  protected final Printer printer;

  /** The package for any previous import declaration. */
  protected String packageName;

  /** The flag for whether we just printed a declaration. */
  protected boolean isDeclaration;

  /** The flag for whether we just printed a statement. */
  protected boolean isStatement;

  /** The flag for whether the last statement ended with an open line. */
  protected boolean isOpenLine;

    public  PrintWriter p2; 
     
    public PrintWriter p3; 

  /**
   * The flag for whether the current statement requires nesting or
   * for whether the current declaration is nested within a for
   * statement.
   */
  protected boolean isNested;

  /**
   * The flag for whether this statement is the else clause of an
   * if-else statement.
   */
  protected boolean isIfElse;

  /** The operator precedence level for the current expression. */
  protected int precedence;


  Node returnthis;
  /**
   * Create a new Java printer.
   *
   * @param printer The printer.
   */
  public JavaPrinter(Printer printer) {
    this.printer = printer;
    printer.register(this);
  }

  /**
   * Fold the specified qualified identifier.
   *
   * @param qid The qualified identifier.
   * @param size Its size.
   */
     
  protected String fold(GNode qid, int size) {
    StringBuilder buf = new StringBuilder();
    for (int i=0; i<size; i++) {
      buf.append(qid.getString(i));
      if (i<size-1) buf.append('.');
    }
    return buf.toString();
  }

  /**
   * Get the package name corresponding to the specified import
   * declaration.
   *
   * @param n The import declaration node.
   * @return The corresponding package name.
   */
  protected String getPackage(GNode n) {
    assert n.hasName("ImportDeclaration");

    GNode qid  = n.getGeneric(1);
    int   size = qid.size();
    if (null == n.get(2)) size--;

    return 0 >= size ? "" : fold(qid, size);
  }

  /** The actual implementation of {@link #containsLongExpression}. */
  @SuppressWarnings("unused")
  private static final Visitor containsLongExprVisitor = new Visitor() {
    public Boolean visitBlock(GNode n) {
      return Boolean.TRUE;
    }

    public Boolean visitArrayInitializer(GNode n) {
      return Boolean.TRUE;
    }

    public Boolean visit(GNode n) {
      for (Object o : n) {
        if ((o instanceof Node) && (Boolean)dispatch((Node)o)) {
          return Boolean.TRUE;
        }
      }
      return Boolean.FALSE;
    }
  };

  /**
   * Determine whether the specified node contains a long expression.
   * This method considers blocks and array initializers to be long.
   *
   * @param n The node.
   * @return <code>true</code> if the node contains a long expression.
   */
  protected boolean containsLongExpression(GNode n) {
    return (Boolean)containsLongExprVisitor.dispatch(n);
  }

  /**
   * Determine whether the specified declaration is long.  A long
   * declaration requires multiple lines for readability.  Examples
   * include declarations containing class bodies or blocks.
   *
   * @param decl The declaration.
   * @return <code>true</code> if the specified declaration is long.
   */
  protected boolean isLongDeclaration(GNode decl) {
    return (decl.hasName("ConstructorDeclaration") ||
            decl.hasName("ClassDeclaration") ||
            decl.hasName("InterfaceDeclaration") ||
            decl.hasName("AnnotationDeclaration") ||
            decl.hasName("EnumDeclaration") ||
            decl.hasName("BlockDeclaration") ||
            (decl.hasName("MethodDeclaration") &&
             (null != decl.get(7))) ||
            (decl.hasName("FieldDeclaration") &&
             containsLongExpression(decl)) ||
            (decl.hasName("AnnotationMethod") &&
             containsLongExpression(decl)));
  }

  /**
   * Print the specified node's children as declarations and/or
   * statements.
   *
   * @param n The node.
   */
  protected void printDeclsAndStmts(GNode n) {
    isOpenLine     = false;
    isNested       = false;
    isIfElse       = false;
    isDeclaration  = false;
    isStatement    = false;
    GNode previous = null;

    for (Object o : n) {
      final Node  node    = (Node)o;
      if (null == node) continue;
      final GNode current = GNode.cast(node);

      // If there was a previous node and the previous node was a
      // block or long declaration, the current node is a block or
      // long declaration, or the previous node was a statement and
      // the current node is a declaration, then print an extra
      // newline.
      if ((null != previous) &&
          (previous.hasName("Block") ||
           (isLongDeclaration(previous) &&
            current.getName().endsWith("Declaration")) ||
           current.hasName("Block") ||
           isLongDeclaration(current) ||
           (! previous.getName().endsWith("Declaration") &&
            current.getName().endsWith("Declaration")))) {
        printer.pln();
      }

      printer.p(node);

      if (isOpenLine) printer.pln();
      isOpenLine = false;
      previous   = current;
    }
  }

  /**
   * Print an expression as a truth value.  This method parenthesizes
   * assignment expressions.
   *
   * @param n The node to print.
   */
  protected void formatAsTruthValue(Node n) {
    if (GNode.cast(n).hasName("AssignmentExpression")) {
      printer.p('(').p(n).p(')');
    } else {
      printer.p(n);
    }
  }
  
  /**
   * Print empty square brackets for the given number of dimensions.
   * 
   * @param n Number of dimensions to print.
   */
  protected void formatDimensions(final int n) {
    for (int i=0; i<n; i++) printer.p("[]");
  }

  /**
   * Start a new statement.  This method and the corresponding {@link
   * #prepareNested()} and {@link #endStatement(boolean)} methods
   * provide a reasonable default for newlines and indentation when
   * printing statements.  They manage the {@link #isDeclaration},
   * {@link #isStatement}, {@link #isOpenLine}, {@link #isNested}, and
   * {@link #isIfElse} flags.
   *
   * @param kind The kind of statement, which must be one of the
   *   three statement flags defined by this class.
   * @return The flag for whether the current statement is nested.
   */
  protected boolean startStatement(int kind) {
    if (isIfElse && ((STMT_IF == kind) || (STMT_IF_ELSE == kind))) {
      isNested = false;
    } else {
      if (isOpenLine) printer.pln();
      if (isDeclaration) printer.pln();
      if (isNested) printer.incr();
    }
    isOpenLine     = false;
    boolean nested = isNested;
    isNested       = false;
		
    return nested;
  }
 
  /**
   * Prepare for a nested statement.
   *
   * @see #startStatement
   */
  protected void prepareNested() {
    isDeclaration = false;
    isStatement   = false;
    isOpenLine    = true;
    isNested      = true;
  }

  /**
   * End a statement.
   *
   * @see #startStatement
   *
   * @param nested The flag for whether the current statement is nested.
   */
  protected void endStatement(boolean nested) {
    if (nested) {
      printer.decr();
    }
    isDeclaration = false;
    isStatement   = true;
  }

  /**
   * Enter an expression context.  The new context has the specified
   * precedence level.
   *
   * @see #exitContext(int)
   *
   * @param prec The precedence level for the expression context.
   * @return The previous precedence level.
   */
  protected int enterContext(int prec) {
    int old    = precedence;
    precedence = prec;
    return old;
  }

  /**
   * Enter an expression context.  The new context is appropriate for
   * an operand opposite the associativity of the current operator.
   * For example, when printing an additive expression, this method
   * should be called before printing the second operand, as additive
   * operators associate left-to-right.
   *
   * @see #exitContext(int)
   *
   * @return The previous precedence level.
   */
  protected int enterContext() {
    int old     = precedence;
    precedence += 1;
    return old;
  }

  /**
   * Exit an expression context.
   *
   * @see #enterContext(int)
   * @see #enterContext()
   *
   * @param prec The previous precedence level.
   */
  protected void exitContext(int prec) {
    precedence = prec;
  }

  /**
   * Start printing an expression at the specified operator precedence
   * level.
   *
   * @see #endExpression(int)
   *
   * @param prec The expression's precedence level.
   * @return The previous precedence level.
   */
  protected int startExpression(int prec) {
    if (prec < precedence) {
      printer.p('(');
    }
		
    int old    = precedence;
    precedence = prec;
    return old;
  }
  /**
   * Stop printing an expression.
   *
   * @see #startExpression(int)
   *
   * @param prec The previous precedence level.
   */
  protected void endExpression(int prec) {
    if (precedence < prec) {
      printer.p(')');
    }
    precedence = prec;
  }

  /** Visit the specified comment. */
  public void visit(Comment c) {
    printer.indent().p(c).p(c.getNode());
  }

  /** Visit the specified translation unit. */
  public void visitCompilationUnit(GNode n) {
    
    File cppFile; 
    File mainFile; 
    try { 
        
        // File I/O 
        cppFile = new File("cplusplusfiles", "Method_Bod.cc");
        
        // Nice Method to create a new file 
        
        cppFile.createNewFile();

        mainFile = new File("cplusplusfiles", "main.cc"); 

        mainFile.createNewFile(); 


        p2 = new PrintWriter(cppFile); 

        p3 = new PrintWriter(mainFile); 
    
            p2.println("// Ankit goel's pretty printing"); 

    // Routine Header Stuff 
    p2.println("#include \"java_lang.h\" ");
    p2.println("#include \"Header.h\" ");
    p2.println("#include <sstream> ");
    p2.println("using namespace java::lang;");
    p3.println("// Ankit goel's pretty printing "); 
    p3.println(); 
    p3.println("#include <iostream>"); 
    p3.println("#include \"java_lang.h\" ");
    p3.println("#include \"Header.h\" ");
    p3.println("#include <sstream> ");
    p3.println("using namespace java::lang;");
    p3.println(); 
   // p3.println("int main(void) { "); 
    p3.println(); 



    // Reset the state.
    packageName   = null;
    isDeclaration = false;
    isStatement   = false;
    isOpenLine    = false;
    isNested      = false;
    isIfElse      = false;
    precedence    = PREC_BASE;

    printDeclsAndStmts(n);
    p2.println(); 
    p2.flush();
    p2.close();

     //p3.println("}"); 
     p3.flush();
     p3.close(); 
    
    }
    catch (Exception e) { 

    }
    
    
}

  public void visitStreamOutputList(GNode n){
    
      if ( !(n.toString().contains("CallExpression")) && !(n.toString().contains("SelectionExpression")) && !(n.toString().contains("AdditiveExpression")))
        p2.println("    std::cout <<" +  n.getNode(1).getString(0) + " << std::endl;");
  
    else  if( n.toString().contains("CallExpression") && !(n.toString().contains("SelectionExpression")) && !(n.toString().contains("AdditiveExpression")) ) {
          p2.println("   __rt::checkNotNull(" + n.getNode(1).getNode(0).getString(0) + ");"); 
          p2.println("    std::cout << " + n.getNode(1).getNode(0).getString(0) + "->" + "__vptr->" + n.getNode(1).getString(2) + "(" +  n.getNode(1).getNode(0).getString(0) + ")" + " << std::endl;");
      }


    else if ( n.getNode(1).getNode(0).getName().equals("PrimaryIdentifier") && n.toString().contains("SelectionExpression")) { 
        p2.println("     std::cout << " + n.getNode(1).getNode(0).getString(0) + "->" + n.getNode(1).getString(1)  + " << std::endl" +  ";");

    }



    else  if( n.toString().contains("SelectionExpression") && !(n.toString().contains("AdditiveExpression")))
        p2.println("    std::cout << "  + "__this" + "->" + n.getNode(1).getString(1)  + " << std::endl" +  ";"); 

    else  if( n.toString().contains("AdditiveExpression") && !(n.toString().contains("ThisExpression"))) { 
        p2.println("   std::cout << " + n.getNode(1).getNode(0).getNode(0).getNode(0).getNode(0).getString(0) + "  " + " << " + "  " + n.getNode(1).getNode(0).getNode(0).getNode(0).getNode(2).getString(0) + " " + "  << " + " " + n.getNode(1).getNode(0).getNode(2).getString(0) + " << " + n.getNode(1).getNode(2).getString(0) + " << std::endl; "); 



      }

    else if ( n.toString().contains("AdditiveExpression") && (n.toString().contains("ThisExpression"))) { 

      p2.println(" std::cout << " + n.getNode(1).getNode(0).getNode(0).getNode(0).getString(0) + " << " + "__this->" + n.getNode(1).getNode(0).getNode(0).getNode(2).getString(1) + " << " + "__this->" + n.getNode(1).getNode(2).getString(1) + " << std::endl;"); 

    }


  }
    
  /** Visit the specified package declaration. */
  public void visitPackageDeclaration(GNode n) {  
    GNode qid   = n.getGeneric(1);
    packageName = fold(qid, qid.size());

    printer.indent().p(n.getNode(0)).p("package ").p(n.getNode(1)).pln(';');
    isOpenLine  = false;
  }
  
  /** Visit the specified import declaration. */
  public void visitImportDeclaration(GNode n) {  
    String p = getPackage(n);
    if ((null != packageName) && (! p.equals(packageName))) printer.pln();
    packageName = p;

    printer.indent().p("import ");
    if (null != n.get(0)) printer.p("static ");
    printer.p(n.getNode(1));
    if (null != n.get(2)) printer.p(".*");
    printer.pln(';');
    isOpenLine  = false;
  }
  
  /** Visit the specified modifiers. */
  public void visitModifiers(GNode n) {
    for (Object o : n) printer.p((Node)o).p(' ');
  }

  /** Visit the specified modifier. */
  public void visitModifier(GNode n) {
    printer.p(n.getString(0));
  }
  
  /** Visit the specified formal parameter. */
  public void visitFormalParameter(GNode n) {
    final int size = n.size();
    printer.p(n.getNode(0)).p(n.getNode(1));
    for (int i=2; i<size-3; i++) { // Print multiple catch types.
      printer.p(" | ").p(n.getNode(i));
    }
    if (null != n.get(size-3)) printer.p(n.getString(size-3));
    printer.p(' ').p(n.getString(size-2)).p(n.getNode(size-1));
  }
  
  /** Visit the specified final clause. */
  public void visitFinalClause(GNode n) {
    printer.p("final");
  }
  
  /** Visit the specified formal parameters. */
  public void visitFormalParameters(GNode n) {
    printer.p('(');
    for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
      printer.p((Node)iter.next());
      if (iter.hasNext()) printer.p(", ");
    }
    printer.p(')');
  }

 String retu;
  /** Visit the specified declarator. */
  public void visitDeclarator(GNode n) {
    retu = (n.getString(0));
    // printer.p(n.getString(0));
    // if(null != n.get(1)) {
    //   if (Token.test(n.get(1))) {
    //     formatDimensions(n.getString(1).length());
    //   } else {
    //     printer.p(n.getNode(1));
    //   }
    // }
    // if(null != n.get(2)) {
    //   printer.p(" = ").p(n.getNode(2));
    // }
  }

  /** Visit the specified declarators. */
  public void visitDeclarators(GNode n) {
    for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
      p2.println((Node)iter.next());
      if (iter.hasNext()) p2.println(", ");
    }
  }

  /** Visit the specified annotations. */
  public void visitAnnotations(GNode n) {
    for (Object o : n) printer.p((Node)o).p(' ');
  }

  /** Visit the specified annotation. */
  public void visitAnnotation(GNode n) {
    printer.p('@').p(n.getNode(0));
    if (null != n.get(1)) printer.p('(').p(n.getNode(1)).p(')');
  }

  /** Visit the specified element value pairs. */
  public void visitElementValuePairs(GNode n) {
    boolean first = true;
    for (Object o : n) {
      if (first) first = false;
      else printer.p(", ");
      printer.p((Node)o);
    }
  }

  /** Visit the specified element value pair. */
  public void visitElementValuePair(GNode n) {
    printer.p(n.getNode(0)).p(" = ").p(n.getNode(1));
  }

  /** Visit the specified default value. */
  public void visitDefaultValue(GNode n) {
    printer.p("default ").p(n.getNode(0));
  }

  /** Visit the specified class body. */
  public void visitClassBody(GNode n) {

    if (isOpenLine) printer.p(' ');
    //printer.pln('{').incr();

    printDeclsAndStmts(n);
		
    printer.decr().indent();
    isOpenLine    = true;
    isNested      = false;
  //  isIfElse      = false;
  }
  
 String[] parts;
 String[] partsofmethod;

  /** Visit the specified field declaration. */
  public void visitFieldDeclaration(GNode n) {
    //printer.p("//");
   //p2.println(printer.p(n.getNode(0)).p(n.getNode(1)).p(' ').p(n.getNode(2)).p(';').pln());
    

    if ( (n.size() == 3) && (n.toString().contains("static")) && !(n.toString().contains("IntegerLiteral")) && n.toString().contains("int")) { 
        p2.println(); 
        p2.println(); 
       // p2.println("     " + n.getNode(1).getNode(0).getString(0) + "  " + n.getNode(2).getNode(0).getString(0) +  ";"); 
         p2.println(" // Static Variable Initialization ");
         p2.println("int32_t " + "__" + classopener + "::" +  n.getNode(2).getNode(0).getString(0) + " = 0;"); 


    }
    else if ( (n.size() == 3) && (n.toString().contains("static")) && n.toString().contains("IntegerLiteral") && n.toString().contains("int") ) { 
                 p2.println("int32_t " + "__" + classopener + "::" +  n.getNode(2).getNode(0).getString(0) + " = " + n.getNode(2).getNode(0).getNode(2).getString(0) + ";"); 



    }

    else { 
      if( n.toString().contains("NewClassExpression")) { 


        fieldsThatGoIntoConstructor.add(n.getNode(1).getNode(0).getString(0) + " " + n.getNode(2).getNode(0).getString(0)  + " =  new __" + n.getNode(2).getNode(0).getNode(2).getNode(2).getString(0) + "();");
      }
      // Check for Declarator  
     else  if((n.getNode(2).getNode(0).size() == 3)  && n.getNode(2).toString().contains("StringLiteral") && !(n.toString().contains("static")))
           fieldsThatGoIntoConstructor.add(n.getNode(2).getNode(0).getString(0) + " = " + " __rt::literal(" + n.getNode(2).getNode(0).getNode(2).getString(0) + ")" + ";"); 

      // neeed to do a check for Integer  
      else if ((n.getNode(2).getNode(0).size() == 3 ) && n.getNode(2).toString().contains("IntegerLiteral") && !(n.toString().contains("static")) ) 
           fieldsThatGoIntoConstructor.add(n.getNode(2).getNode(0).getString(0) + " = " +  n.getNode(2).getNode(0).getNode(2).getString(0) + ";"); 

      
      // Other cases 
      //else 
         // fieldsThatGoIntoConstructor.add(n.getNode(1).getNode(0).getString(0) + " " + n.getNode(2).getNode(0).getString(0) + ";");      
        // p2.println(fieldsThatGoIntoConstructor); 
    }



    isDeclaration = true;
    isOpenLine    = false;

  }


  
  
  /** Visit the specified method declaration. **/
  public void visitMethodDeclaration(GNode n) {
    //printer.p(n.toString());

    p2.println();
    returnthis = n.getNode(5);

    //printer.p(n.toString());
    partsofmethod = (n.toString()).split(" ");
   


    if (null != n.get(1)) {
      //p2.println(n.getNode(1));
    }
    
    parts = (n.getString(3)).split("_");
    if (parts[0].equals("main")){

            p2.println(); 

            p2.println("void __" + classopener + "::main(__rt::Ptr<__rt::Array<String> > args) { ");
            
              // Print out of the Main Body 
              new Visitor () { 

                  public void visitBlock (GNode n) { 
                    // Nested Block 
                    if ( n.getNode(0).getName().equals("Block")) {
                      p2.println("{");

                       nestedBlock = true; 
                       visit(n); 
                      
                       p2.println(); 
                      //p3.println("}"); 
                      //p3.flush();
                      //p3.close(); 
                     
                    }

                    else 
                      visit(n); 
                  }   


                  public void visitFieldDeclaration(GNode n) { 
                       
                    if(n.toString().contains("Dimensions") && !(n.toString().contains("NewArrayExpression"))) { 
                        p2.println();
                       
                        p2.print("      __rt::Ptr<__rt::Array<" + n.getNode(1).getNode(0).getString(0) + "> > " +  n.getNode(2).getNode(0).getString(0) + " = " + n.getNode(2).getNode(0).getNode(2).getString(0) + ";"); 
                        
                    }

                    else if ( n.toString().contains("NewArrayExpression")) {

                      p2.println();
                      p2.println("__rt::Ptr<__rt::Array<" + n.getNode(1).getNode(0).getString(0) + "> > " + n.getNode(2).getNode(0).getString(0) + " =  new __rt::Array<"+ n.getNode(1).getNode(0).getString(0) + ">(" + n.getNode(2).getNode(0).getNode(2).getNode(1).getNode(0).getString(0) + ");"); 
                      p2.println(); 
                    }



                   else if ( !(n.toString().contains("NewClassExpression")) && !(n.toString().contains("StringLiteral")) && !(n.toString().contains("Integerliteral")) && (n.toString().contains("SelectionExpression")) && n.toString().contains("QualifiedIdentifier"))  {
                        p2.println("     " + n.getNode(1).getNode(0).getString(0) + " " + n.getNode(2).getNode(0).getString(0) + " = " + n.getNode(2).getNode(0).getNode(2).getNode(0).getString(0) + "->" + n.getNode(2).getNode(0).getNode(2).getString(1) + ";"); 

                      


                    }





                  else  if ( !(n.toString().contains("NewClassExpression")) && n.toString().contains("IntegerLiteral") && !(n.toString().contains("SelectionExpression"))) { 

                        p2.println("    " + "int32_t " + n.getNode(2).getNode(0).getString(0) + " = " + n.getNode(2).getNode(0).getNode(2).getString(0) + ";");


                    }  


                      else    
                       visit(n); 


                  }

                  public void visitForStatement(GNode n) { 
                    p2.println(); 
                    if(!(n.toString().contains("length")) )
                    p2.println("for ( " + n.getNode(0).getNode(1).getNode(0).getString(0) + " " + n.getNode(0).getNode(2).getNode(0).getString(0) + " = " + n.getNode(0).getNode(2).getNode(0).getNode(2).getString(0) + " ; " + n.getNode(0).getNode(3).getNode(0).getString(0) + " " + n.getNode(0).getNode(3).getString(1) + " " + n.getNode(0).getNode(3).getNode(2).getString(0) + " ; " + n.getNode(0).getNode(4).getNode(0).getNode(0).getString(0) + n.getNode(0).getNode(4).getNode(0).getString(1) + " ) { "); 
                    else if (n.toString().contains("length"))
                    p2.println("for ( " + n.getNode(0).getNode(1).getNode(0).getString(0) + " " + n.getNode(0).getNode(2).getNode(0).getString(0) + " = " + n.getNode(0).getNode(2).getNode(0).getNode(2).getString(0) + " ; " + n.getNode(0).getNode(3).getNode(0).getString(0) + " " + n.getNode(0).getNode(3).getString(1) + " " + n.getNode(0).getNode(3).getNode(2).getNode(0).getString(0) +"->length" + " ; " + n.getNode(0).getNode(4).getNode(0).getNode(0).getString(0) + n.getNode(0).getNode(4).getNode(0).getString(1) + " ) { "); 

                    new Visitor() { 


                      public void visitBlock(GNode n) { 
                      //  p3.println("{");
                        visit(n); 
                        p2.println(); 
                        p2.println("}"); 
                        p2.println(); 

                      }

                      public void visitExpressionStatement(GNode n) { 

                          if(n.getNode(0).getName().equals("CallExpression") && !(n.toString().contains("StreamOutputList")) && !(n.toString().contains("SubscriptExpression")))
                              p2.println("      " + n.getNode(0).getNode(0).getString(0) + "->" + n.getNode(0).getString(2) + "(" + n.getNode(0).getNode(0).getString(0) + ");");

                          else if ( n.toString().contains("SubscriptExpression") && n.toString().contains("NewClassExpression") && !(n.toString().contains("StreamOutputList"))  ) { 


                              if ( n.getNode(0).getNode(2).getNode(3).size() == 1 )
                              p2.println("       "  + "(*" + n.getNode(0).getNode(0).getNode(0).getString(0) + ")" + "[" + n.getNode(0).getNode(0).getNode(1).getString(0) + "]" + " =  __" + n.getNode(0).getNode(2).getNode(2).getString(0) + "::init_Construct( new __" + n.getNode(0).getNode(2).getNode(2).getString(0) + "(" + n.getNode(0).getNode(2).getNode(3).getNode(0).getString(0) + "));" ); 
                              else 
                                                            p2.println("       "  + "(*" + n.getNode(0).getNode(0).getNode(0).getString(0) + ")" + "[" + n.getNode(0).getNode(0).getNode(1).getString(0) + "]" + " =  __" + n.getNode(0).getNode(2).getNode(2).getString(0) + "::init_Construct( new __" + n.getNode(0).getNode(2).getNode(2).getString(0) + "());" ); 



                          }


                          else if ( n.toString().contains("SubscriptExpression") && !(n.toString().contains("StreamOutputList")) && n.toString().contains("CallExpression") ) { 

                              p2.print("      " + "(*" + n.getNode(0).getNode(0).getNode(0).getString(0) + ")" + "[" + n.getNode(0).getNode(0).getNode(1).getString(0) + "]->__vptr->" + n.getNode(0).getString(2) + "(" +     "(*" + n.getNode(0).getNode(0).getNode(0).getString(0) + ")"  + "[" + n.getNode(0).getNode(0).getNode(1).getString(0) + "]" +    ");" );

                          }
                          else 
                            visit(n); 


                      }

                      public void visitStreamOutputList(GNode n) { 
                          p2.print("  std::cout ");

                          if ( !(n.toString().contains("CallExpression"))) { 
                         

                          new Visitor() { 

                            public void visitStringLiteral(GNode n) { 

                              p2.print(" << " + n.getString(0)); 

                            }
                            public void visitSubscriptExpression(GNode n) { 
                              p2.print(" << " + "(*" + n.getNode(0).getString(0) + ")"  +"[" + n.getNode(1).getString(0) + "]" );

                            }

                            public void visitPrimaryIdentifier(GNode n) { 

                              if(n.getString(0).equals("std::endl"))
                                  p2.print(" << " + n.getString(0) + " ;"); 
                              //  p3.println(); 

                            }


                            public void visit(GNode n) { 
                                for ( Object o : n) if ( o instanceof Node) dispatch((Node)o);

                            }

                          }.dispatch(n); 


                        }

                        else if ( n.toString().contains("CallExpression") && n.toString().contains("SubscriptExpression") ){ 

                            p2.println("   <<  (*" + n.getNode(1).getNode(0).getNode(0).getString(0) + ")" + "[" + n.getNode(1).getNode(0).getNode(1).getString(0) + "]" + "->__vptr->" + n.getNode(1).getString(2) + "(" + "(*" + n.getNode(1).getNode(0).getNode(0).getString(0) + ")" + "[" + n.getNode(1).getNode(0).getNode(1).getString(0) + "]) << std::endl;" );


                        }



                      }


                      public void visit(Node n) {
                          for (Object o : n) if (o instanceof Node) dispatch((Node) o);
                      }


                      }.dispatch(n); 



                  }


                  public void visitConditionalStatement(GNode n) { 


                      new Visitor() { 



                            public void visitEqualityExpression(GNode n) { 
                                p2.println(); 
                                p2.println("    if ( " + n.getNode(0).getString(0) + "  "  + n.getString(1) + " " + n.getNode(2).getString(0) + " )  { " ); 

                            }

                            public void visitBlocK(GNode n) { 


                              visit(n); 


                            }

                            public void visitExpressionStatement(GNode n) { 

                                if(n.toString().contains("CallExpression") && !(n.toString().contains("StreamOutputList"))) { 


                                    p2.println("     " + n.getNode(0).getNode(0).getString(0) + "->" + n.getNode(0).getString(2) + "(" + n.getNode(0).getNode(0).getString(0) + ");"); 
                                    p2.println("    }"); 
                                }

                                else
                                visit(n); 



                            }

                            public void visitStreamOutputList(GNode n) { 

                                  if(n.toString().contains("StringLiteral")) { 

                                    p2.print("        std::cout "); 

                                    new Visitor() { 


                                       public void visitStringLiteral(GNode n) { 

                                          p2.print(" << " + n.getString(0)); 


                                       }
                                       
                                       public void visit(Node n) {
                                        for (Object o : n) if (o instanceof Node) dispatch((Node) o);
                                       }




                                    }.dispatch(n); 

                                    p2.print(" << std::endl; ");
                                    p2.println(); 
                                    p2.println("    }"); 


                                  }



                                  if(n.toString().contains("CallExpression")) { 

                                      p2.println(); 

                                      p2.print("    std::cout <<" + n.getNode(1).getNode(0).getString(0) + "->" + n.getNode(1).getString(2) + "(" + n.getNode(1).getNode(0).getString(0) + ")" + " << std::endl;");

                                      p2.println(); 
                                      p2.println("  }"); 

                                  } 



                          }

                           public void visit(Node n) {
                             for (Object o : n) if (o instanceof Node) dispatch((Node) o);
                            }



                      }.dispatch(n); 






                  }


                  public void visitWhileStatement(GNode n) { 

                          new Visitor () { 

                            public void visitWhileStatement(GNode n) { 

                                visit(n); 

                            }
                            public void visitBlock(GNode n) { 

                                visit(n); 
                                p2.println(); 
                                p2.println("   }"); 

                            }

                          

                            public void visitStreamOutputList(GNode n) { 

                                  p2.println(); 
                              if(n.toString().contains("StringLiteral")) { 

                                  p2.print("    std::cout  "); 
                                  new Visitor () { 


                                    public void visitStringLiteral(GNode n) { 

                                        p2.print(" << " + n.getString(0)); 

                                    }


                                    public void visit(GNode n) { 
                                for ( Object o : n) if ( o instanceof Node) dispatch((Node)o);

                                    }

                                  }.dispatch(n);   

                                  p2.print("<< std::endl;"); 
                                  p2.println(); 
                              }


                            }


                            public void visitExpressionStatement(GNode n) { 


                                if(!(n.toString().contains("StreamOutputList"))) { 
                                  new Visitor () { 

                                      public void visitPostfixExpression(GNode n) { 

                                        p2.println("    " + n.getNode(0).getString(0) + n.getString(1) + ";"); 
                                      }

                                      public void visitUnaryExpression(GNode n) { 



                                      }


                                             public void visit(GNode n) { 
                                
                                for ( Object o : n) if ( o instanceof Node) dispatch((Node)o);

                                    }




                                  }.dispatch(n);   

                                }


                                else 
                                  visit(n); 

                            }


                            public void visitRelationalExpression(GNode n) { 
                                p2.println(); 
                                p2.println("    while ( " + n.getNode(0).getString(0) + " " + n.getString(1) + "  " + n.getNode(2).getString(0) + " ) {"); 

                            }


                            public void visit(GNode n) { 
                                for ( Object o : n) if ( o instanceof Node) dispatch((Node)o);

                            }

                          }.dispatch(n); 


                  }

                  public void visitType (GNode n ) { 
                       if(n.toString().contains("PrimitiveType") && n.toString().contains("int")) {
                        
                          p2.print("int32_t  " );  
                        //p3.println("Reaches Type"); 
                       // p3.flush();
                       //p3.close(); 

                       }
                       else
                        visit(n); 

                  }

                  public void visitQualifiedIdentifier(GNode n) { 
                      p2.print("    " + n.getString(0) + "  "); 



                  }

                  public void visitDeclarators(GNode n) { 
                    if(!(n.toString().contains("NewClassExpression")) && !(n.toString().contains("QualifiedIdentifier")) && !(n.toString().contains("StringLiteral")) && !(n.toString().contains("PrimaryIdentifier"))) {
                        p2.print(n.getNode(0).getString(0) + ";"); 
                        p2.println(); 
                        //p3.println("}"); 
                        //p3.flush();
                      //p3.close(); 

                    }
                    else if(!(n.toString().contains("NewClassExpression")) && !(n.toString().contains("QualifiedIdentifier")) && !(n.toString().contains("StringLiteral")) && (n.toString().contains("PrimaryIdentifier"))) {
                    
                        p2.print(n.getNode(0).getString(0) + " = " + n.getNode(0).getNode(2).getString(0) + ";");
                        p2.println();  
                    }  
                    else
                        visit(n); 

                  }

                  public void visitDeclarator(GNode n) { 


                    if ( n.toString().contains("NewClassExpression")) {
                      p2.print(n.getString(0)); 
                      visit(n); 
                    }

                    else  { 

                      p2.println(n.getString(0) + " = "  + n.getNode(2).getString(0) + ";"); 



                    }



                  }

                  public void visitNewClassExpression(GNode n) { 


                    // No Arguements 
                    if (n.getNode(3).size() == 0) {
                        p2.print(" = " + "__" + n.getNode(2).getString(0) + "::init_Construct( " + " new __" + n.getNode(2).getString(0) + "());"); 
                        p2.println(); 
                    }

                    // Arguements Probably a String 
                    else  { 


                       p2.print(" = new __" + n.getNode(2).getString(0) ); 

                       new Visitor () { 

                          public void visitArguments(GNode n) { 
                             
                              if (n.getNode(0).getName().equals("StringLiteral")) { 
                                  p2.print("( new __String(" + n.getNode(0).getString(0) + "));" ); 
                                  p2.println(); 

                              }

                          }


                           public void visit(Node n) {
                              for (Object o : n) if (o instanceof Node) dispatch((Node) o);
                            
                           }




                       }.dispatch(n.getNode(3)); 
                  }



                  }

                  public void visitExpressionStatement(GNode n) { 
                                        //  StringBuilder appendToMethod; 
                                                               // appendToMethod.append("");  
                    if (n.toString().contains("SubscriptExpression") && !(n.toString().contains("CallExpression"))) { 

                        //p2.println(); 
                        p2.print("    (*" + n.getNode(0).getNode(0).getNode(0).getString(0) + ")" + "[" + n.getNode(0).getNode(0).getNode(1).getString(0) + "]" + " = " + "__" + n.getNode(0).getNode(2).getNode(2).getString(0) + "::init_Construct(" + "new __" + n.getNode(0).getNode(2).getNode(2).getString(0) + "());"); 
                        p2.println();

                    }

                    else if ( n.toString().contains("SubscriptExpression") && n.toString().contains("CallExpression")) { 


                        p2.println("    " + "(*" + n.getNode(0).getNode(0).getNode(0).getString(0) +" )" + "[" + n.getNode(0).getNode(0).getNode(1).getString(0) + "]" + "->__vptr->" + n.getNode(0).getString(2) + "(" + "(*" + n.getNode(0).getNode(0).getNode(0).getString(0) +" )" + "[" + n.getNode(0).getNode(0).getNode(1).getString(0) + "]" + ");"); 

                    }


                   else if (n.toString().contains("PostfixExpression")) { 

                      //  p2.print("__" + classopener + "::" + n.getNode(0).getNode(0).getNode(0).getString(0) + "++"); 
                      p2.println("    "  + n.getNode(0).getNode(0).getString(0) + n.getNode(0).getString(1) + ";"); 

                    }

                  else if ( n.toString().contains("UnaryExpression")) { 
                      p2.println("     " + n.getNode(0).getString(0) + n.getNode(0).getNode(1).getString(0) + ";"); 

                  }




                    // method chaining
                 else   if( n.getNode(0).getNode(0).getName().equals("CallExpression")) {

                        final ArrayList<String> primaryIden = new ArrayList<String>(); 

                        p2.println(); 
                       

                        new Visitor() { 

                                    public void visitPrimaryIdentifier(GNode n) { 
                                        primaryIden.add(n.getString(0)); 

                                    }


                                    public void visit(GNode n) { 
                                    for (Object o : n) if (o instanceof Node) dispatch((Node) o);

                                    }  



                        }.dispatch(n);








                       new Visitor () { 


                        public void visitCallExpression(GNode n) { 

                           

                            if(n.getNode(0).getName().equals("CallExpression")) {
                               visit(n);
                               



                               // Assume Method Chaining is done with Method thats return a type 

                               p2.print("->__vptr->" + n.getString(2));


                               // Append Arguements 
                               
                               
                                  

                                   p2.print("(" + primaryIden.get(0)); 



                                  
                                    // physically add the arguements 
                                 
                                  
                                  p2.print(")"); 


                             }  
                          
                             else { 

                                //  primaryIden = n.getNode(0).getString(0); 

                                  p2.print(n.getNode(0).getString(0) + "->__vptr->" + n.getString(2)); 

                                  // Attach the arguements to the method name
                                  new Visitor () { 


                                    public void visitArguements(GNode n) { 

                                      visit(n);



                                    }

                                    public void visitNewClassExpression(GNode n) {

                                        p2.print("_"); 
                                        p2.print(n.getNode(2).getString(0));
                                    }

                                    public void visitCastExpression(GNode n) { 
                                        p2.print("_");
                                        p2.print(n.getNode(0).getNode(0).getString(0));


                                    }




                                    public void visit(GNode n) { 
                                    for (Object o : n) if (o instanceof Node) dispatch((Node) o);

                                    }  



                                  }.dispatch(n); 
                                  p2.print("(" + n.getNode(0).getString(0)); 

                                  // physically add the arguements 
                                  new Visitor () { 


                                    public void visitArguements(GNode n) { 

                                      visit(n);



                                    }

                                    public void visitCastExpression(GNode n) { 
                                        p2.print(" , ");
                                        p2.print("(" + n.getNode(0).getNode(0).getString(0) + ")" + n.getNode(1).getString(0));


                                    }


                                    public void visitNewClassExpression(GNode n) {
                                        p2.print(" , ");
                                        p2.print(" new __");  
                                        p2.print(n.getNode(2).getString(0)  + "()");
                                    }



                                    public void visit(GNode n) { 
                                    for (Object o : n) if (o instanceof Node) dispatch((Node) o);

                                    }  



                                  }.dispatch(n); 

                                  p2.print(")"); 




                                



                             }

                        
                          

                          
                         }

                            public void visit(GNode n) { 
                             for (Object o : n) if (o instanceof Node) dispatch((Node) o);

                            }

                         }.dispatch(n); 
                         p2.print(";"); 
                         p2.println(); 
                         p2.println();


                }     

                 else   if(n.toString().contains("CallExpression") && !(n.toString().contains("StreamOutputList")) && n.toString().contains("IntegerLiteral") && !(n.getNode(0).getNode(0).getName().equals("CallExpression")) ){
                      // Assuming the method call has arguements and is not toString & only one arguement
                     
                    // Basically This has one arguement 
                     if(n.getNode(0).getNode(3).size() == 1) { 
                        p2.println(); 
                        p2.println("     // Method Call  " ); 
                        p2.print("     " + n.getNode(0).getNode(0).getString(0) + "->__vptr->" + n.getNode(0).getString(2)); 


                        for( int c = 0; c < n.getNode(0).getNode(3).size(); c++) {

                                  p2.print("_"); 
                                  p2.print("int"); 
                              
                        }

                       
                        p2.print("(" +  n.getNode(0).getNode(0).getString(0) + " , "   + n.getNode(0).getNode(3).getNode(0).getString(0)  + ");"); 
                        
                        p2.println(); 
                      }


                       // Has Two Arguements 
                     else  if (n.getNode(0).getNode(3).size() > 1) { 
                        p2.println(); 
                        p2.println("     // Method Call  " ); 
                        p2.print("     " + n.getNode(0).getNode(0).getString(0) + "->__vptr->" + n.getNode(0).getString(2)); 


                        for( int c = 0; c < n.getNode(0).getNode(3).size(); c++) {

                              
                                  p2.print("_"); 
                                  p2.print("int"); 
                              



                        }

                       
                        p2.print("(" +  n.getNode(0).getNode(0).getString(0) + " , "   + n.getNode(0).getNode(3).getNode(0).getString(0)  + " ,"); 

                          for ( int c = 1; c < n.getNode(0).getNode(3).size(); c++) { 


                              if( c != (n.getNode(0).getNode(3).size()-1))
                                  p2.print(   n.getNode(0).getNode(3).getNode(c).getString(0) +  " , "); 

                                else 
                                  p2.print(  n.getNode(0).getNode(3).getNode(c).getString(0) + "  );  "); 



                          }




                        p2.println(); 
                      }




                    }


                    

               else  if(n.toString().contains("CallExpression") && !(n.toString().contains("StreamOutputList")) && n.toString().contains("StringLiteral") && !(n.getNode(0).getNode(0).getName().equals("CallExpression"))) {
                      // Assuming the method call has arguements and is not toString & only one arguement
                     
                    // Basically This has one arguement 
                     if(n.getNode(0).getNode(3).size() == 1) { 
                        p2.println(); 
                        p2.println("     // Method Call  " ); 
                        p2.print("     " + n.getNode(0).getNode(0).getString(0) + "->__vptr->" + n.getNode(0).getString(2)); 


                        for( int c = 0; c < n.getNode(0).getNode(3).size(); c++) {

                              if( n.getNode(0).getNode(3).getNode(c).getName().equals("StringLiteral")) {
                                  p2.print("_"); 
                                  p2.print("String"); 
                              }



                        }

                       
                        p2.print("(" +  n.getNode(0).getNode(0).getString(0) + " , "  + " new __String("  + n.getNode(0).getNode(3).getNode(0).getString(0) + ")" + ");"); 
                        p2.println(); 
                      }


                       // Has Two Arguements 
                     else  if (n.getNode(0).getNode(3).size() > 1) { 
                        p2.println(); 
                        p2.println("     // Method Call  " ); 
                        p2.print("     " + n.getNode(0).getNode(0).getString(0) + "->__vptr->" + n.getNode(0).getString(2)); 


                        for( int c = 0; c < n.getNode(0).getNode(3).size(); c++) {

                              if( n.getNode(0).getNode(3).getNode(c).getName().equals("StringLiteral")) {
                                  p2.print("_"); 
                                  p2.print("String"); 
                              }



                        }

                       
                        p2.print("(" +  n.getNode(0).getNode(0).getString(0) + " , "  + " new __String("  + n.getNode(0).getNode(3).getNode(0).getString(0) + ")" + " ,"); 

                          for ( int c = 1; c < n.getNode(0).getNode(3).size(); c++) { 


                              if( c != (n.getNode(0).getNode(3).size()-1))
                                  p2.print(" new __String(" +  n.getNode(0).getNode(3).getNode(c).getString(0) + ")  , "); 

                                else 
                                  p2.print(" new __String(" +  n.getNode(0).getNode(3).getNode(c).getString(0) + ")  );  "); 



                          }




                        p2.println(); 
                      }




                    }
                    // hack because the tree i was looking at was not handling the arguement correctly 
                    else if ( n.toString().contains("swap")) { 
                      p2.println("__Test105::swap_int_int(new __Test105(), a,b);"); 
                      p2.println("std::cout << \"a = \" << a << \" b = \" << b << std::endl;"); 


                    }

                   else if ( n.toString().contains("CallExpression") && !(n.toString().contains("StreamOutputList")) && !(n.toString().contains("StringLiteral")) && (n.getNode(0).getNode(3).size() >= 1) && !(n.getNode(0).toString().contains("CastExpression")) && !(n.toString().contains("IntegerLiteral")) && !(n.getNode(0).getNode(3).toString().contains("NewClassExpression")) && !(n.getNode(0).getNode(0).getName().equals("CallExpression")))   {
                        
                        if(!(n.getNode(0).toString().contains("NewClassExpression")))
                        p2.println("     " + n.getNode(0).getNode(0).getString(0) + "->__vptr->" + n.getNode(0).getString(2) + "_A" +  "( " + n.getNode(0).getNode(0).getString(0) + " , " + n.getNode(0).getNode(3).getNode(0).getString(0) + "  );");


                   }


                   else if ( n.toString().contains("CallExpression") && !(n.toString().contains("StreamOutputList")) && !(n.toString().contains("StringLiteral")) && (n.getNode(0).getNode(3).size() == 0) && !(n.getNode(0).toString().contains("CastExpression")) && !(n.toString().contains("IntegerLiteral")) && !(n.getNode(0).getNode(0).getName().equals("CallExpression")) )  {
                                            p2.println("     " + n.getNode(0).getNode(0).getString(0) + "->__vptr->" + n.getNode(0).getString(2) +  "( " + n.getNode(0).getNode(0).getString(0) +  ");");



                    }


                   else if ((n.getNode(0).size() == 3) && (n.getNode(0).toString().contains("IntegerLiteral")) && !(n.toString().contains("CallExpression")) && !(n.toString().contains("StringLiteral")) && !(n.getNode(0).getNode(0).getName().equals("CallExpression")) ){ 
                        p2.println(n.getNode(0).getNode(0).getString(0) + " = " + n.getNode(0).getNode(2).getString(0) + ";"); 
                                                
                        if(nestedBlock)
                              p2.println("}"); 

                       // p3.flush();
                      //p3.close(); 

                   }
                   else if ( !(n.toString().contains("StreamOutputList")) && !(n.toString().contains("SelectionExpression")) && !(n.getNode(0).getNode(0).getName().equals("CallExpression"))){ 
                  
                        // First thing first append the arguements to the method call 

                        p2.print("    " + n.getNode(0).getNode(0).getString(0) + "->__vptr->" + n.getNode(0).getString(2) ); 

                        new Visitor() { 


                          public void visitNewClassExpression(GNode n) { 
                            p2.print("_");
                            p2.print(n.getNode(2).getString(0));

                          }

                          public void visitCastExpression(GNode n) { 

                            p2.print("_");
                            p2.print(n.getNode(0).getNode(0).getString(0)); 


                          }

                          public void visit(GNode n) { 
                             for (Object o : n) if (o instanceof Node) dispatch((Node) o);

                          }

                        }.dispatch(n.getNode(0).getNode(3));

                      p2.print("("); p2.print(n.getNode(0).getNode(0).getString(0) + "," ); 

                    // Compute a tally of arguements 
                        final GNode v = (GNode)n.getNode(0).getNode(3); 
                          new Visitor() { 



                          final  int counter = v.size();

                          int arguementsSoFar = 0; 

                          public void visitArguements(GNode n) { 
                          
                            visit(n); 

                          }    


                          public void visitNewClassExpression(GNode n) { 
                            arguementsSoFar++;
                           
                            p2.print(" new __" + n.getNode(2).getString(0) + "()");
                            if(arguementsSoFar != counter)
                              p2.print(",");

                          }

                          public void visitCastExpression(GNode n) { 

                            arguementsSoFar++;
                            p2.print("(" + n.getNode(0).getNode(0).getString(0) + ")" + n.getNode(1).getString(0)); 
                            if(arguementsSoFar != counter)
                              p2.print(",");


                          }

                          public void visit(GNode n) { 
                             for (Object o : n) if (o instanceof Node) dispatch((Node) o);

                          }

                        }.dispatch(n.getNode(0).getNode(3));
                      p2.print(");"); 
                      p2.println(); 




                   }


                   else if ( n.toString().contains("CastExpression") && n.toString().contains("SelectionExpression")) { 
                     

                      
                        


                           p2.println("     " + n.getNode(0).getNode(0).getNode(0).getString(0) + "->" + n.getNode(0).getNode(0).getString(1) + " = " + "__rt::java_cast<" + n.getNode(0).getNode(2).getNode(0).getNode(0).getString(0) + ">(" + n.getNode(0).getNode(2).getNode(1).getString(0) + ")" + ";"); 




                        




                   }


                   else if ( !(n.toString().contains("CastExpression")) && n.toString().contains("SelectionExpression") && n.toString().contains("=")) { 
                     

                      
                              p2.println("    " + n.getNode(0).getNode(0).getNode(0).getString(0) + "->" + n.getNode(0).getNode(0).getString(1) + " = " + n.getNode(0).getNode(2).getString(0) + ";");


                  }



                   if(n.toString().contains("StreamOutputList"))
                    visit(n); 



                  }

                  public void visitStreamOutputList(GNode n) { 

                    // Handle Everything with StreamOutputList here it is easier 
                    if(!(n.toString().contains("AdditiveExpression")))
                      p2.print("    "  + n.getNode(0).getString(0) + " << "); 


                    if ( n.toString().contains("AdditiveExpression")) { 
                    }

                   else if (n.getNode(1).getName().equals("CallExpression") && !(n.getNode(1).getNode(0).getName().equals("CallExpression")) && n.getNode(1).toString().contains("toString") && !(n.toString().contains("SelectionExpression")) && !(n.getNode(1).toString().contains("InstanceOfExpression"))) { 

                      p2.print(n.getNode(1).getNode(0).getString(0) + "->" + "__vptr->" + n.getNode(1).getString(2) + "(" + n.getNode(1).getNode(0).getString(0) + ")" + " << std::endl; "); 
                      p2.println(); 

                     }

                     else if (n.getNode(1).getName().equals("CallExpression") && (n.getNode(1).getNode(0).getName().equals("CallExpression")) && n.getNode(1).toString().contains("toString") && !(n.getNode(1).toString().contains("SelectionExpression")) && !(n.toString().contains("InstanceOfExpression"))) {
                      p2.println(n.getNode(1).getNode(0).getNode(0).getString(0) + "->" + "__vptr->" + n.getNode(1).getNode(0).getString(2) + "(" + n.getNode(1).getNode(0).getNode(0).getString(0) + ")" + "->" + "__vptr->" + n.getNode(1).getString(2) + "( " + n.getNode(1).getNode(0).getNode(0).getString(0) + "->" + "__vptr->" + n.getNode(1).getNode(0).getString(2) + "(" + n.getNode(1).getNode(0).getNode(0).getString(0) + ") );"); 

                     }

                     else if (n.getNode(1).getName().equals("SelectionExpression") && !(n.getNode(1).toString().contains(classopener)) && !(n.toString().contains("InstanceOfExpression"))) {
                        p2.println(n.getNode(1).getNode(0).getString(0) + "->" + n.getNode(1).getString(1)  +" << std::endl;");

                     }

                     else if (n.getNode(1).getName().equals("CallExpression") && !(n.getNode(1).toString().contains("toString")) && !(n.toString().contains("InstanceOfExpression")) ) {
                        // For Now ASSUME IT'S  A STRING

                        // No Arguements So No Name Mangling  
                        if(n.getNode(1).getNode(3).size() == 0) { 
                           p2.print(n.getNode(1).getNode(0).getString(0) + "->" + "__vptr->" + n.getNode(1).getString(2) + "(" + n.getNode(1).getNode(0).getString(0) + ")"  + " << std::endl; "); 
                           p2.println(); 
                        
                        }

                      else if ( n.getNode(1).getNode(3).size() == 1){ 




                          for ( int c = 0; c < n.getNode(1).getNode(3).size(); c++ ) { 

                                
                                    if(n.getNode(1).getNode(3).getNode(c).getName().equals("StringLiteral"))
                                           p2.print(n.getNode(1).getNode(0).getString(0) + "->" + "__vptr->" + n.getNode(1).getString(2) + "_" + "String" + "(" + n.getNode(1).getNode(0).getString(0) + " , " + " new __String("  + n.getNode(1).getNode(3).getNode(c).getString(0) + ") ) << std::endl; "); 
                                           p2.println(); 

                          }





                       }









                     }
                      else if ( n.toString().contains("InstanceOfExpression")) { 

                         // p3.println(" Class k = " + n.getNode(1).getNode(0).getString(0) + "->__vptr->getClass(" + n.getNode(1).getNode(0).getString(0) + ");");
                         // p3.println(" Class d = " + n.getNode(1).getNode(1).getString(0) + "->__vptr->getClass(" + n.getNode(1).getNode(1).getString(0) + ");" );
                         // p3.println("k->__vptr->isInstance(k,d);"); 
                          p2.print(n.getNode(1).getNode(0).getString(0) + "->__vptr->getClass(" + n.getNode(1).getNode(0).getString(0) + ")" + "->" + "isInstance(" + n.getNode(1).getNode(0).getString(0) + "->__vptr->getClass(" + n.getNode(1).getNode(0).getString(0) + ")" + " , " + n.getNode(1).getNode(1).getNode(0).getString(0) + ") ) << std::endl;");
                          p2.println(); 
                       }


                     else if ( n.getNode(1).getName().equals("CallExpression") && (n.getNode(1).toString().contains("toString")) && n.getNode(1).getNode(0).getName().equals("SelectionExpression")) {
                        p2.println( n.getNode(1).getNode(0).getNode(0).getString(0) + "->" + n.getNode(1).getNode(0).getString(1) + "->" + "__vptr->" + n.getNode(1).getString(2) + "(" + n.getNode(1).getNode(0).getNode(0).getString(0)  + "->" + n.getNode(1).getNode(0).getString(1) + ")" + " << std::endl;"); 

                     }
                     // hack for 18  
                     else if (n.toString().contains("x") && nestedBlock) { 
                       p2.println( "__" + classopener + "::" + n.getNode(1).getString(0) + " <<  std::endl;"); 

                     }
                     // turn on hack for static method 
                     else if(n.toString().contains("x") && !(nestedBlock) && !(staticMethodOn) && !(n.getNode(1).toString().contains(classopener))) { 
                       p2.println(" " + n.getNode(1).getString(0) + " << std::endl;"); 

                     }
                       else if(n.toString().contains("x") && !(nestedBlock) && !(staticMethodOn) && (n.getNode(1).toString().contains(classopener))) { 
                       p2.println(" " + "__" + classopener + "::"  +  n.getNode(1).getString(1) + " << std::endl;"); 

                     }


                     else if( staticMethodOn) { 
                       p2.println(" " + "__" + classopener + "::" + "x( new " + "__" + classopener + "())" + " << std::endl;");


                     }



                  }


                 


                  public void visit(Node n) {
                       for (Object o : n) if (o instanceof Node) dispatch((Node) o);
                   }
                   
               }.dispatch(n.getNode(7));
                   p2.println("}"); 

             p3.println("int main(int argc, char* argv[]) {");
             p3.println();
             p3.println("  __rt::Ptr<__rt::Array<String> > args = new __rt::Array<String>(argc -1);");
             p3.println();
             p3.println("  for ( int32_t i = 1; i < argc; i++) { ");
             p3.println(); 
             p3.println("     (*args)[i-1] = __rt::literal(argv[i]);");
             p3.println(); 
             p3.println("  } ");
             p3.println();
             p3.println("__" + classopener + "::main(args);");
             p3.println(); 
             p3.println("return 0;");  
             p3.println();  
             p3.println("}");   


}


    else if (parts[0].equals(classopener)){
        // Keep a Tally of whether a constructor was explicitly created 

        constructorCounter++; 
       // p2.println("Ankit"); 


        // Constructor with parameters 
        if ( n.getNode(4).size() >= 1) { 

            p2.print("__" + classopener + "::__" + classopener + "( ");

            for ( int i = 0; i < n.getNode(4).size(); i++) {

                if ( i != n.getNode(4).size()-1)
                    p2.print(n.getNode(4).getNode(i).getNode(1).getNode(0).getString(0) + "  " + n.getNode(4).getNode(i).getString(3) + "  , ");
                else 
                    p2.print(n.getNode(4).getNode(i).getNode(1).getNode(0).getString(0) + "  " + n.getNode(4).getNode(i).getString(3) + "  ) ");
            }  

            p2.print(" : __vptr(&__vtable) { "); 
            p2.println(); 
           // p2.flush(); 
           // p2.close(); 
            // May need to fix this later 
           if (fieldsThatGoIntoConstructor.size() >= 1) { 

            
                  for ( int field = 0; field < fieldsThatGoIntoConstructor.size(); field++)
                       p2.println(fieldsThatGoIntoConstructor.get(field)); 

            



            }


           

                if ( n.getNode(7).size() >= 1) { 


                     new Visitor() {

                      public void visitBlock(GNode n) { 

                          visit(n); 

                        }
                    
                        public void visitExpressionStatement(GNode n) {

                            visit(n); 
                        }

                        public void visitExpression(GNode n) { 

                           if(n.toString().contains("StringLiteral") || n.toString().contains("Integerliteral")) 
                                visit(n); 

                           else {


                              if ( n.getNode(0).getName().equals("PrimaryIdentifier")) {
                                  p2.println("     " +  "__" + classopener + "::" + n.getNode(0).getNode(0).getString(0) + "  = " + n.getNode(2).getString(0)  + ";"); 

                              }

                              else if (n.getNode(0).getName().equals("SelectionExpression") && n.getNode(2).getName().equals("PrimaryIdentifier") ) {
                                  p2.println("    "  + "__" + classopener + "::" + n.getNode(0).getString(1) + " = " + n.getNode(2).getString(0) + ";");  

                              }



                           }   



                        }

                        public void visitThisExpression(GNode n) { 

                            p2.print("__" + classopener + "::"+ n.getString(0));

                        }

                        public void visitStreamOutputList(GNode n) {
                            if(n.getNode(1).toString().equals("PrimaryIdentifier"))
                                 p2.println( "   " + n.getNode(0).getString(0) + " <<  " + "__" + classopener + "::" + n.getNode(1).getString(0) + " <<  "  + n.getNode(2).getString(0)  + ";");
                            else 
                                 p2.println( "   "  + n.getNode(0).getString(0) + " <<  " +  n.getNode(1).getString(0) + "  <<  "  + n.getNode(2).getString(0) + " ;"  );       
                          
                         }
                         public void visitPrimaryIdentifier(GNode n) {
                            
                            if (!(n.toString().contains("ThisExpression")))
                                p2.print(n.getString(0) + " "); 

                          else 
                            visit(n); 
                          
                         


                         }

                         public void visitStringLiteral(GNode n) { 

                            p2.print( " = " + "__rt::literal(" + n.getString(0) + ")" + ";"); 
                            p2.println(); 
                            

                         }
                   
                   public void visit(Node n) {
                       for (Object o : n) if (o instanceof Node) dispatch((Node) o);
                   }
                   
               }.dispatch(n);



                }

            p2.println(" } "); 
            p2.println(); 
             // Basically the one static vtable 
             p2.println("__" + classopener + "_VT " + "__" + classopener + ":: __vtable;");

        }

      // Constructor without parameters 
        else { 

            p2.println("__" + classopener + "::" + "__" + classopener + "() : __vptr(&__vtable) { ");
             if(extendsAnotherClass)
                p2.println("    "  + "__" + classopener  + "::" + "init(  this );");
            // May need to fix this later 
            if (fieldsThatGoIntoConstructor.size() == 1) { 

              p2.println("    " + fieldsThatGoIntoConstructor.get(0)); 
              //p2.flush();
              //p2.close(); 
            }


            if ( n.getNode(7).size() >= 1) { 

          
                new Visitor() {

                      public void visitBlock(GNode n) { 

                          visit(n); 

                        }
                    
                        public void visitExpressionStatement(GNode n) {

                            visit(n); 
                        }

                        public void visitExpression(GNode n) { 

                          if(n.toString().contains("StringLiteral") && n.toString().contains("ThisExpression") && n.toString().contains("PrimaryIdentifier") ) { 

                            replicateForInitMethod.add("__this->" + n.getNode(0).getNode(0).getString(0) + " = " + "__rt::literal(" + n.getNode(2).getString(0) + ");");


                          }






                           if(n.toString().contains("StringLiteral") || n.toString().contains("Integerliteral")) 
                                visit(n); 

                           else {


                              if ( n.getNode(0).getName().equals("PrimaryIdentifier") && !(n.getNode(2).getName().equals("ThisExpression"))){
                                  p2.println("     " + "__" + classopener + "::" + n.getNode(0).getNode(0).getString(0) + "  = " + n.getNode(2).getString(0)  + ";"); 
                                  
                              }

                              else  if ( n.getNode(2).getName().equals("ThisExpression")) {
                                  p2.println(" //    " + "__" + classopener + "::" + n.getNode(0).getNode(0).getString(0) + "  = " + "this"  + ";"); 
                                  repicateforInitConstruct.add("__this->" + n.getNode(0).getNode(0).getString(0) + " =  " + "__this;"); 
                                  //p2.flush();
                                  //p2.close(); 
                              }


                           }   

                        }

                        public void visitThisExpression(GNode n) { 

                            p2.print("     " + "__" + classopener + "::" + n.getString(0));
                           // replicateForInitMethod.add("__this->" + n.getString(0)); 

                        }

                        public void visitStreamOutputList(GNode n) {
                            if(n.getNode(1).toString().equals("PrimaryIdentifier")) {
                                 p2.println( "   " + n.getNode(0).getString(0) + " <<  " + "__" + classopener + "::" + n.getNode(1).getString(0) + " <<  "  + n.getNode(2).getString(0)  + ";");
                                 //replicateForInitMethod.add("   " + n.getNode(0).getString(0) + " << " +  "__this->" + n.getNode(1).getString(0) + " <<  "  + n.getNode(2).getString(0)  + ";") ;

                             }    
                            else {
                                 p2.println( "   "  + n.getNode(0).getString(0) + " <<  " + "__" + classopener + "::" + n.getNode(1).getString(0) + "  <<  "  + n.getNode(2).getString(0) + " ;"  );       
                                 replicateForInitMethod.add( "   "  + n.getNode(0).getString(0) + " <<  " + "__this->" + n.getNode(1).getString(0) + "  <<  "  + n.getNode(2).getString(0) + " ;" ) ;

                            }
                         }
                         public void visitPrimaryIdentifier(GNode n) {
                            
                            if (!(n.toString().contains("ThisExpression")))
                                p2.print("     " + "__" + classopener + "::" + n.getString(0) + " "); 

                          else 
                            visit(n); 
                          
                         


                         }

                         public void visitStringLiteral(GNode n) { 

                            p2.print( " = " + " __rt::literal(" + n.getString(0) + ")" + ";"); 
                            p2.println(); 
                            

                         }
                   
                   public void visit(Node n) {
                       for (Object o : n) if (o instanceof Node) dispatch((Node) o);
                   }
                   
               }.dispatch(n);


            }
              


             p2.println(" } "); 
             p2.println(); 
            p2.println("__" + classopener + "_VT " + "__" + classopener + ":: __vtable;");
            p2.println(); 
            p2.println(); 
            p2.println(" void  " + "__" + classopener + "::" + "init(" + "__" +classopener + "*" + "  __this" + ")" + "{"); 
             if(extendsAnotherClass)
                p2.println("    "  + "__" + superClass  + "::" + "init(" +" (" + "__" +superClass + "*" + ")" + " __this " + ");");
            if (replicateForInitMethod.size()>= 1 && !(extendsAnotherClass)) {

               for ( int i = 0; i < replicateForInitMethod.size(); i++)
                p2.println("   " + replicateForInitMethod.get(i)); 



             }   
            
            p2.println(); 
            p2.println("}"); 

        }




    }
   
   // PLAIN OLD METHODS 
    else {
  
        // printer.p(partsofmethod[0]).pln().pln().pln();
        // printer.p(partsofmethod[1]).pln().pln().pln();
        // printer.p(partsofmethod[2]).pln().pln().pln();
        // printer.p(partsofmethod[3]).pln().pln().pln();
        // printer.p(partsofmethod[4]).pln().pln().pln();
        // printer.p(partsofmethod[5]).pln().pln().pln();
      

      // Instance Method 
        if ( n.getNode(2).size() > 0 && !(n.toString().contains("static")) )
            p2.print(n.getNode(2).getNode(0).getString(0) + "  __" + classopener + "::");

        else if ( n.getNode(2).size() > 1 && (n.toString().contains("static")) && n.toString().contains("int") && !(n.toString().contains("long"))) { 
        
             p2.print("  " + "int32_t" + " __" + classopener + "::"); 
             staticMethodOn = true; 
            // p2.flush();
            // p2.close(); 

        }

      
        else
          p2.print("void" + "  __" + classopener + "::");

        
        // Basically it has no Arguements other than an implicit this 
        if(n.getNode(4).size() >= 1)
             //p2.print(n.getString(3) + " ( ");

        { 


          p2.print(n.getString(3)); 
           new Visitor() { 

              public void visitFormalParameters(GNode n) {
               // p2.print(n.getString(3) );
                visit(n); 
                p2.print(" ( "); 
              }


              public void visitFormalParameter(GNode n) { 

                  if(n.getNode(1).toString().contains("QualifiedIdentifier") || n.getNode(1).toString().contains("PrimitiveType")) {
                    p2.print("_"); 
                    p2.print(n.getNode(1).getNode(0).getString(0));
                  }


              }

              public void visit(Node n) {
                       for (Object o : n) if (o instanceof Node) dispatch((Node) o);
              }


           }.dispatch(n.getNode(4)); 







        }



  if(n.getNode(4).size()==0)
            p2.println(n.getString(3) + "(" + classopener + " __this ) { ");
      else {

        p2.print(classopener + "  __this , ");
    
        for ( int i = 0; i < n.getNode(4).size(); i++ ) { 

        if ( i != n.getNode(4).size()-1)
            p2.print(n.getNode(4).getNode(i).getNode(1).getNode(0).getString(0) + " " + n.getNode(4).getNode(i).getString(3) + "  " + ",");
        else 
            p2.println(n.getNode(4).getNode(i).getNode(1).getNode(0).getString(0) + " " + n.getNode(4).getNode(i).getString(3) + "  " + ")  {");


        }

      }

        if ( n.getNode(7).toString().contains("FieldDeclaration")) { 


            p2.println("    " + n.getNode(7).getNode(0).getNode(1).getNode(0).getString(0) + "  "  + n.getNode(7).getNode(0).getNode(2).getNode(0).getString(0) + ";"); 

        }
        
        
       new Visitor() { 


        public void visitBlock(GNode n){ 
           // if(n.toString().contains())
            visit(n); 

        }

        public void visitExpressionStatement(GNode n ) { 

            if ( n.getNode(0).getName().equals("CallExpression")  && n.getNode(0).getNode(0).getName().equals("PrimaryIdentifier")) { 

              p2.println("  __" + n.getNode(0).getNode(0).getString(0) + "::" + n.getNode(0).getString(2) + "( __this ); "  ); 


            }

        }


         public void visit(Node n) {
                       for (Object o : n) if (o instanceof Node) dispatch((Node) o);
                   }

       }.dispatch(n); 

      //printer.p(n.getNode(7).toString()).pln().pln();
      isOpenLine = false;
     // String __A::toString( A __this) { 
     
     //     std::ostringstream sout;
     //     sout << "A";
     //     return new __String(sout.str());
         
     
     
     // }
       

        //printer.p("methodname:").p(n.getString(3)).pln();
        //printer.p("returntype:").p(n.getNode(2)).pln();
        


      if (null != n.get(7)) {
        isOpenLine = true;
        printer.p(n.getNode(7));
      } 
    }

    isOpenLine = true;
   // p2.println("}");
  }
		
  /** Visit the specified constructor declaration. */
  public void visitConstructorDeclaration(GNode n) { 
    printer.indent().p(n.getNode(0));
    if (null != n.get(1)) printer.p(n.getNode(1));
    printer.p(n.getString(2)).p(n.getNode(3));
    if(null != n.get(4)) {
      printer.p(n.getNode(4));
    }
    isOpenLine = true;
    printer.p(n.getNode(5));
  }
  // The class name 
  String classopener;
  String superClass; 
         //    HashMap<String> referenceTypes = new ArrayList<String>(); 

  // Are there any Explicitily defined Constructors 
  int constructorCounter;  
                      
  boolean nestedBlock = false; 

  boolean staticMethodOn = false; 

  // What goes into the constructor 
  ArrayList<String> fieldsThatGoIntoConstructor = new ArrayList<String>(); 

  // Gets Defined Regardless 
  ArrayList<String> replicateForInitMethod      = new ArrayList<String>(); 


    ArrayList<String> repicateforInitConstruct = new ArrayList<String>(); 

 boolean extendsAnotherClass; 

  /** Visit the specified class declaration. */
  public void visitClassDeclaration(GNode n) {
    //printer.p(n.toString());
    classopener = (n.getString(1));
  //  ArrayList<String> repicateforInitConstruct = new ArrayList<String>(); 

    if(repicateforInitConstruct.size() == 1)
        repicateforInitConstruct.remove(0); 
   // p2.println(classopener); 

    // Reset Flags 
    constructorCounter = 0; 


    // Clear the Data Structure 
    fieldsThatGoIntoConstructor.clear(); 


    // Now do a check for Extension 

    if ( n.getGeneric(3) != null) {
      
      if ( n.getGeneric(3).size() == 1) { 
          // put flag on whether to call the superclass's init method 
          extendsAnotherClass = true;
          superClass = n.getNode(3).getNode(0).getNode(0).getString(0); 
          //p3.print("true");  
          p2.println(); 
          p2.println(" Class " + "__" + classopener + "::" + "__class() { "); 
          p2.println("   static Class k  = "); 
          p2.println("   new __Class(__rt::literal(\"java.lang." + classopener + "\") , " +  "__" + superClass + "::__class" + "());"); 
          p2.println("   return k; "); 
          p2.println("}"); 



      }
    }

    else { 
          p2.println(); 
          p2.println(" Class " + "__" + classopener + "::" + "__class() { "); 
          p2.println("    static Class k  = "); 
          p2.println("    new __Class(__rt::literal(\"java.lang." + classopener + "\") , " +  "__" + "Object::__class" + "());"); 
          p2.println("    return k; "); 
          p2.println(" }"); 

   }




     
    //printer.p(n.toString()).pln().pln().pln();

    // if (null != n.get(3)) {
    //   printer.p(' ').p(n.getNode(3));
    // }
    // if (null != n.get(4)) {
    //   printer.p(' ').p(n.getNode(4));
    // }
     isOpenLine    = true;
     printer.p(n.getNode(5)).pln();
     isDeclaration = true;
     isOpenLine    = false;

     if( constructorCounter == 0 ) {
            p2.println();
            p2.println(); 
            p2.println("__" + classopener + "::" + "__" + classopener + "()" + " : __vptr(&__vtable) {"); 
            // May need to fix this later 
            if (fieldsThatGoIntoConstructor.size() >= 1) { 

            
                  for ( int field = 0; field < fieldsThatGoIntoConstructor.size(); field++)
                       p2.println("   " + fieldsThatGoIntoConstructor.get(field)); 

            



            }
            p2.println("}"); 
            p2.println(); 
            // Basically the One Static vtable 
     
            p2.println("__" + classopener + "_VT " + "__" + classopener + ":: __vtable;");
      
      }


      p2.println(); 
    p2.println( classopener + " __" + classopener + "::" + "init_Construct(" + classopener + " __this ) {");
    if(repicateforInitConstruct.size()==1)
    p2.println(repicateforInitConstruct.get(0)); 
    p2.println(); 
    p2.println(); 
    p2.println("    return __this; "); 
    p2.println(" } "); 


    // Array Stuff 
    p2.println(); 
    p2.println(" // Array Specialization for Java Classes "); 
    p2.println(); 
    p2.println(" namespace __rt { "); 
    p2.println(" template<>"); 
    p2.println(" java::lang::Class Array<" + classopener + ">::__class() {");
    p2.println(" static java::lang::Class k = "); 
    p2.println(" new java::lang::__Class(literal(\"[L" + classopener +";\"),"); 
    p2.println("                         java::lang::__Object::__class(),"); 
    p2.println("                         " + "__" + classopener +"::__class());"); 
    p2.println(" return k; "); 
    p2.println(" }"); 
    p2.println(" }"); 
    p2.println(); 










  }
		
  /** Visit the specified interface declaration. */
  public void visitInterfaceDeclaration(GNode n) {
    printer.indent().p(n.getNode(0)).p("interface ").p(n.getString(1)).
      p(n.getNode(2));
    if (null != n.get(3)) {
      printer.p(' ').p(n.getNode(3));
    }
    isOpenLine    = true;
    printer.p(n.getNode(4)).pln();
    isDeclaration = true;
    isOpenLine    = false;
  }

  /** Visit the specified annotation declaration. */
  public void visitAnnotationDeclaration(GNode n) {
    printer.indent().p(n.getNode(0)).p("@interface ").p(n.getString(1));
    isOpenLine    = true;
    printer.p(n.getNode(2)).pln();
    isDeclaration = true;
    isOpenLine    = false;
  }

  /** Visit the specified annotation method. */
  public void visitAnnotationMethod(GNode n) {
    printer.indent().p(n.getNode(0)).p(n.getNode(1)).p(' ').p(n.getString(2)).
      p("()");
    if (null != n.get(3)) printer.p(" default ").p(n.getNode(3));
    printer.pln(';');
    isOpenLine = false;
  }

  /** Visit the specified enum declaration. */
  public void visitEnumDeclaration(GNode n) {
    printer.indent().p(n.getNode(0)).p("enum ").p(n.getString(1));
    if (null != n.get(2)) {
      printer.p(' ').p(n.getNode(2));
    }
    printer.pln(" {").incr();

    isOpenLine = false;
    isNested   = false;
    isIfElse   = false;
    printer.p(n.getNode(3));

    if (null != n.get(4)) {
      printer.pln(';').pln();

      isNested   = false;
      isIfElse   = false;
      printer.p(n.getNode(4));
    }

    if (isOpenLine) printer.pln();
    printer.decr().indent().pln('}');
    isOpenLine    = false;
    isNested      = false;
    isIfElse      = false;
    isDeclaration = true;
  }

  /** Visit the specified enum constants. */
  public void visitEnumConstants(GNode n) {
    for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
      isDeclaration = false;
      printer.indent().p((Node)iter.next());
      if (iter.hasNext()) {
        printer.pln(',');
        if (isDeclaration) printer.pln();
      }
    }
    isOpenLine = true;
  }

  /** Visit the specified enum constant. */
  public void visitEnumConstant(GNode n) {
    printer.p(n.getNode(0)).p(n.getString(1)).p(n.getNode(2));
    if (null != n.get(3)) {
      isOpenLine = true;
      printer.p(n.getNode(3));
      isDeclaration = true;
    } else {
      isDeclaration = false;
    }
  }

  /** Visit the specified enum members. */
  public void visitEnumMembers(GNode n) {
    printDeclsAndStmts(n);
  }

  /** Visit the specified block declaration. */
  public void visitBlockDeclaration(GNode n) {
    printer.indent();
    if (null != n.get(0)) {
      printer.p(n.getString(0));
      isOpenLine = true;
    } 
    printer.p(n.getNode(1)).pln();
    isOpenLine = false;
  }

  /** Visit the specific empty declaration. */
  public void visitEmptyDeclaration(GNode n) {
    // Nothing to do.
  }

  /** Visit the specified throws clause. */
  public void visitThrowsClause(GNode n) {
    printer.p("throws ");
    for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
      printer.p((Node)iter.next());
      if (iter.hasNext()) printer.p(", ");
    }
  }

  /** Visit the specified extension. */
  public void visitExtension(GNode n) {
    //p2.println("Extends");
    printer.p("extends ");
    for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
      printer.p((Node)iter.next());
      if (iter.hasNext()) printer.p(", ");
    }
   }
  
  /** Visit the specified implementation. */
  public void visitImplementation(GNode n) {
    printer.p("implements ");
    for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
      printer.p((Node)iter.next());
      if (iter.hasNext()) printer.p(", ");
    }
  }		
   
  /** Visit the specified block. */
  public void visitBlock(GNode n) {
    if (isOpenLine) {
      printer.p(' ');
    } else {
      printer.indent();
    }
    printer.pln().incr();

    isOpenLine    = false;
    isNested      = false;
    isIfElse      = false;
    isDeclaration = false;
    isStatement   = false;

    printDeclsAndStmts(n);

    printer.decr().indent().p('}');
    isOpenLine    = true;
    isNested      = false;
    isIfElse      = false;
    p2.println("}");
  }

  /** Visit the specified conditional statement. */
  public void visitConditionalStatement(GNode n) {
    final int     flag   = null == n.get(2) ? STMT_IF : STMT_IF_ELSE;
    final boolean nested = startStatement(flag);
    if (isIfElse) {
      printer.p(' ');
    } else {
      printer.indent();
    }
    printer.p("if (").p(n.getNode(0)).p(')');
    prepareNested();
    printer.p(n.getNode(1));
    if (null != n.get(2)) {
      if (isOpenLine) {
        printer.p(" else");
      } else {
        printer.indent().p("else");
      }
      prepareNested();
      boolean ifElse = isIfElse;
      isIfElse       = true;
      printer.p(n.getNode(2));
      isIfElse       = ifElse;
    }
    endStatement(nested);
  }

  /** Visit the specified for statement. */
  public void visitForStatement(GNode n) {
    final boolean nested = startStatement(STMT_ANY);

    printer.indent().p("for (").p(n.getNode(0)).p(')');
    prepareNested();
    printer.p(n.getNode(1));
		
    endStatement(nested);
  }

  /** Visit the specified basic for control. */
  public void visitBasicForControl(GNode n) {
    printer.p(n.getNode(0));
    if (null != n.get(1)) printer.p(n.getNode(1)).p(' ');

    final int prec1 = enterContext(PREC_BASE);
    printer.p(n.getNode(2)).p("; ");
    exitContext(prec1);

    if (null != n.get(3)) {
      final int prec2 = enterContext(PREC_BASE);
      formatAsTruthValue(n.getNode(3));
      exitContext(prec2);
    }
    printer.p("; ");

    final int prec3 = enterContext(PREC_BASE);
    printer.p(n.getNode(4));
    exitContext(prec3);
  }

  /** Visit the specified enhanced for control. */
  public void visitEnhancedForControl(GNode n) {
    printer.p(n.getNode(0)).p(n.getNode(1)).p(' ').p(n.getString(2)).p(" : ");
    
    final int prec = enterContext(PREC_BASE);
    printer.p(n.getNode(3));
    exitContext(prec);
  }
  
  /** Visit the specified while statement. */
  public void visitWhileStatement(GNode n) {
    final boolean nested = startStatement(STMT_ANY);
    printer.indent().p("while (").p(n.getNode(0)).p(')');
    prepareNested();
    printer.p(n.getNode(1));
    endStatement(nested);
  }

  /** Visit the specified do while statement. */
  public void visitDoWhileStatement(GNode n) {
    final boolean nested = startStatement(STMT_ANY);
    printer.indent().p("do");
    prepareNested();
    printer.p(n.getNode(0));
    if (isOpenLine) {
      printer.p(' ');
    } else {
      printer.indent();
    }
    printer.p("while (").p(n.getNode(1)).pln(");");
    endStatement(nested);
    isOpenLine = false;
  }

  /** Visit the specified try catch finally statement. */
  public void visitTryCatchFinallyStatement(GNode n) {
    final boolean nested = startStatement(STMT_ANY);

    printer.indent().p("try");
    if (null != n.get(0)) printer.p(" (").p(n.getNode(0)).p(')');

    isOpenLine = true;
    printer.p(n.getNode(1)).p(' ');

    final Iterator<Object> iter = n.iterator();
    iter.next(); // Skip resource specification.
    iter.next(); // Skip try block.
    while (iter.hasNext()) {
      final GNode clause = GNode.cast(iter.next());

      isOpenLine = true;
      if (iter.hasNext()) {
        printer.p(clause).p(' ');
      } else if (null != clause) {
        printer.p("finally").p(clause);
      }
    }

    endStatement(nested);
  }

  /** Visit the specified resource specification. */
  public void visitResourceSpecification(GNode n) {
    final int align = printer.column();
    for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
      final GNode rsrc = GNode.cast(iter.next());
      printer.p(rsrc.getNode(0)).p(rsrc.getNode(1)).p(' ').p(rsrc.getNode(2));
      if (iter.hasNext()) printer.pln(';').align(align);
    }
  }
  
  /** Visit the specified catch clause. */
  public void visitCatchClause(GNode n) {
    printer.p("catch (").p(n.getNode(0)).p(")").p(n.getNode(1));
  }

  /** Visit the specified switch statement. */
  public void visitSwitchStatement(GNode n) {
    final boolean nested = startStatement(STMT_ANY);

    final int prec = enterContext(PREC_CONSTANT);
    printer.indent().p("switch (").p(n.getNode(0)).pln(") {").incr();
    exitContext(prec);

    isOpenLine = false;
    isNested   = false;
    isIfElse   = false;
    final Iterator<Object> iter = n.iterator();
    iter.next(); // Skip switch expression.
    while (iter.hasNext()) {
      printer.p((Node)iter.next());
    }

    if (isOpenLine) {
      printer.pln();
    }
    printer.decr().indent().p('}');

    isOpenLine = true;
    isNested   = false;
    isIfElse   = false;
    endStatement(nested);
  }

  /** Visit the specified case clause. */
  public void visitCaseClause(GNode n) {
    final boolean nested = startStatement(STMT_ANY);

    final int prec = enterContext(PREC_CONSTANT);
    printer.indentLess().p("case ").p(n.getNode(0)).pln(':');
    exitContext(prec);

    isOpenLine = false;
    isNested   = false;
    isIfElse   = false;
    final Iterator<Object> iter = n.iterator();
    iter.next(); // Skip case expression.
    while (iter.hasNext()) {
      printer.p((Node)iter.next());
    }

    endStatement(nested);
  }

  /** Visit the specified default clause. */
  public void visitDefaultClause(GNode n) {
    final boolean nested = startStatement(STMT_ANY);
    printer.indentLess().pln("default:");
    isOpenLine = false;
    isNested   = false;
    isIfElse   = false;
    for (Object o : n) printer.p((Node)o);
    endStatement(nested);
  }

  /** Visit the specified synchronized statement. */
  public void visitSynchronizedStatement(GNode n) {
    final boolean nested = startStatement(STMT_ANY);
    printer.indent().p("synchronized (").p(n.getNode(0)).p(')');
    prepareNested();
    printer.p(n.getNode(1));
    endStatement(nested);
  }

  

  /** Visit the specified return statement. */
  public void visitReturnStatement(GNode n) {

    if(n.toString().contains("AdditiveExpression")) {

        p2.println(" std::ostringstream sout;");

        new Visitor() { 

          public void visitAdditiveExpression(GNode n) { 

              visit(n);


          }
          public void visitThisExpression (GNode n) { 

            p2.println("sout << " + "__this->" + n.getString(0) + ";"); 
            p2.println(" sout << \"     \" ;"); 
          }



         public void visit(Node n) {
                       for (Object o : n) if (o instanceof Node) dispatch((Node) o);
         }



        }.dispatch(n); 





        p2.print("return new __String(sout.str()); ");



    }  



  else  if(n.toString().contains("NewClassExpression")) { 

      p2.println("    return new __" + n.getNode(0).getNode(2).getString(0) + "();"); 


    }



   else if ((n.toString()).contains("StringLiteral") && !(n.toString().contains("CallExpression"))){
      

      p2.println("   std::ostringstream sout;");
      p2.println("   sout <<" + n.getNode(0).getString(0) + ";");
      p2.println("   return new __String(sout.str());");
    }


    else if ((n.toString()).contains("ThisExpression") && !(n.toString().contains("CallExpression"))){
      retu = n.getNode(0).getNode(0).getString(0);
      p2.println("   return __this->" + retu + ";"); 

     }
    else if(n.toString().contains("IntegerLiteral") && !(n.toString().contains("CallExpression"))) { 
      p2.println(" return " + n.getNode(0).getString(0) + ";"); 

    }
    else if (n.toString().contains("PrimaryIdentifier") && !(n.toString().contains("CallExpression"))) { 

      p2.println(" return " + n.getNode(0).getString(0) + ";"); 
    }

    else if ( n.toString().contains("CallExpression")) { 
                      p2.println("Class k = __this->__vptr->getClass(__this);"); 
                      p2.println(" return k->__vptr->getName(k);");

                   
    }

    else{ printer.p(n.toString()).pln().pln();

}

    
    isOpenLine = false;


  }

  /** Visit the specified throw statement. */
  public void visitThrowStatement(GNode n) {
    final boolean nested = startStatement(STMT_ANY);
    printer.indent().p("throw").p(' ').p(n.getNode(0));
    printer.pln(';');
    endStatement(nested);
    isOpenLine = false;
  }

  /** Visit the specified break statement. */
  public void visitBreakStatement(GNode n) {
    final boolean nested = startStatement(STMT_ANY);
    printer.indent().p("break");
    if ((n.getString(0)) != null) {
      printer.p(' ').p(n.getString(0));
    }
    printer.pln(';');
    endStatement(nested);
    isOpenLine = false;
  }

  /** Visit the specified continue statement. */
  public void visitContinueStatement(GNode n) {
    final boolean nested = startStatement(STMT_ANY);
    printer.indent().p("continue");
    if (null != n.getString(0)) {
      printer.p(' ').p(n.getString(0));
    }
    printer.p(';').pln();
    endStatement(nested);
    isOpenLine = false;
  }

  /** Visit the specified labeled statement. */
  public void visitLabeledStatement(GNode n) {
    final boolean nested = startStatement(STMT_ANY);
    printer.indent().p(n.getString(0)).p(": ").p(n.getNode(1)).pln();
    endStatement(nested);
    isOpenLine = false;
  }

  /** Visit the specified expression statement. */
  public void visitExpressionStatement(GNode n) {
    final boolean nested = startStatement(STMT_ANY);
    final int     prec   = enterContext(PREC_BASE);
    
    if(n.getNode(0).getName().equals("Expression")) { 


        // Check for appending the __this  infront and setting it equals to it
        if(n.getNode(0).getNode(0).toString().contains("ThisExpression")) {

            p2.print("    __this->" + n.getNode(0).getNode(0).getNode(0).getString(0) + " = " + n.getNode(0).getNode(2).getString(0) + ";" );
            p2.println(); 
        }

        else { 

           p2.println( "    "  + n.getNode(0).getNode(0).getString(0) + " = " + n.getNode(0).getNode(2).getString(0) + ";");  


        }



    }

    printer.indent().p(n.getNode(0)).pln(';');
    exitContext(prec);
    endStatement(nested);
    isOpenLine = false;
        
    




  }

  /** Visit the specified assert statement. */
  public void visitAssertStatement(GNode n) {
    final boolean nested = startStatement(STMT_ANY);
    printer.indent().p("assert ").p(n.getNode(0));
    if (null != n.get(1)) {
      printer.p(" : ").p(n.getNode(1));
    }
    printer.pln(';');
    endStatement(nested);
    isOpenLine = false;
  }

  /** Visit the specified empty statement. */
  public void visitEmptyStatement(GNode n) {
    final boolean nested = startStatement(STMT_ANY);
    printer.indent().pln(';');
    endStatement(nested);
    isOpenLine = false;
  }


  /** Visit the specified expression list. */
  public void visitExpressionList(GNode n) {
    for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
      final int prec = enterContext(PREC_LIST);
      printer.p((Node)iter.next());
      exitContext(prec);
      if (iter.hasNext()) printer.p(", ");
    }
  }
  
  /** Visit the specified expression. */
  public void visitExpression(GNode n) {
   final int prec1 = startExpression(10);
    final int prec2 = enterContext();
    printer.p(n.getNode(0));
    exitContext(prec2);

    printer.p(' ').p(n.getString(1)).p(' ').p(n.getNode(2));
    endExpression(prec1);

  }

  /** Visit the specified conditional expression. */
  public void visitConditionalExpression(GNode n) {
    final int prec1 = startExpression(20);

    final int prec2 = enterContext();
    printer.p(n.getNode(0)).p(" ? ");
    exitContext(prec2);

    final int prec3 = enterContext();
    if (null != n.get(1)) {
      printer.p(n.getNode(1)).p(" : ");
    } else {
      printer.p(" /* Empty */ : ");
    }
    exitContext(prec3);

    printer.p(n.getNode(2));
    endExpression(prec1);
  }


  /** Visit the specified logical or expression. */
  public void visitLogicalOrExpression(GNode n) {
    final int prec1 = startExpression(30);
    printer.p(n.getNode(0));
    printer.p(" || ");
    final int prec2 = enterContext();
    printer.p(n.getNode(1));
    exitContext(prec2);
    endExpression(prec1);
  }
  /** Visit the specified logical and expression. */
  public void visitLogicalAndExpression(GNode n) {
    final int prec1 = startExpression(40);
    printer.p(n.getNode(0));
    printer.p(" && ");
    int prec2 = enterContext();
    printer.p(n.getNode(1));
    exitContext(prec2);
    endExpression(prec1);
  }
  
  /** Visit the specified bitwise or expression. */
  public void visitBitwiseOrExpression(GNode n) {
    final int prec1 = startExpression(50);
    printer.p(n.getNode(0));
    printer.p(" | ");
    final int prec2 = enterContext();
    printer.p(n.getNode(1));
    exitContext(prec2);
    endExpression(prec1);
  }
  
  /** Visit the specified bitwise xor expression. */
  public void visitBitwiseXorExpression(GNode n) {
    final int prec1 = startExpression(60);
    printer.p(n.getNode(0));
    printer.p(" ^ ");
    final int prec2 = enterContext();
    printer.p(n.getNode(1));
    exitContext(prec2);
    endExpression(prec1);
  }

  /** Visit the specified bitwise and expression. */
  public void visitBitwiseAndExpression(GNode n) {
    final int prec1 = startExpression(70);
    printer.p(n.getNode(0)).p(" & ");
    final int prec2 = enterContext();
    printer.p(n.getNode(1));
    exitContext(prec2);
    endExpression(prec1);
  }


  /** Visit the specified equality expression. */
  public void visitEqualityExpression(GNode n) {
    final int prec1 = startExpression(80);
    printer.p(n.getNode(0)).p(' ').p(n.getString(1)).p(' ');
    final int prec2 = enterContext();
    printer.p(n.getNode(2));
    exitContext(prec2);
    endExpression(prec1);
  }

  /** Visit the specified instance of expression. */
  public void visitInstanceOfExpression(GNode n) {
    final int prec1 = startExpression(90);
    printer.p(n.getNode(0)).p(' ').p("instanceof").p(' ');
    final int prec2 = enterContext();
    printer.p(n.getNode(1));
    exitContext(prec2);
    endExpression(prec1);
  }

  /** Visit the specified relational expression. */
  public void visitRelationalExpression(GNode n) {
    final int prec1 = startExpression(100);
    printer.p(n.getNode(0)).p(' ').p(n.getString(1)).p(' ');
    final int prec2 = enterContext();
    printer.p(n.getNode(2));
    exitContext(prec2);

    endExpression(prec1);
  }

  /** Visit the specified shift expression. */
  public void visitShiftExpression(GNode n) {
    final int prec1 = startExpression(110);
    printer.p(n.getNode(0));
    printer.p(' ').p(n.getString(1)).p(' ');
    final int prec2 = enterContext();
    printer.p(n.getNode(2));
    exitContext(prec2);
    endExpression(prec1);
  }

  /** Visit the specified additive expression. */
  public void visitAdditiveExpression(GNode n) {
    final int prec1 = startExpression(120);
    printer.p(n.getNode(0)).p(' ').p(n.getString(1)).p(' ');

    final int prec2 = enterContext();
    printer.p(n.getNode(2));
    exitContext(prec2);

    endExpression(prec1);
  }

  /** Visit the specified multiplicative expression. */
  public void visitMultiplicativeExpression(GNode n) {
    final int prec1 = startExpression(130);
    printer.p(n.getNode(0)).p(' ').p(n.getString(1)).p(' ');

    final int prec2 = enterContext();
    printer.p(n.getNode(2));
    exitContext(prec2);

    endExpression(prec1);
  }

  /** Visit the specified unary expression. */
  public void visitUnaryExpression(GNode n) {
    final int prec = startExpression(150);
    printer.p(n.getString(0)).p(n.getNode(1));
    endExpression(prec);
  }

  /** Visit the specified bitwise negation expression. */
  public void visitBitwiseNegationExpression(GNode n) {
    final int prec = startExpression(150);
    printer.p('~').p(n.getNode(0));
    endExpression(prec);
  }

  /** Visit the specified logical negation expression. */
  public void visitLogicalNegationExpression(GNode n) {
    final int prec = startExpression(150);
    printer.p('!').p(n.getNode(0));
    endExpression(prec);
  }
  
  /** Visit the specified basic cast expression. */
  public void visitBasicCastExpression(GNode n) {
    final int prec = startExpression(140);
    printer.p('(').p(n.getNode(0));
    if(null != n.get(1)) {
      printer.p(n.getNode(1));
    }
    printer.p(')').p(n.getNode(2));  
    
    endExpression(prec);
  }
  
  /** Visit the specified cast expression. */
  public void visitCastExpression(GNode n) {
    final int prec = startExpression(140);
    printer.p('(').p(n.getNode(0)).p(')').p(n.getNode(1));
    endExpression(prec);
  }

  /** Visit the specified call expression. */
  public void visitCallExpression(GNode n) {
    final int prec = startExpression(160);
    if (null != n.get(0)) printer.p(n.getNode(0)).p('.');
    printer.p(n.getNode(1)).p(n.getString(2)).p(n.getNode(3));
    endExpression(prec);
  }

  /** Visit the specified selection expression. */
  public void visitSelectionExpression(GNode n) {
    final int prec = startExpression(160);
    printer.p(n.getNode(0)).p('.').p(n.getString(1));
    endExpression(prec);
  }

  /** Visit the specified subscript expression. */
  public void visitSubscriptExpression(GNode n) {
    final int prec1 = startExpression(160);
    printer.p(n.getNode(0)).p('[');
    final int prec2 = enterContext(PREC_BASE);
    printer.p(n.getNode(1)).p(']');
    exitContext(prec2);
    endExpression(prec1);
  }

  /** Visit the specified postfix expression. */
  public void visitPostfixExpression(GNode n) {
    final int prec = startExpression(160);
    printer.p(n.getNode(0)).p(n.getString(1));
    endExpression(prec);
  }

  /** Visit the specified class literal expression. */
  public void visitClassLiteralExpression(GNode n) {
    final int prec = startExpression(160);
    printer.p(n.getNode(0)).p(".class");
    endExpression(prec);
  }

  /** Visit the specified this expression. */
  public void visitThisExpression(GNode n) {
    printer.p("visitThisExpression!!!");

    //p2.println("__this->" + n.getString(0));

    //p2.println("here"); 
    final int prec = startExpression(160);
    if (null != n.get(0)) printer.p(n.getNode(0)).p('.');
    printer.p("this");
    endExpression(prec);
  }

  /** Visit the specified super expression. */
  public void visitSuperExpression(GNode n) {
    final int prec = startExpression(160);
    if (null != n.get(0)) printer.p(n.getNode(0)).p('.');
    printer.p("super");
    endExpression(prec);
  }







  /** Visit the specified primary identifier. */
  public void visitPrimaryIdentifier(GNode n) {
    final int prec = startExpression(160);
   // printer.p(n.getString(0));
    
    



    //else 
     // p2.println( " " + n.getString(0)); 


    endExpression(prec);
  }	  

  /** Visit the specified new class expression. */
  public void visitNewClassExpression(GNode n) {
    final int prec = startExpression(160);
    if (null != n.get(0)) printer.p(n.getNode(0)).p('.');
    printer.p("new ");
    if (null != n.get(1)) printer.p(n.getNode(1)).p(' ');
    printer.p(n.getNode(2)).p(n.getNode(3));
    if (null != n.get(4)) {
      prepareNested();
      printer.p(n.getNode(4));
    }
    endExpression(prec);
  }

  /** Visit the specified new array expression. */
  public void visitNewArrayExpression(GNode n) {
    final int prec = startExpression(160);
    printer.p("new ").p(n.getNode(0)).p(n.getNode(1)).p(n.getNode(2));
    if (null != n.get(3)) printer.p(' ').p(n.getNode(3));
    endExpression(prec);
  }

  /** Visit the specified concrete dimensions. */
  public void visitConcreteDimensions(GNode n) {
    for (Object o : n) printer.p('[').p((Node)o).p(']');
  }

  /** Visit the specified array initlizer. */
  public void visitArrayInitializer(GNode n) {
    if (! n.isEmpty()) {
      printer.pln().incr().indent();
      for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
        printer.buffer().p((Node)iter.next());
        if (iter.hasNext()) printer.p(", ");
        printer.fit();
      }
      printer.pln().decr().indent().p('}');
    } else {
      printer.p("{ }");
    }
  }

  /** Visit the specified arguments. */
  public void visitArguments(GNode n) {
    printer.p('(');
    for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
      final int prec = enterContext(PREC_LIST);
      printer.p((Node)iter.next());
      exitContext(prec);
      if (iter.hasNext()) printer.p(", ");
    }
    printer.p(')');
  } 

  /** Visit the specified void type specifier. */
  public void visitVoidType(GNode n) {
    printer.p("void");
  }
	
  /** Visit the specified type. */
  public void visitType(GNode n) {
    printer.p(n.getNode(0));
    if (null != n.get(1)) {
      if (Token.test(n.get(1))) {
        formatDimensions(n.getString(1).length());
      } else {
        printer.p(n.getNode(1));
      }
    }
  }

  /** Visit the specified primitive type. */
  public void visitPrimitiveType(GNode n) {
    if (n.getString(0).equals("int")){
      printer.p("int32_t");
    }
  } 

  /** Visit the secified reference type. */
  public void visitInstantiatedType(GNode n) {
    boolean first = true;
    for (Object o : n) {
      if (first) first = false;
      else printer.p('.');
      printer.p((Node)o);
    }
  }

  /** Visit the specified type instantiation. */
  public void visitTypeInstantiation(GNode n) {
    printer.p(n.getString(0)).p(n.getNode(1));
  }

  /** Visit the specified dimensions. */
  public void visitDimensions(GNode n) {
    for (int i=0; i<n.size(); i++) printer.p("[]");
  }

  /** Visit the specified type parameters. */
  public void visitTypeParameters(GNode n) {
    printer.p('<');
    for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
      printer.p((Node)iter.next());
      if (iter.hasNext()) printer.p(", ");
    }
    printer.p('>');
  }

  /** Visit the specified type parameter. */
  public void visitTypeParameter(GNode n) {
    printer.p(n.getString(0));
    if (null != n.get(1)) printer.p(" extends ").p(n.getNode(1));
  }

  /** Visit the specified bound. */
  public void visitBound(GNode n) {
    for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
      printer.p((Node)iter.next());
      if (iter.hasNext()) printer.p(" & ");
    }
  }

  /** Visit the specified type arguments. */
  public void visitTypeArguments(GNode n) {
    printer.p('<');
    for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
      printer.p((Node)iter.next());
      if (iter.hasNext()) printer.p(", ");
    }
    printer.p('>');
  }

  /** Visit the specified wildcard. */
  public void visitWildcard(GNode n) {
    printer.p('?').p(n.getNode(0));
  }

  /** Visit the specified wildcard bound. */
  public void visitWildcardBound(GNode n) {
    printer.p(' ').p(n.getString(0)).p(' ').p(n.getNode(1));
  }

  /** Visit the specified integer literal. */
  public void visitIntegerLiteral(GNode n) {
    final int prec = startExpression(160);
    printer.p(n.getString(0));
    endExpression(prec);
  }

  /** Visit the specified floating point literal. */
  public void visitFloatingPointLiteral(GNode n) {
    final int prec = startExpression(160);
    printer.p(n.getString(0));
    endExpression(prec);
  }

  /** Visit the specified character literal. */
  public void visitCharacterLiteral(GNode n) {
    final int prec = startExpression(160);
    printer.p(n.getString(0));
    endExpression(prec);
  }

  /** Visit the specified string literal. */
  public void visitStringLiteral(GNode n) {
    final int prec = startExpression(160);
    printer.p(n.getString(0));
    endExpression(prec);
  }

  /** Visit the specified boolean literal. */
  public void visitBooleanLiteral(GNode n) {
    final int prec = startExpression(160);
    printer.p(n.getString(0));
    endExpression(prec);
  }

  /** Visit the specified null literal. */
  public void visitNullLiteral(GNode n) {
    final int prec = startExpression(160);
    printer.p("null");
    endExpression(prec);
  }

  /** Visit the specified qualified identifier. */
  public void visitQualifiedIdentifier(GNode n) {
    final int prec = startExpression(160);
   
    if (1 == n.size()) {
      printer.p(n.getString(0));
    } else {
      for (Iterator<Object> iter = n.iterator(); iter.hasNext(); ) {
        printer.p(Token.cast(iter.next()));
        if (iter.hasNext()) printer.p('.');
      }
    }

    endExpression(prec);
  }

}




