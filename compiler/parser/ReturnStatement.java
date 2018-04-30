package parser;

import java.util.ArrayList;

import lowlevel.*;

public class ReturnStatement extends Statement {

    Expression expr;

    public ReturnStatement (Expression e) {
        // this (express);
        expr = e;
    }

    public Integer genLLCode(Function func, CompoundStatement scope) throws CodeGenerationException {
        return -1;
    }

    public void printTree(int level) {
        super.printTree(level);
        System.out.println("Return statement");
        if(expr != null) { expr.printTree(level+1); }
    }
}
