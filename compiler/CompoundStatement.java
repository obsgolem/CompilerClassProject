public class CompoundStatement extends Statement {

    Expression expr;
    Statement thenStmt;
    Statement elseStmt;

    public CompoundStatement (Expression express, Statement stmt) {
        this (express, stmt, null);
    }
}
