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
        for(int i = 0; i < level; i++) {
            System.out.print("\t");
        }
        System.out.print("If statement");
        expr.printTree(level+1);
        thenStmt.printTree(level+1);
        elseStmt.printTree(level+1);
    }
}
