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
			getNextToken();
			parseExpressionP();
		} else if (scanner.viewNextToken().getType() == Token.TokenType.NUM) {
			getNextToken();
			parseSimpleExpressionP();
		} else if (scanner.viewNextToken().getType() == Token.TokenType.LPAREN) {
			getNextToken();
			parseExpression();
			consumeToken(Token.TokenType.RPAREN);
			parseSimpleExpressionP();
		}

		return expr;
	}
	
	public void parseExpressionP() throws ParseException, ScannerException, IOException {
		if(scanner.viewNextToken().getType() == Token.TokenType.ASSIGN) {
			getNextToken();
			parseExpression();
		} else if (scanner.viewNextToken().getType() == Token.TokenType.LSQUARE) {
			getNextToken();
			parseExpression();
			consumeToken(Token.TokenType.RSQUARE)
			parseExpressionPP();
		} else { 								// TODO: if next is the first set of SE'
			parseSimpleExpressionP();
		}
		return;
	}
	public void parseExpressionPP() throws ParseException, ScannerException, IOException {
		if(scanner.viewNextToken().getType() == Token.TokenType.ASSIGN) {
			getNextToken();
			parseExpression();
		} else { 								// TODO: if next is the first set of SE'
			parseSimpleExpressionP();
		}
		return;
	}
	public void parseSimpleExpressionP() throws ParseException, ScannerException, IOException {
		parseAdditiveExpressionP();
		while(scanner.viewNextToken().getType() == Token.TokenType.LEQUAL || scanner.viewNextToken().getType() == Token.TokenType.LESS || scanner.viewNextToken().getType() == Token.TokenType.GREATER || scanner.viewNextToken().getType() == Token.TokenType.GREQUAL || scanner.viewNextToken().getType() == Token.TokenType.EQUAL || scanner.viewNextToken().getType() == Token.TokenType.NEQUAL)
		{
			getNextToken();
			parseAdditiveExpression();
		}
		return;
	}

	public void parseAdditiveExpression() throws ParseException, ScannerException, IOException {
		parseTerm();
		while(scanner.viewNextToken().getType() == Token.TokenType.PLUS || scanner.viewNextToken().getType() == Token.TokenType.MINUS)
		{
			getNextToken();
			parseTerm();
		}
		return;
	}

	public void parseAdditiveExpressionP() throws ParseException, ScannerException, IOException {
		parseTermP();
		while(scanner.viewNextToken().getType() == Token.TokenType.PLUS || scanner.viewNextToken().getType() == Token.TokenType.MINUS)
		{
			getNextToken();
			parseTerm();
		}
		return;
	}

	public void parseTerm() throws ParseException, ScannerException, IOException {
		parseFactor();
		while(scanner.viewNextToken().getType() == Token.TokenType.MULT || scanner.viewNextToken().getType() == Token.TokenType.DIV)
		{
			getNextToken();
			parseFactor();
		}
		return;
	}

	public void parseTermP() throws ParseException, ScannerException, IOException {
		while(scanner.viewNextToken().getType() == Token.TokenType.MULT || scanner.viewNextToken().getType() == Token.TokenType.DIV)
		{
			getNextToken();
			parseFactor();
		}
		return;
	}

	public Factor parseFactor() throws ParseException, ScannerException, IOException {
		Factor fac = new Factor();
		if(scanner.viewNextToken().getType() == Token.TokenType.LPAREN) {
			getNextToken();
			parseExpression();
			consumeToken(Token.TokenType.RPAREN);
		} else if (scanner.viewNextToken().getType() == Token.TokenType.NUM) {
			// TODO: Return here? Set factor equal to num's value?
		} else if (scanner.viewNextToken().getType() == Token.TokenType.IDENT) {
			getNextToken();
			parseVarcall();
		}

		return fac;
	}

	public Varcall parseVarcall() throws ParseException, ScannerException, IOException {
		Varcall varc = new Varcall();
		// TODO: What do we set varc too?
		if(scanner.viewNextToken().getType() == Token.TokenType.LPAREN) {
			getNextToken();
			parseArgs();
			consumeToken(Token.TokenType.RPAREN);
		} else if (scanner.viewNextToken().getType() == Token.TokenType.LSQUARE) {
			getNextToken();
			parseExpression();
			consumeToken(Token.TokenType.RSQUARE);
		} else 
			varc = null;
		}

		return varc;
	}

	public Args parseArgs() throws ParseException, ScannerException, IOException {
		Args args = new Args();
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