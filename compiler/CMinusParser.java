package compiler;

class CMinusParser {
	class ParseException extends Exception {

	}

	Scanner scanner;

	CMinusParser(Scanner s) {
		scanner = s;
	}

	public ArrayList<Declaration> parseAll() {
		return parseDeclList();
	}

	private Token consumeToken(Token.TokenType type) throws ParseException {
		Token t = scanner.getNextToken();
		if(t.getType() != type) {
			thrown new ParseException("Expected token " + type + ", got" + t.getType());
		}
		return t;
	}

	public ArrayList<Declaration> parseDeclList() throws ParseException {
		ArrayList<Declaration> decls = new ArrayList<Declaration>();
		while(scanner.viewNextToken().getType() == Token.TokenType.INT || scanner.viewNextToken().getType() == Token.TokenType.VOID) {
			Token.TokenType decl_type = scanner.viewNextToken().getType();
			Token id = consumeToken(Token.TokenType.ID);

			if(scanner.viewNextToken().getType() == Token.TokenType.SEMI || scanner.viewNextToken().getType() == Token.TokenType.LSQUARE) {
				Integer size = null;
				if(scanner.viewNextToken().getType() == Token.TokenType.LSQUARE) {
					scanner.getNextToken()
					Token num = consumeToken(Token.TokenType.NUM);
					size = (Integer) num.getData();
					consumeToken(Token.TokenType.RSQUARE);
				}

				decls.add(new VarDecl((String) id.getData(), size));
			}
			else if(scanner.viewNextToken().getType() == Token.TokenType.LPAREN) {
				scanner.getNextToken();

				ArrayList<VarDecl> params = new ArrayList<VarDecl>();

				if(scanner.viewNextToken().getType() != Token.TokenType.RPAREN) {
					while(true) {
						consumeToken(Token.TokenType.INT);
						Token param_id = consumeToken(Token.TokenType.ID);

						Integer size = null;
						if(scanner.viewNextToken().getType() == Token.TokenType.LSQUARE) {
							scanner.getNextToken()
							Token num = consumeToken(Token.TokenType.NUM);
							size = (Integer) num.getData();
							consumeToken(Token.TokenType.RSQUARE);
						}

						params.add(new VarDecl((String) param_id.getData(), size));

						if(scanner.viewNextToken().getType() != Token.TokenType.COMMA) {
							break;
						}
						consumeToken(Token.TokenType.COMMA);
					}
				}

				consumeToken(Token.TokenType.RPAREN);

				decls.add(new FunDecl((String) id.getData(), params));
			}
			else {
				thrown new ParseException("Unexpected token " + scanner.viewNextToken().getType().getType());
			}
		}
	}
}