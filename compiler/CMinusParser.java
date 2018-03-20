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
		System.out.println("in parse decl with " + scanner.viewNextToken().getType().toString());
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