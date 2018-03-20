public class WhileStatement extends Statement {

    Expression expr;
    Statement stmt;

    public WhileStatement (Expression e, Statement s) {
        // this (express, stmt);
    	expr = e;
    	stmt = s;
    }

    public void printTree(int level) {
        for(int i = 0; i < level; i++) {
            System.out.print("\t");
        }
        System.out.print("While statement");
        expr.printTree(level+1);
        stmt.printTree(level+1);
    }
}
