package compiler;

import java.io.BufferedReader;
import java.io.FileReader;

public class CMinusScanner implements Scanner {
	private BufferedReader input;
	private Token next_token;

	public CMinusScanner(BufferedReader file) {
		input = file;
		next_token = scanToken();
	}

	public Token getNextToken() {
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
		DONE,
	}

	private Token scanToken() {
		Token.TokenType type;
		String data = null;

		State state = State.START;

		while(state != State.DONE) {
			case State.START: {
				if(c == -1) {
					type = EOF;
					state = DONE;
				}
			} break;

			case State.Done:
			default: {

			}
		}

		return token;
	}

	public static void Main() {
		CMinusScanner scanner;

		try {
			scanner = new CMinusScanner(new BufferedReader(new FileReader("test.cm")));
		}
		catch(Exception ex) {
			return;
		}

		Token next;
		while((next = scanner.getNextToken()).getType() != Token.TokenType.EOF) {
			System.out.println(next.getType().toString() + ": " + next.getData().toString());
		}
	}
}