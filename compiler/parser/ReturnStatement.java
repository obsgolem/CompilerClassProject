package parser;

import java.util.ArrayList;

import lowlevel.*;

public class ReturnStatement extends Statement {

    Expression expr;

    public ReturnStatement (Expression e) {
        // this (express);
        expr = e;
    }

    // TODO
    public Integer genLLCode(Function func, CompoundStatement scope) throws CodeGenerationException {
        
        // Get value of expression
        Integer res = expr.genLLCode(func, scope);

        // Set value in the retreg
        Integer reg = func.getNewRegNum();
        op = new Operation(Operation.OperationType.ASSIGN, func.getCurrBlock());
        op.setSrcOperand(0, new Operand(Operand.OperandType.INTEGER, res));
        op.setDestOperand(0, new Operand(Operand.OperandType.MACRO, "RetReg"));
        func.getCurrBlock().appendOper(op);

        // Return unconditionally to return block
        Operation ret_branch = new Operation(Operation.OperationType.JMP, func.getCurrBlock());
        ret_branch.setSrcOperand(0, new Operand(Operand.OperandType.BLOCK, func.getReturnBlock()));
        func.getCurrBlock().appendOper(ret_branch);

        return -1; //statments should return -1, expr regurn regnum with value
    }

    public void printTree(int level) {
        super.printTree(level);
        System.out.println("Return statement");
        if(expr != null) { expr.printTree(level+1); }
    }
}