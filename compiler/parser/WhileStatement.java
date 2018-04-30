package parser;

import java.util.ArrayList;

import lowlevel.*;

public class WhileStatement extends Statement {

    Expression expr;
    Statement stmt;

    public WhileStatement (Expression e, Statement s) {
        // this (express, stmt);
    	expr = e;
    	stmt = s;
    }

    public Integer genLLCode(Function func, CompoundStatement scope) throws CodeGenerationException {
        Integer reg = expr.genLLCode(func, scope);

        Operation top_branch = new Operation(Operation.OperationType.BEQ, func.getCurrBlock());
        top_branch.setSrcOperand(0, new Operand(Operand.OperandType.REGISTER, reg));
        top_branch.setSrcOperand(1, new Operand(Operand.OperandType.INTEGER, 0));
        func.getCurrBlock().appendOper(top_branch);

        BasicBlock body = new BasicBlock(func);
        func.appendToCurrentBlock(body);

        func.setCurrBlock(body);

        stmt.genLLCode(func, scope);

        reg = expr.genLLCode(func, scope);
        Operation bottom_branch = new Operation(Operation.OperationType.BNE, func.getCurrBlock());
        bottom_branch.setSrcOperand(0, new Operand(Operand.OperandType.REGISTER, reg));
        bottom_branch.setSrcOperand(1, new Operand(Operand.OperandType.INTEGER, 0));
        bottom_branch.setSrcOperand(2, new Operand(Operand.OperandType.BLOCK, body.getBlockNum()));
        func.getCurrBlock().appendOper(bottom_branch);

        BasicBlock after = new BasicBlock(func);
        func.appendToCurrentBlock(after);
        top_branch.setSrcOperand(2, new Operand(Operand.OperandType.BLOCK, after.getBlockNum()));

        func.setCurrBlock(after);

        return -1;
    }

    public void printTree(int level) {
        super.printTree(level);
        System.out.println("While statement");
        expr.printTree(level+1);
        if(stmt != null) { stmt.printTree(level+1); }
    }
}
