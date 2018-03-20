public class WhileStatement extends Statement {

    Expression expr;
    Statement thenStmt;
    Statement elseStmt;

    public WhileStatement (Expression express, Statement stmt) {
        this (express, stmt, null);
    }
}
