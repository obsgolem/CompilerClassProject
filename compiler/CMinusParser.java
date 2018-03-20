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

	public ArrayList<Declaration> parseAll() throws ParseException, ScannerException, IOException {
		return parseDeclList();
	}

	private Token consumeToken(Token.TokenType type) throws ParseException, ScannerException, IOException {
		Token t = scanner.getNextToken();
		if(t.getType() != type) {
			throw new ParseException("Expected token " + type.toString() + ", got" + t.getType().toString());
		}
		return t;
	}

	public ArrayList<Declaration> parseDeclList() throws ParseException, ScannerException, IOException {
		ArrayList<Declaration> decls = new ArrayList<Declaration>();
		while(scanner.viewNextToken().getType() == Token.TokenType.INT || scanner.viewNextToken().getType() == Token.TokenType.VOID) {
			Token.TokenType decl_type = scanner.getNextToken().getType();
			Token id = consumeToken(Token.TokenType.IDENT);

			if(scanner.viewNextToken().getType() == Token.TokenType.SEMI || scanner.viewNextToken().getType() == Token.TokenType.LSQUARE) {
				Integer size = null;
				if(scanner.viewNextToken().getType() == Token.TokenType.LSQUARE) {
					scanner.getNextToken();
					Token num = consumeToken(Token.TokenType.NUM);
					size = (Integer) num.getData();
					consumeToken(Token.TokenType.RSQUARE);
				}
				else{
					scanner.getNextToken();
				}

				decls.add(new Declaration.VarDecl((String) id.getData(), size));
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

						params.add(new Declaration.VarDecl((String) param_id.getData(), size));

						if(scanner.viewNextToken().getType() != Token.TokenType.COMMA) {
							break;
						}
						consumeToken(Token.TokenType.COMMA);
					}
				}

				consumeToken(Token.TokenType.RPAREN);

				decls.add(new Declaration.FunDecl((String) id.getData(), params));
			}
			else {
				throw new ParseException("Unexpected token " + scanner.viewNextToken().getType().toString());
			}
		}
		return decls;
	}

	public Expression parseExpression() throws ParseException, ScannerException, IOException {
		Expression expr = new Expression();
		if(scanner.viewNextToken().getType() == Token.TokenType.IDENT) {
			Token identifier = getNextToken();
			parseExpressionP(t);
		} else if (scanner.viewNextToken().getType() == Token.TokenType.NUM) {
			expr = parseSimpleExpressionP(new Num((Integer)getNextToken().getData()));
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
			expr = new Assign(new Var((String)identifier.getData()), expr);
		} else if (scanner.viewNextToken().getType() == Token.TokenType.LSQUARE) {
			getNextToken();
			expr = parseExpression();
			expr = new Var((String)identifier.getData(), expr);
			consumeToken(Token.TokenType.RSQUARE)
			expr = parseExpressionPP(expr);
		} else { 								// TODO: if next is the first set of SE'
			expr = parseSimpleExpressionP(new Var((String)identifier.getData()));
		}
		return expr;
	}
	public Expression parseExpressionPP(Expression e) throws ParseException, ScannerException, IOException {
		Expression expr;
		if(scanner.viewNextToken().getType() == Token.TokenType.ASSIGN) {
			getNextToken();
			expr = parseExpression();
			expr = new Assign((Var)e, expr);
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
		} else 
			return new Expression.Var((String) identifier.getData());
		}
	}

	public ArrayList<Expression> parseArgs() throws ParseException, ScannerException, IOException {
		ArrayList<Expression> args = new Args();
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

{
		INT,
		ELSE,
		IF,
		WHILE,
		RETURN,
		VOID,
		IDENT,
		NUM,
		PLUS,
		MINUS,
		MULT,
		DIV,
		LESS,
		LEQUAL,
		GREATER,
		GREQUAL,
		EQUAL,
		NEQUAL,
		ASSIGN,
		SEMI,
		COMMA,
		LPAREN,
		RPAREN,
		LSQUARE,
		RSQUARE,
		LCURLY,
		RCURLY,
		EOF
}