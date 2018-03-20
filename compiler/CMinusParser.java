package compiler;

import java.util.ArrayList;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

class CMinusParser {
	class ParseException extends Exception {
		public ParseException(String s) {
			super(s);
		}
	}

	Scanner scanner;

	public CMinusParser(Scanner s) {
		scanner = s;
	}

	private Token consumeToken(Token.TokenType type) throws ParseException, ScannerException, IOException {
		Token t = scanner.getNextToken();
		if(t.getType() != type) {
			throw new ParseException("Expected token " + type.toString() + ", got" + t.getType().toString());
		}
		return t;
	}


	public ArrayList<Declaration> parseAll() throws ParseException, ScannerException, IOException {
		System.out.println("in parse decl with " + scanner.viewNextToken().getType().toString());
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
				Declaration.VarDecl var = parseVariable(decl_type, id);

				decls.add(var);
			}
			else if(scanner.viewNextToken().getType() == Token.TokenType.LPAREN) {
				scanner.getNextToken();

				ArrayList<Declaration.VarDecl> params = new ArrayList<Declaration.VarDecl>();

				if(scanner.viewNextToken().getType() != Token.TokenType.RPAREN) {
					while(true) {
						consumeToken(Token.TokenType.INT);
						Token param_id = consumeToken(Token.TokenType.IDENT);
						Integer size = null;
						if(scanner.viewNextToken().getType() == Token.TokenType.LSQUARE) {
							scanner.getNextToken();
							size = 0;
							consumeToken(Token.TokenType.RSQUARE);
						}

						params.add(new Declaration.VarDecl((String) param_id.getData(), Token.TokenType.INT, size));

						if(scanner.viewNextToken().getType() != Token.TokenType.COMMA) {
							break;
						}
						consumeToken(Token.TokenType.COMMA);
					}
				}

				consumeToken(Token.TokenType.RPAREN);
				CompoundStatement stmt = parseCompound();

				decls.add(new Declaration.FunDecl((String) id.getData(), decl_type, param, stmt));
			}
			else {
				throw new ParseException("Unexpected token " + scanner.viewNextToken().getType().toString());
			}
		}

		consumeToken(Token.TokenType.EOF);

		return decls;
	}

	public Expression parseExpression() throws ParseException, ScannerException, IOException {
		Expression expr;
		if(scanner.viewNextToken().getType() == Token.TokenType.IDENT) {
			Token identifier = getNextToken();
			parseExpressionP(identifier);
		} else if (scanner.viewNextToken().getType() == Token.TokenType.NUM) {
			expr = parseSimpleExpressionP(new Expression.Num((Integer)getNextToken().getData()));
		} else if (scanner.viewNextToken().getType() == Token.TokenType.LPAREN) {
			getNextToken();
			expr = parseExpression();
			consumeToken(Token.TokenType.RPAREN);
			expr = parseSimpleExpressionP(expr);
		}

		return expr;
	}
	
	public Expression parseExpressionP(Token identifier) throws ParseException, ScannerException, IOException {
		Expression expr;
		if(scanner.viewNextToken().getType() == Token.TokenType.ASSIGN) {
			getNextToken();
			expr = parseExpression();
			expr = new Expression.Assign(new Expression.Var((String)identifier.getData()), expr);
		} else if (scanner.viewNextToken().getType() == Token.TokenType.LSQUARE) {
			getNextToken();
			expr = parseExpression();
			expr = new Expression.Var((String)identifier.getData(), expr);
			consumeToken(Token.TokenType.RSQUARE);
			expr = parseExpressionPP(expr);
		} else { 								// TODO: if next is the first set of SE'
			expr = parseSimpleExpressionP(new Expression.Var((String)identifier.getData()));
		}
		return expr;
	}
	public Expression parseExpressionPP(Expression e) throws ParseException, ScannerException, IOException {
		Expression expr;
		if(scanner.viewNextToken().getType() == Token.TokenType.ASSIGN) {
			getNextToken();
			expr = parseExpression();
			expr = new Expression.Assign((Expression.Var)e, expr);
		} else { 								// TODO: if next is the first set of SE'
			expr = parseSimpleExpressionP(expr);
		}
		return expr;
	}
	public Expression parseSimpleExpressionP(Expression e) throws ParseException, ScannerException, IOException {
		Expression expr;
		expr = parseAdditiveExpressionP(e);
		while(scanner.viewNextToken().getType() == Token.TokenType.LEQUAL || scanner.viewNextToken().getType() == Token.TokenType.LESS || scanner.viewNextToken().getType() == Token.TokenType.GREATER || scanner.viewNextToken().getType() == Token.TokenType.GREQUAL || scanner.viewNextToken().getType() == Token.TokenType.EQUAL || scanner.viewNextToken().getType() == Token.TokenType.NEQUAL)
		{
			getNextToken();
			parseAdditiveExpression();
		}
		return expr;
	}

	public Expression parseAdditiveExpression() throws ParseException, ScannerException, IOException {
		Expression expr;
		expr = parseTerm();
		while(scanner.viewNextToken().getType() == Token.TokenType.PLUS || scanner.viewNextToken().getType() == Token.TokenType.MINUS)
		{
			expr = new Binop (getNextToken(), expr, parseTerm());
		}
		return expr;
	}

	public Expression parseAdditiveExpressionP(Expression e) throws ParseException, ScannerException, IOException {
		Expression expr;
		expr = parseTermP(e);
		while(scanner.viewNextToken().getType() == Token.TokenType.PLUS || scanner.viewNextToken().getType() == Token.TokenType.MINUS)
		{
			getNextToken();
			parseTerm();
		}
		return expr;
	}

	public Expression parseTerm() throws ParseException, ScannerException, IOException {
		Expression expr;
		expr = parseFactor();
		while(scanner.viewNextToken().getType() == Token.TokenType.MULT || scanner.viewNextToken().getType() == Token.TokenType.DIV)
		{
			expr = new Binop(getNextToken(), expr, parseFactor());
		}
		return expr;
	}

	public void parseTermP(Expression e) throws ParseException, ScannerException, IOException {
		Expression expr = e;
		while(scanner.viewNextToken().getType() == Token.TokenType.MULT || scanner.viewNextToken().getType() == Token.TokenType.DIV)
		{
			expr = new Binop(getNextToken(), e, parseFactor());
		}
		return expr;
	}

	public Expression parseFactor() throws ParseException, ScannerException, IOException {
		if(scanner.viewNextToken().getType() == Token.TokenType.LPAREN) {
			getNextToken();
			Expression expr = parseExpression();
			consumeToken(Token.TokenType.RPAREN);
			return expr;
		} else if (scanner.viewNextToken().getType() == Token.TokenType.NUM) {
			return new Num((Integer)Token.getData());
		} else if (scanner.viewNextToken().getType() == Token.TokenType.IDENT) {
			Token id = getNextToken();
			return parseVarcall(id);
		} else {
			throw ParseException("Expected token LPAREN, NUM, or IDENT but got " + getNextToken().getType().toString());
		}

	}

	public Expression parseVarcall(Token identifier) throws ParseException, ScannerException, IOException {
		if(scanner.viewNextToken().getType() == Token.TokenType.LPAREN) {
			getNextToken();
			ArrayList<Expression> args = parseArgs();
			consumeToken(Token.TokenType.RPAREN);

			return new Expression.Call((String) identifier.getData(), args);
		} else if (scanner.viewNextToken().getType() == Token.TokenType.LSQUARE) {
			getNextToken();
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

		if(scanner.viewNextToken().getType() == Token.TokenType.IDENT || scanner.viewNextToken().getType() == Token.TokenType.NUM || scanner.viewNextToken().getType() == Token.TokenType.LPAREN) {
			parseExpression();
			consumeToken(Token.TokenType.RPAREN);
			while(scanner.viewNextToken().getType() == Token.TokenType.COMMA)
			{
				getNextToken();
				parseExpression();
			}
		}

		return args;
	}

	private Declaration.VarDecl parseVariable(Token.TokenType decl_type, Token id) throws ParseException, ScannerException, IOException {
		Integer size = null;
		if(scanner.viewNextToken().getType() == Token.TokenType.LSQUARE) {
			scanner.getNextToken();
			Token num = consumeToken(Token.TokenType.NUM);
			size = (Integer) num.getData();
			consumeToken(Token.TokenType.RSQUARE);
		}

		consumeToken(Token.TokenType.SEMI);

		return new Declaration.VarDecl((String) id.getData(), decl_type, size);
	}

	private CompoundStatement parseCompound() throws ParseException, ScannerException, IOException {
		consumeToken(Token.TokenType.LCURLY);

		ArrayList<Declaration.VarDecl> decls = new ArrayList<Declaration.VarDecl>();
		ArrayList<Statement> statements = new ArrayList<Statement>();

		while(scanner.viewNextToken().getType() == Token.TokenType.INT) {
			Token.TokenType decl_type = scanner.getNextToken().getType();
			Token id = consumeToken(Token.TokenType.IDENT);

			Declaration.VarDecl var = parseVariable(decl_type, id);
		}

		while(scanner.viewNextToken().getType() != Token.TokenType.RCURLY) {
			Statement stmt = parseStatement();
			statements.add(stmt);
		}
		
		// Munch the end curly.
		scanner.getNextToken();

		return new CompoundStatement(decls, statements);
	}

	private Statement parseStatement() throws ParseException, ScannerException, IOException {
		if(scanner.viewNextToken().getType() == Token.TokenType.LCURLY) {
			return parseCompound();
		}
		else if(scanner.viewNextToken().getType() == Token.TokenType.IF) {
			scanner.getNextToken();
			consumeToken(Token.TokenType.LPAREN);
			Expression expr = parseExpression();
			consumeToken(Token.TokenType.RPAREN);
			Statement if_ = parseStatement();
			Statement else_ = null;
			if(scanner.viewNextToken().getType() == Token.TokenType.ELSE) {
				scanner.getNextToken();
				else_ = parseStatement();
			}

			return new IfStatement(expr, if_, else_);
		}
		else if(scanner.viewNextToken().getType() == Token.TokenType.WHILE) {
			scanner.getNextToken();
			consumeToken(Token.TokenType.LPAREN);
			Expression expr = parseExpression();
			consumeToken(Token.TokenType.RPAREN);
			Statement stmt = parseStatement();

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
	
	public static void main(String args[]) {
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

			parser.parseAll();
		}
		catch(Exception ex) {
			System.out.println(ex.toString());
			return;
		}
	}
}