package compiler;

import java.util.ArrayList;

public abstract class Expression extends Statement {

	public static class Binop extends Expression {
		public Token.TokenType binop;
		public Expression lexp;
		public Expression rexp;

		public Binop(Token.TokenType op, Expression left, Expression right) {
			binop = op;
			lexp = left;
			rexp = right;
		}

		public void printTree(int level) {
			super.printTree(level);
			System.out.println("Binary expression " + binop.toString());
			lexp.printTree(level+1);
			lexp.printTree(level+1);
		}
	}

	public static class Call extends Expression {
		public String name;
		public ArrayList<Expression> args;

		public Call(String n, ArrayList<Expression> a) {
			name = n;
			args = a;
		}

		public void printTree(int level) {
			super.printTree(level);
			System.out.println("Call expression: " + name);
			for(Expression expr : args) {
				expr.printTree(level+1);
			}
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

		public void printTree(int level) {
			super.printTree(level);
			System.out.println("Var expression: " + name);
			index.printTree(level+1);
		}
	}

	public static class Assign extends Expression {
		public Var v;
		public Expression val;

		public Assign(Var vr, Expression e) {
			v = vr;
			val = e;
		}

		public void printTree(int level) {
			super.printTree(level);
			System.out.println("Assign expression: ");
			v.printTree(level+1);
			val.printTree(level+1);
		}
	}

	public static class Num extends Expression {
		public Integer val;

		public Num(Integer n) {
			val = n;
		}

		public void printTree(int level) {
			super.printTree(level);
			System.out.println("Num expression: " + val.toString());
		}
	}
}