package parser;

import java.util.ArrayList;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;

public class CMinusParser {
	public class ParseException extends Exception {
		public ParseException(String s) {
			super(s);
		}
	}

	Scanner scanner;

	public CMinusParser(String s) throws FileNotFoundException, ScannerException, IOException {
		scanner = new CMinusScanner(new BufferedReader(new FileReader(s)));
	}

	public CMinusParser(Scanner s) {
		scanner = s;
	}

	private Token consumeToken(Token.TokenType type) throws ParseException, ScannerException, IOException {
		Token t = scanner.getNextToken();
		if(t.getType() != type) {
			throw new ParseException("Expected token " + type.toString() + ", got " + t.getType().toString());
		}
		return t;
	}


	public Program parseAll() throws ParseException, ScannerException, IOException {
		ArrayList<Declaration> decls = new ArrayList<Declaration>();
		if(!(scanner.viewNextToken().getType() == Token.TokenType.INT || scanner.viewNextToken().getType() == Token.TokenType.VOID)) {
			throw new ParseException("C- programs must have at least one decl.");
		}

		while(scanner.viewNextToken().getType() == Token.TokenType.INT || scanner.viewNextToken().getType() == Token.TokenType.VOID) {
			Token.TokenType decl_type = scanner.getNextToken().getType();
			Token id = consumeToken(Token.TokenType.IDENT);

			if(scanner.viewNextToken().getType() == Token.TokenType.SEMI || scanner.viewNextToken().getType() == Token.TokenType.LSQUARE) {
				if(decl_type == Token.TokenType.VOID) {
					throw new ParseException("Tried to create a variable with type void.");
				}
				Declaration.VarDecl var = parseVariable(decl_type, id, true);

				decls.add(var);
			}
			else if(scanner.viewNextToken().getType() == Token.TokenType.LPAREN) {
				scanner.getNextToken();

				ArrayList<Declaration.VarDecl> params = new ArrayList<Declaration.VarDecl>();

				if(scanner.viewNextToken().getType() == Token.TokenType.VOID) {
					consumeToken(Token.TokenType.VOID);
				}
				else if(scanner.viewNextToken().getType() != Token.TokenType.RPAREN) {
					while(true) {
						consumeToken(Token.TokenType.INT);
						Token param_id = consumeToken(Token.TokenType.IDENT);
						Integer size = null;
						if(scanner.viewNextToken().getType() == Token.TokenType.LSQUARE) {
							scanner.getNextToken();
							size = 0;
							consumeToken(Token.TokenType.RSQUARE);
						}

						params.add(new Declaration.VarDecl((String) param_id.getData(), Token.TokenType.INT, size, Declaration.VarDecl.VarLocation.PARAM));

						if(scanner.viewNextToken().getType() != Token.TokenType.COMMA) {
							break;
						}
						consumeToken(Token.TokenType.COMMA);
					}
				}

				consumeToken(Token.TokenType.RPAREN);
				CompoundStatement stmt = parseCompound(null);

				decls.add(new Declaration.FunDecl((String) id.getData(), decl_type, params, stmt));
			}
			else {
				throw new ParseException("Unexpected token " + scanner.viewNextToken().getType().toString());
			}
		}

		consumeToken(Token.TokenType.EOF);

		return new Program(decls);
	}

	private Expression parseExpression() throws ParseException, ScannerException, IOException {
		Expression expr;
		if(scanner.viewNextToken().getType() == Token.TokenType.IDENT) {
			Token identifier = scanner.getNextToken();
			expr = parseExpressionP(identifier);
		} else if (scanner.viewNextToken().getType() == Token.TokenType.NUM) {
			expr = parseSimpleExpressionP(new Expression.Num((Integer)scanner.getNextToken().getData()));
		} else if (scanner.viewNextToken().getType() == Token.TokenType.LPAREN) {
			scanner.getNextToken();
			expr = parseExpression();
			consumeToken(Token.TokenType.RPAREN);
			expr = parseSimpleExpressionP(expr);
		} else {
			throw new ParseException("Expected IDENT, NUM, or LPAREN token but got " + scanner.getNextToken().getType().toString());
		}

		return expr;
	}

	private Expression parseExpressionP(Token identifier) throws ParseException, ScannerException, IOException {
		Expression expr;
		if(scanner.viewNextToken().getType() == Token.TokenType.ASSIGN) {
			scanner.getNextToken();
			expr = parseExpression();
			expr = new Expression.Assign(new Expression.Var((String)identifier.getData()), expr);
		} else if (scanner.viewNextToken().getType() == Token.TokenType.LSQUARE) {
			scanner.getNextToken();
			expr = parseExpression();
			expr = new Expression.Var((String)identifier.getData(), expr);
			consumeToken(Token.TokenType.RSQUARE);
			expr = parseExpressionPP(expr);
		} else if (scanner.viewNextToken().getType() == Token.TokenType.LPAREN) {
			scanner.getNextToken();
			expr = new Expression.Call((String)identifier.getData(), parseArgs());
			consumeToken(Token.TokenType.RPAREN);
		} else { 								// TODO: if next is the first set of SE'
			expr = parseSimpleExpressionP(new Expression.Var((String)identifier.getData()));
		}
		return expr;
	}
	private Expression parseExpressionPP(Expression e) throws ParseException, ScannerException, IOException {
		Expression expr;
		if(scanner.viewNextToken().getType() == Token.TokenType.ASSIGN) {
			scanner.getNextToken();
			expr = parseExpression();
			expr = new Expression.Assign((Expression.Var)e, expr);
		} else { 								// TODO: if next is the first set of SE'
			expr = parseSimpleExpressionP(e);
		}
		return expr;
	}
	private Expression parseSimpleExpressionP(Expression e) throws ParseException, ScannerException, IOException {
		Expression expr;
		expr = parseAdditiveExpressionP(e);
		while(scanner.viewNextToken().getType() == Token.TokenType.LEQUAL || scanner.viewNextToken().getType() == Token.TokenType.LESS || scanner.viewNextToken().getType() == Token.TokenType.GREATER || scanner.viewNextToken().getType() == Token.TokenType.GREQUAL || scanner.viewNextToken().getType() == Token.TokenType.EQUAL || scanner.viewNextToken().getType() == Token.TokenType.NEQUAL)
		{
			expr = new Expression.Binop(scanner.getNextToken().getType(), expr, parseAdditiveExpression());
		}
		return expr;
	}

	private Expression parseAdditiveExpression() throws ParseException, ScannerException, IOException {
		Expression expr;
		expr = parseTerm();
		while(scanner.viewNextToken().getType() == Token.TokenType.PLUS || scanner.viewNextToken().getType() == Token.TokenType.MINUS)
		{
			expr = new Expression.Binop (scanner.getNextToken().getType(), expr, parseTerm());
		}
		return expr;
	}

	private Expression parseAdditiveExpressionP(Expression e) throws ParseException, ScannerException, IOException {
		Expression expr;
		expr = parseTermP(e);
		while(scanner.viewNextToken().getType() == Token.TokenType.PLUS || scanner.viewNextToken().getType() == Token.TokenType.MINUS)
		{
			expr = new Expression.Binop (scanner.getNextToken().getType(), expr, parseTerm());
		}
		return expr;
	}

	private Expression parseTerm() throws ParseException, ScannerException, IOException {
		Expression expr;
		expr = parseFactor();
		while(scanner.viewNextToken().getType() == Token.TokenType.MULT || scanner.viewNextToken().getType() == Token.TokenType.DIV)
		{
			expr = new Expression.Binop(scanner.getNextToken().getType(), expr, parseFactor());
		}
		return expr;
	}

	public Expression parseTermP(Expression e) throws ParseException, ScannerException, IOException {
		Expression expr = e;
		while(scanner.viewNextToken().getType() == Token.TokenType.MULT || scanner.viewNextToken().getType() == Token.TokenType.DIV)
		{
			expr = new Expression.Binop(scanner.getNextToken().getType(), expr, parseFactor());
		}
		return expr;
	}

	private Expression parseFactor() throws ParseException, ScannerException, IOException {
		if(scanner.viewNextToken().getType() == Token.TokenType.LPAREN) {
			scanner.getNextToken();
			Expression expr = parseExpression();
			consumeToken(Token.TokenType.RPAREN);
			return expr;
		} else if (scanner.viewNextToken().getType() == Token.TokenType.NUM) {
			return new Expression.Num((Integer)scanner.getNextToken().getData());
		} else if (scanner.viewNextToken().getType() == Token.TokenType.IDENT) {
			Token id = scanner.getNextToken();
			return parseVarcall(id);
		} else {
			throw new ParseException("Expected token LPAREN, NUM, or IDENT but got " + scanner.getNextToken().getType().toString());
		}

	}

	private Expression parseVarcall(Token identifier) throws ParseException, ScannerException, IOException {
		if(scanner.viewNextToken().getType() == Token.TokenType.LPAREN) {
			scanner.getNextToken();
			ArrayList<Expression> args = parseArgs();
			consumeToken(Token.TokenType.RPAREN);

			return new Expression.Call((String) identifier.getData(), args);
		} else if (scanner.viewNextToken().getType() == Token.TokenType.LSQUARE) {
			scanner.getNextToken();
			Expression index = parseExpression();
			consumeToken(Token.TokenType.RSQUARE);

			return new Expression.Var((String) identifier.getData(), index);
		} else {
			return new Expression.Var((String) identifier.getData());
		}
	}

	public ArrayList<Expression> parseArgs() throws ParseException, ScannerException, IOException {
		// TODO: What do we set args too?
		// Note, we've switched to arg-list
		// On the first set of expression...

		ArrayList<Expression> args = new ArrayList<Expression>();

		if(scanner.viewNextToken().getType() == Token.TokenType.IDENT || scanner.viewNextToken().getType() == Token.TokenType.NUM) {
			args.add(parseExpression());
		} else if (scanner.viewNextToken().getType() == Token.TokenType.LPAREN) {
			args.add(parseExpression());
			consumeToken(Token.TokenType.RPAREN);
		}
			while(scanner.viewNextToken().getType() == Token.TokenType.COMMA)
			{
				scanner.getNextToken();
				args.add(parseExpression());
			}
		return args;
	}

	private Declaration.VarDecl parseVariable(Token.TokenType decl_type, Token id, boolean global) throws ParseException, ScannerException, IOException {
		Integer size = null;
		if(scanner.viewNextToken().getType() == Token.TokenType.LSQUARE) {
			scanner.getNextToken();
			Token num = consumeToken(Token.TokenType.NUM);
			size = (Integer) num.getData();
			consumeToken(Token.TokenType.RSQUARE);
		}

		consumeToken(Token.TokenType.SEMI);

		if(global) {

			return new Declaration.VarDecl((String) id.getData(), decl_type, size, Declaration.VarDecl.VarLocation.GLOBAL);
		}
		else {
			return new Declaration.VarDecl((String) id.getData(), decl_type, size, Declaration.VarDecl.VarLocation.LOCAL);
		}
	}

	private CompoundStatement parseCompound(CompoundStatement parent) throws ParseException, ScannerException, IOException {
		consumeToken(Token.TokenType.LCURLY);

		ArrayList<Declaration.VarDecl> decls = new ArrayList<Declaration.VarDecl>();
		ArrayList<Statement> statements = new ArrayList<Statement>();

		CompoundStatement cpd_stmt = new CompoundStatement(decls, statements, parent);

		while(scanner.viewNextToken().getType() == Token.TokenType.INT) {
			Token.TokenType decl_type = scanner.getNextToken().getType();
			Token id = consumeToken(Token.TokenType.IDENT);

			Declaration.VarDecl var = parseVariable(decl_type, id, false);
			decls.add(var);
		}

		while(scanner.viewNextToken().getType() != Token.TokenType.RCURLY) {
			Statement stmt = parseStatement(cpd_stmt);
			statements.add(stmt);
		}

		// Munch the end curly.
		scanner.getNextToken();

		return cpd_stmt;
	}

	private Statement parseStatement(CompoundStatement parent) throws ParseException, ScannerException, IOException {
		if(scanner.viewNextToken().getType() == Token.TokenType.LCURLY) {
			return parseCompound(parent);
		}
		else if(scanner.viewNextToken().getType() == Token.TokenType.IF) {
			scanner.getNextToken();
			consumeToken(Token.TokenType.LPAREN);
			Expression expr = parseExpression();
			consumeToken(Token.TokenType.RPAREN);
			Statement if_ = parseStatement(parent);
			Statement else_ = null;
			if(scanner.viewNextToken().getType() == Token.TokenType.ELSE) {
				scanner.getNextToken();
				else_ = parseStatement(parent);
			}

			return new IfStatement(expr, if_, else_);
		}
		else if(scanner.viewNextToken().getType() == Token.TokenType.WHILE) {
			scanner.getNextToken();
			consumeToken(Token.TokenType.LPAREN);
			Expression expr = parseExpression();
			consumeToken(Token.TokenType.RPAREN);
			Statement stmt = parseStatement(parent);

			return new WhileStatement(expr, stmt);
		}
		else if(scanner.viewNextToken().getType() == Token.TokenType.RETURN) {
			scanner.getNextToken();
			Expression expr = null;
			if(scanner.viewNextToken().getType() != Token.TokenType.SEMI) {
				expr = parseExpression();
			}
			consumeToken(Token.TokenType.SEMI);

			return new ReturnStatement(expr);
		}
		else {
			Expression expr = null;

			if(scanner.viewNextToken().getType() != Token.TokenType.SEMI) {
				expr = parseExpression();
			}
			consumeToken(Token.TokenType.SEMI);

			return expr;
		}
	}

	/*public static void main(String args[]) {
		CMinusScanner scanner;
		CMinusParser parser;

		try {
			scanner = new CMinusScanner(new BufferedReader(new FileReader(args[0])));
			parser = new CMinusParser(scanner);

			// Token next;
			// while((next = scanner.getNextToken()).getType() != Token.TokenType.EOF) {
			// 	if(next.getData() == null) {
			// 		System.out.println(next.getType().toString());
			// 	}
			// 	else {
			// 		System.out.println(next.getType().toString() + ": " + next.getData().toString());
			// 	}
			// }

			ArrayList<Declaration> prog = parser.parseAll();
			for(Declaration decl:prog)
			{
				decl.printTree(0);
			}
		}
		catch(Exception ex) {
			System.out.println(ex.toString());
			return;
		}
	}*/
}