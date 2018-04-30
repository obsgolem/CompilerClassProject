package parser;

import java.util.ArrayList;
import lowlevel.*;

public class Program {
	ArrayList<Declaration> declarations;

	public Program(ArrayList<Declaration> d) {
		declarations = d;
	}

	public CodeItem genLLCode() throws CodeGenerationException {
		CodeItem prev = null;
		for(Declaration decl:declarations) {
			CodeItem next = decl.genLLCodeForTopLevel();
			if(prev != null) {
				prev.setNextItem(next);
			}
			prev = next;
		}

		return declarations.get(0).getCodeItem();
	}

	public void printTree() {
		for(Declaration decl:declarations) {
			decl.printTree(0);
		}
	}
}