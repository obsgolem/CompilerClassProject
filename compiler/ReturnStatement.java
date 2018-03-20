package compiler;

import java.util.ArrayList;

public class ReturnStatement extends Statement {

    Expression expr;

    public ReturnStatement (Expression e) {
        // this (express);
        expr = e;
    }
    public void printTree(int level) {
        for(int i = 0; i < level; i++) {
            System.out.print("\t");
        }
        System.out.print("Return statement");
        expr.printTree(level+1);
    }
}
