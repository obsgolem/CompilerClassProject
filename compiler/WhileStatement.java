package compiler;

import java.util.ArrayList;

public class WhileStatement extends Statement {

    Expression expr;
    Statement stmt;

    public WhileStatement (Expression e, Statement s) {
        // this (express, stmt);
    	expr = e;
    	stmt = s;
    }

    public void printTree(int level) {
        super.printTree(level);
        System.out.println("While statement");
        expr.printTree(level+1);
        stmt.printTree(level+1);
    }
}
