package compiler;

import java.util.ArrayList;

public class IfStatement extends Statement {

    Expression expr;
    Statement thenStmt;
    Statement elseStmt;

    public IfStatement (Expression express, Statement stmt) {
        this (express, stmt, null);
    }

    public IfStatement (Expression express, Statement stmt1, Statement stmt2) {
        expr = express;
        thenStmt = stmt1;
        elseStmt = stmt2;
    }

    public void printTree(int level) {
        super.printTree(level);
        System.out.println("If statement");
        expr.printTree(level+1);
        if(thenStmt != null) { thenStmt.printTree(level+1); }
        if(elseStmt != null) { elseStmt.printTree(level+1); }
    }
}
