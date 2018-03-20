public class IfStatement extends Statement {

    Expression expr;
    Statement thenStmt;
    Statement elseStmt;

    public WhileStatement (Expression express, Statement stmt) {
        this (express, stmt, null);
    }
}
