package parser;

public class Token {
	public enum TokenType {
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

	private TokenType token_type;
	private Object token_data;

	public Token(TokenType type) {
		this(type, null);
	}

	public Token(TokenType type, Object data) {
		token_type = type;
		token_data = data;
	}

	public TokenType getType() {
		return token_type;
	}

	public Object getData() {
		return token_data;
	}
}