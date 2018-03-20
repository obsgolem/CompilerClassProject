package compiler;

import java.util.ArrayList;

public class CompoundStatement extends Statement {

	public ArrayList<Declaration.VarDecl> decls;
	public ArrayList<Statement> statements;

    public CompoundStatement (ArrayList<Declaration.VarDecl> d, ArrayList<Statement> s) {
    	decls = d;
    	statements = s;
    }

    public void printTree(int level) {
        for(int i = 0; i < level; i++) {
            System.out.print("\t");
        }
        System.out.print("Compound statement");
        for(Declaration.VarDecl d : decls) {
        	d.printTree(level+1);
        }
        for(Statement s : statements) {
        	s.printTree(level+1);
        }
    }
}
