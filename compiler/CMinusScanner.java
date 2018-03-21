package compiler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CMinusScanner implements Scanner {
	private BufferedReader input;
	private Token next_token;

	public CMinusScanner(BufferedReader file) throws ScannerException, IOException {
		input = file;
		next_token = scanToken();
	}

	public Token getNextToken() throws ScannerException, IOException {
		Token out = next_token;
		if(next_token.getType() != Token.TokenType.EOF) {
			next_token = scanToken();
		}
		return out;
	}

	public Token viewNextToken() {
		return next_token;
	}

	private enum State {
		START,
		DOING_NUMBER,
		DOING_IDENT_OR_KEYWORD,
		DOING_COMMENT,
		ENDING_COMMENT,
		GOT_LESS,
		GOT_GREATER,
		GOT_NOT,
		GOT_EQUAL,
		GOT_SLASH,
		DONE,
	}

	private Token scanToken() throws ScannerException, IOException {
		Token.TokenType type = Token.TokenType.IDENT;
		String data = null;

		State state = State.START;

		while(state != State.DONE) {
			// Mark input so we can back up. Use four characters in case of unicode.
			input.mark(4);

			int c = input.read();
			Boolean save = true;

			switch(state) {
				case START: {
					if(c == -1) {
						save = false;
						type = Token.TokenType.EOF;
						state = State.DONE;
					}
					else if(Character.isDigit(c)) {
						state = State.DOING_NUMBER;
					}
					else if(Character.isLetter(c)) {
						state = State.DOING_IDENT_OR_KEYWORD;
					}
					else if(Character.isWhitespace(c)) {
						save = false;
					}
					else {
						save = false;

						switch((char) c) {
							case '+': {
								type = Token.TokenType.PLUS;
								state = State.DONE;
							} break;

							case '-': {
								type = Token.TokenType.MINUS;
								state = State.DONE;
							} break;

							case '*': {
								type = Token.TokenType.MULT;
								state = State.DONE;
							} break;

							case '/': {
								type = Token.TokenType.DIV;
								state = State.GOT_SLASH;
							} break;

							case '<': {
								type = Token.TokenType.LESS;
								state = State.GOT_LESS;
							} break;

							case '>': {
								type = Token.TokenType.GREATER;
								state = State.GOT_GREATER;
							} break;

							case '!': {
								state = State.GOT_NOT;
							} break;

							case '=': {
								type = Token.TokenType.ASSIGN;
								state = State.GOT_EQUAL;
							} break;

							case ';': {
								type = Token.TokenType.SEMI;
								state = State.DONE;
							} break;

							case ',': {
								type = Token.TokenType.COMMA;
								state = State.DONE;
							} break;

							case '{': {
								type = Token.TokenType.LCURLY;
								state = State.DONE;
							} break;

							case '}': {
								type = Token.TokenType.RCURLY;
								state = State.DONE;
							} break;

							case '(': {
								type = Token.TokenType.LPAREN;
								state = State.DONE;
							} break;

							case ')': {
								type = Token.TokenType.RPAREN;
								state = State.DONE;
							} break;

							case '[': {
								type = Token.TokenType.LSQUARE;
								state = State.DONE;
							} break;

							case ']': {
								type = Token.TokenType.RSQUARE;
								state = State.DONE;
							} break;

							default: {
								throw new ScannerException("Invalid character in file.");
							}
						}
					}
				} break;

				case DOING_NUMBER: {
					if(!Character.isDigit(c)) {
						if(Character.isLetter(c))
						{
							throw new ScannerException("Invalid token: numerical expression expected");
						}
						input.reset();
						type = Token.TokenType.NUM;
						save = false;
						state = state.DONE;
					}
				} break;

				case DOING_IDENT_OR_KEYWORD: {
					if(!Character.isLetter(c)) {
						if(Character.isDigit(c))
						{
							throw new ScannerException("Invalid token: identifier naming expected");
						}
						input.reset();
						type = Token.TokenType.IDENT;
						save = false;
						state = state.DONE;
					}
				} break;

				case GOT_NOT: {
					save = false;

					if((char) c != '=') {
						input.reset();
						throw new ScannerException("Invalid token: !, expected !=.");
					}
					state = State.DONE;
					type = Token.TokenType.NEQUAL;
				} break;

				case GOT_LESS: {
					save = false;

					if((char) c == '=') {
						type = Token.TokenType.LEQUAL;
					}
					else {
						input.reset();
					}
					state = State.DONE;
				} break;

				case GOT_GREATER: {
					save = false;

					if((char) c == '=') {
						type = Token.TokenType.GREQUAL;
					}
					else {
						input.reset();
					}
					state = State.DONE;
				} break;

				case GOT_EQUAL: {
					save = false;

					if((char) c == '=') {
						type = Token.TokenType.EQUAL;
					}
					else {
						input.reset();
					}
					state = State.DONE;

				} break;

				case GOT_SLASH: {
					save = false;

					if((char) c != '*') {
						type = Token.TokenType.DIV;
						state = State.DONE;
						input.reset();
					}
					else {
						state = State.DOING_COMMENT;
					}
				} break;

				case DOING_COMMENT: {
					save = false;

					if((char) c == '*') {
						state = State.ENDING_COMMENT;
					}
				} break;

				case ENDING_COMMENT: {
					save = false;

					if((char) c == '/') {
						state = State.START;
					}
					else {
						state = State.DOING_COMMENT;
					}
				} break;

				case DONE:
				default: {
					throw new ScannerException("Scanner bug: got into done state without breaking.");
				}
			}

			if(save) {
				if(data == null) {
					data = "";
				}
				data += (char) c;
			}
		}

		if(type == Token.TokenType.IDENT) {
			if(data.equals("if")) {
				type = Token.TokenType.IF;
				data = null;
			}
			else if(data.equals("else")) {
				type = Token.TokenType.ELSE;
				data = null;
			}
			else if(data.equals("while")) {
				type = Token.TokenType.WHILE;
				data = null;
			}
			else if(data.equals("int")) {
				type = Token.TokenType.INT;
				data = null;
			}
			else if(data.equals("return")) {
				type = Token.TokenType.RETURN;
				data = null;
			}
			else if(data.equals("void")  || data.equals("gallagher")) {
				type = Token.TokenType.VOID;
				data = null;
			}
		}
		Object obj = data;

		if(type == Token.TokenType.NUM) {
			obj = new Integer(data);
		}
		Token token = new Token(type, obj);

		return token;
	}
}