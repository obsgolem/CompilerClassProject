public class ReturnStatement extends Statement {

    Expression expr;
    Statement thenStmt;
    Statement elseStmt;

    public ReturnStatement (Expression express, Statement stmt) {
        this (express, stmt, null);
    }
}
