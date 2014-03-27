package oop;

import xtc.util.SymbolTable;
import xtc.util.Runtime;

import xtc.tree.GNode;


/**
 * 
 * @author johnlshankman
 *
 * In essence, this an exact copy of the JavaExternalAnalyzer
 * This subclass adds the Block and ForStatement visitors
 * because JavaExternalAnalyzer does not currently visit
 * Blocks or For statements (even though they need scoping).
 * 
 * Furthermore, JavaExternalAnalyzer has been slightly modified.
 * The visitMethodDeclaration, visitClassDeclaration, and visitCompilationUnit
 * methods have all been modified so that they utilize mark(n);
 * 
 * visitMethodDeclaration has an added visit(n); call in order
 * to ensure we visit the block/body of every method. This explains
 * why we added the visitBlock and visitForStatment methods below.
 * 
 * TODO: visitPackageDeclaration in JavaExternalAnalyzer needs to 
 * utilze mark(n); but whenever I attempt to add the line, the 
 * program throws a NullPointerException right when I invoke mark(n); 
 * on the packageDeclaration GNode. No clue why.
 */

public class SymbolTableHandler extends xtc.lang.JavaExternalAnalyzer {
	
	//call to supeclass's constructor
	public SymbolTableHandler(Runtime runtime, SymbolTable table)
	{
		super(runtime, table);
	}
	
	//added in order to find local variables and scoping information within blocks
	public void visitBlock(GNode n) 
	{
		_table.enter(_table.freshName("block"));
		_table.mark(n);
		visit(n);
		_table.exit();
	}
	
	//added in order to scope the iterator within For loops
	public void visitForStatement(GNode n) 
	{
		_table.enter(_table.freshName("forStatement"));
		_table.mark(n);
		visit(n);
		_table.exit();
	}	
}




