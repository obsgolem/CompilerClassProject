package compiler;
import java.io.IOException;


public interface Scanner {
	public Token getNextToken() throws ScannerException, IOException;
	public Token viewNextToken();
}