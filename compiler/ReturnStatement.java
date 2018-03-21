package compiler;

import java.util.ArrayList;

public class ReturnStatement extends Statement {

    Expression expr;

    public ReturnStatement (Expression e) {
        // this (express);
        expr = e;
    }
    public void printTree(int level) {
        super.printTree(level);
        System.out.println("Return statement");
        if(expr != null) { expr.printTree(level+1); }
    }
}
