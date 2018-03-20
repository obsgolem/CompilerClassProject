package compiler;
import java.util.ArrayList;

public abstract class Declaration {
	public String name;
	Token.TokenType type;

	public Declaration(String s, Token.TokenType t) {
		name = s;
		type = t;
	}

	public void printTree(int level) {
		for(int i = 0; i < level; i++) {
			System.out.print("\t");
		}
		System.out.print("Declaration "+ type.toString() + " " + name);
	}

	public static class FunDecl extends Declaration {
		public ArrayList<VarDecl> params;
		public CompoundStatement cpd_stmt;

		public FunDecl(String s, Token.TokenType t, ArrayList<VarDecl> p, CompoundStatement c) {
			super(s);
			params = p;
			cpd_stmt = c;
		}

		public void printTree(int level) {
			super(level);
			System.out.print("(");
			Boolean first = true;
			for (VarDecl v : params) {
				if(!first) {
					s += ", "
				}
				s += v.toString();
			}
			System.out.print(")\n");
			cpd_stmt.printTree(level+1);
		}
	}

	public static class VarDecl extends Declaration {
		public Integer array_size;

		public VarDecl(String s, Token.TokenType t, Integer a) {
			super(s);
			array_size = a;
		}

		public void printTree(int level) {
			super(level);
			System.out.println("(size = " + (array_size == null ? "Unknown" : array_size) + ")");
		}
	}
}