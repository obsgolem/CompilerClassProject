package compiler;

import java.io.BufferedReader;
import java.io.FileReader;

public class CMinusScanner implements Scanner {
	private BufferedReader input;
	private Token next_token;

	public class ScannerException extends Exception {

	}

	public CMinusScanner(BufferedReader file) {
		input = file;
		next_token = scanToken();
	}

	public Token getNextToken() throws ScannerException {
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
		DONE,
	}

	private Token scanToken() throws ScannerException {
		Token.TokenType type;
		String data = null;

		State state = State.START;

		while(state != State.DONE) {
			// Mark input so we can back up. Use four characters in case of unicode.
			input.mark(4);

			int c = input.read();
			Boolean save = true;

			switch(state) {
				case State.START: {
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
								state = State.DONE;
							} break;

							case '>': {
								type = Token.TokenType.LESS;
								state = State.GOT_LESS;
							} break;

							case '<': {
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

				case State.DOING_NUMBER: {
					if(!Character.isDigit(c)) {
						input.reset();
						type = Token.TokenType.NUM;
						save = false;
						state = state.DONE;
					}
				} break;

				case State.DOING_IDENT_OR_KEYWORD: {
					if(!Character.isLetter(c)) {
						input.reset();
						type = Token.TokenType.IDENT;
						save = false;
						state = state.DONE;
					}
				} break;

				case State.GOT_NOT: {
					save = false;

					if((char) c != '=') {
						throw new ScannerException("Invalid token: !, expected !=.");
					}

					type = Token.TokenType.NEQUAL;
				} break;

				case State.GOT_LESS: {
					save = false;

					if((char) c == '=') {
						type = Token.TokenType.LEQUAL;
					}
				} break;

				case State.GOT_GREATER: {
					save = false;

					if((char) c == '=') {
						type = Token.TokenType.GREQUAL;
					}
				} break;

				case State.GOT_EQUAL: {
					save = false;

					if((char) c == '=') {
						type = Token.TokenType.EQUAL;
					}
				} break;

				case State.Done:
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
			if(data == "if") {
				type = Token.TokenType.IF;
				data = null;
			}
			else if(data == "else") {
				type = Token.TokenType.ELSE;
				data = null;
			}
			else if(data == "while") {
				type = Token.TokenType.WHILE;
				data = null;
			}
			else if(data == "int") {
				type = Token.TokenType.INT;
				data = null;
			}
			else if(data == "return") {
				type = Token.TokenType.RETURN;
				data = null;
			}
			else if(data == "void") {
				type = Token.TokenType.VOID;
				data = null;
			}
		}

		if(type == Token.TokenType.NUM) {
			data = new Integer(data, 10);
		}

		token = new Token(type, data);

		return token;
	}

	public static void Main() {
		CMinusScanner scanner;

		try {
			scanner = new CMinusScanner(new BufferedReader(new FileReader("test.cm")));

			Token next;
			while((next = scanner.getNextToken()).getType() != Token.TokenType.EOF) {
				System.out.println(next.getType().toString() + ": " + next.getData().toString());
			}
		}
		catch(Exception ex) {
			return;
		}
	}
}