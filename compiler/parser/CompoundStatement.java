package parser;

import java.util.ArrayList;
import java.util.HashMap;

import lowlevel.*;

public class CompoundStatement extends Statement {
    // Maintain an inverted tree of scopes so that we can do scope resultion for variables.
    CompoundStatement parent;

	public ArrayList<Declaration.VarDecl> decls;
	public ArrayList<Statement> statements;

    HashMap<String, Declaration.VarDecl> table;

    public CompoundStatement (ArrayList<Declaration.VarDecl> d, ArrayList<Statement> s, CompoundStatement p) {
    	decls = d;
    	statements = s;
        parent = p;
        table = new HashMap<String, Declaration.VarDecl>();
    }

    public CompoundStatement getParent() {
        return parent;
    }

    public HashMap<String, Declaration.VarDecl> getSymbolTable() {
        return table;
    }

    public void addTableEntry(Declaration.VarDecl decl) {
        table.put(decl.getName(), decl);
    }

    public Integer genLLCode(Function func, CompoundStatement scope) throws CodeGenerationException {
        // Generate registers for local variables.
        for(Declaration.VarDecl decl:decls) {
            decl.setRegister(func.getNewRegNum());
            addTableEntry(decl);
        }

        // Generate code for the individual statements.
        for(Statement stmt:statements) {
            stmt.genLLCode(func, this);
        }
        return -1;
    }

    public void printTree(int level) {
        super.printTree(level);

        System.out.println("Compound statement");
        for(Declaration.VarDecl d : decls) {
        	d.printTree(level+1);
        }
        for(Statement s : statements) {
            if(s != null) { s.printTree(level+1); }
        }
    }
}
