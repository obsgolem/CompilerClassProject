package compiler;
import java.util.ArrayList;

public class Declaration {
	public String name;

	public Declaration(String s) {
		name = s;
	}

	public class FunDecl extends Declaration {
		public ArrayList<VarDecl> params;

		public FunDecl(String s, ArrayList<VarDecl> p) {
			super(s);
			params = p;
		}
	}

	public class VarDecl extends Declaration {
		public Integer array_size;

		public VarDecl(String s, Integer a) {
			super(s);
			array_size = a;
		}
	}
}