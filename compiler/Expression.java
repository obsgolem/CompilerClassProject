package compiler;
import java.util.ArrayList;

public abstract class Expression {

	public static class Binop extends Expression {
		public TokenType binop;
		public Expression lexp;
		public Expression rexp;

		public binop(TokenType op, Expression left, Expression right) {
			binop = op;
			lexp = left;
			rexp = right;
		}
	}

	public static class Call extends Expression {
		public String name;
		public ArrayList<Expression> args;

		public Call(String n, ArrayList<Expression> a) {
			name = n;
			args = a;
		}
	}

	public static class Var extends Expression {
		public String name;
		public Expression index;

		public Var(String n, Expression i) {
			name = n;
			index = i;
		}

		public Var(String n) {
			name = n;
			index = null;
		}
	}

	public static class Assign extends Expression {
		public Var v;
		public Expression val;

		public Assign(Var vr, Expression e) {
			v = vr;
			val = e;
		}
	}

	public static class Num extends Expression {
		public Integer val;

		public Num(Integer n) {
			val = n;
		}
	}
}