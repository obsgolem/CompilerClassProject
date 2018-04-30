package parser;

import java.util.ArrayList;

import lowlevel.*;

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

    public Integer genLLCode(Function func, CompoundStatement scope) throws CodeGenerationException {
        // Generate the branch. We do not set the destination until after we have generated the then block.
        Integer reg = expr.genLLCode(func, scope);
        Operation branch = new Operation(Operation.OperationType.BEQ, func.getCurrBlock());
        branch.setSrcOperand(0, new Operand(Operand.OperandType.REGISTER, reg));
        branch.setSrcOperand(1, new Operand(Operand.OperandType.INTEGER, 0));
        func.getCurrBlock().appendOper(branch);

        BasicBlock if_block = new BasicBlock(func);
        BasicBlock finally_block = new BasicBlock(func);

        // Generate the then block.
        func.appendToCurrentBlock(if_block);
        func.setCurrBlock(if_block);
        thenStmt.genLLCode(func, scope);
        func.appendToCurrentBlock(finally_block);

        if(elseStmt != null) {
            // If we are an else statement then our branch goes to that. Generate the else block and a jump to the then block,  then append to the unconnected chain.
            BasicBlock else_block = new BasicBlock(func);
            branch.setSrcOperand(2, new Operand(Operand.OperandType.BLOCK, else_block.getBlockNum()));

            func.setCurrBlock(else_block);

            elseStmt.genLLCode(func, scope);

            Operation else_branch = new Operation(Operation.OperationType.JMP, func.getCurrBlock());
            else_branch.setSrcOperand(0, new Operand(Operand.OperandType.BLOCK, finally_block.getBlockNum()));
            func.getCurrBlock().appendOper(else_branch);

            func.appendUnconnectedBlock(else_block);
        }
        else {
            // Else just jump to finally on false.
            branch.setSrcOperand(2, new Operand(Operand.OperandType.BLOCK, finally_block.getBlockNum()));
        }

        // After we are done we should continue codegen at the finally block.
        func.setCurrBlock(finally_block);

        return -1;
    }

    public void printTree(int level) {
        super.printTree(level);
        System.out.println("If statement");
        expr.printTree(level+1);
        if(thenStmt != null) { thenStmt.printTree(level+1); }
        if(elseStmt != null) { elseStmt.printTree(level+1); }
    }
}
