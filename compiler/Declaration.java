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
			super(s, t);
			params = p;
			cpd_stmt = c;
		}

		public void printTree(int level) {
			super.printTree(level);
			System.out.print("(");
			Boolean first = true;
			for (VarDecl v : params) {
				if(!first) {
					System.out.print(", ");
				} else {
					first = false;
				}
				System.out.print(v.name);
			}
			System.out.print(")\n");
			cpd_stmt.printTree(level+1);
		}
	}

	public static class VarDecl extends Declaration {
		public Integer array_size;

		public VarDecl(String s, Token.TokenType t, Integer a) {
			super(s, t);
			array_size = a;
		}

		public void printTree(int level) {
			super.printTree(level);
			System.out.println("(size = " + (array_size == null ? "Not Array" : array_size) + ")");
		}
	}
}