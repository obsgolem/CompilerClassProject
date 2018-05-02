package parser;

import java.util.ArrayList;

import compiler.*;
import lowlevel.*;

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

		public Integer genLLCode(Function func, CompoundStatement scope) throws CodeGenerationException {

		 	Integer left_reg = lexp.genLLCode(func, scope);
		 	Integer right_reg = rexp.genLLCode(func, scope);
		 	Operation op = new Operation(Operation.OperationType.UNKNOWN, func.getCurrBlock());

			switch(binop) {
				case PLUS :
					op.setType(Operation.OperationType.ADD_I);
					break;

				case MINUS :
					op.setType(Operation.OperationType.SUB_I);
					break;

				case MULT :
					op.setType(Operation.OperationType.MUL_I);
					break;

				case DIV :
					op.setType(Operation.OperationType.DIV_I);					// Statements
					break;

				case LESS :
					op.setType(Operation.OperationType.LT);
					break;

				case LEQUAL :
					op.setType(Operation.OperationType.LTE);
					break;

				case GREATER :
					op.setType(Operation.OperationType.GT);
					break;

				case GREQUAL :
					op.setType(Operation.OperationType.GTE);
					break;

				case EQUAL :
					op.setType(Operation.OperationType.EQUAL);
					break;

				case NEQUAL :
					op.setType(Operation.OperationType.NOT_EQUAL);
					break;

				// default :
				// 	 throw error if binop is something else
			}

			op.setSrcOperand(0, new Operand(Operand.OperandType.REGISTER, left_reg));
			op.setSrcOperand(1, new Operand(Operand.OperandType.REGISTER, right_reg));
			Integer reg = func.getNewRegNum();
			op.setDestOperand(0, new Operand(Operand.OperandType.REGISTER, reg));
	        func.getCurrBlock().appendOper(op);

	        // Get the return value from retreg.
			// op = new Operation(Operation.OperationType.ASSIGN);

			return reg;
		}

		public void printTree(int level) {
			super.printTree(level);
			System.out.println("Binary expression " + binop.toString());
			lexp.printTree(level+1);
			rexp.printTree(level+1);
		}
	}

	public static class Call extends Expression {
		public String name;
		public ArrayList<Expression> args;

		public Call(String n, ArrayList<Expression> a) {
			name = n;
			args = a;
		}

		public Integer genLLCode(Function func, CompoundStatement scope) throws CodeGenerationException {
			// PASS the parameters
			ArrayList<Integer> regs = new ArrayList<Integer>();
			for(Expression expr:args) {
				regs.add(expr.genLLCode(func, scope));
			}
			int i = 0;
			for(Integer reg:regs) {
				Operation op = new Operation(Operation.OperationType.PASS, func.getCurrBlock());
		        op.addAttribute(new Attribute("PARAM_NUM", ((Integer)i).toString()));
		        op.setSrcOperand(0, new Operand(Operand.OperandType.REGISTER, reg));
	            func.getCurrBlock().appendOper(op);
	            i++;
	        }
					// Perform the call
			Operation op = new Operation(Operation.OperationType.CALL, func.getCurrBlock());
			op.addAttribute(new Attribute("numParams", ((Integer)regs.size()).toString()));
			op.setSrcOperand(0, new Operand(Operand.OperandType.STRING, name));
	        func.getCurrBlock().appendOper(op);


	        // Get the return value from retreg.
			Integer reg = func.getNewRegNum();
			op = new Operation(Operation.OperationType.ASSIGN, func.getCurrBlock());
			Operand in = new Operand(Operand.OperandType.MACRO, "RetReg");
			op.setSrcOperand(0, in);
			op.setDestOperand(0, new Operand(Operand.OperandType.REGISTER, reg));
	        func.getCurrBlock().appendOper(op);

			return reg;
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

		// Search up the scope tree until we find a scope with our variable.
		public Declaration.VarDecl resolve(Function func, CompoundStatement scope) throws CodeGenerationException {
			Declaration.VarDecl decl = null;
			// Look in function level scopes.
			while(scope != null) {
				if(scope.getSymbolTable().containsKey(name)) {
					decl = scope.getSymbolTable().get(name);
					break;
				}
				else {
					scope = scope.getParent();
				}
			}
			// If we didn't find in the function, look in global scope.
			if(decl == null) {
				if(!CMinusCompiler.globalTable.containsKey(name)) {
					throw new CodeGenerationException("Could not find variable " + name + " in any scope.");
				}
				decl = CMinusCompiler.globalTable.get(name);
			}

			return decl;
		}

		public Integer genLLCode(Function func, CompoundStatement scope) throws CodeGenerationException {
			Declaration.VarDecl decl = resolve(func, scope);

			if(decl.getLocation() == Declaration.VarDecl.VarLocation.GLOBAL) {
				// Globals and params must be loaded into registers before use.
				Operand var = new Operand(Operand.OperandType.STRING, name);
				Integer reg = func.getNewRegNum();
				Operand out = new Operand(Operand.OperandType.REGISTER, reg);

				Operation op = new Operation(Operation.OperationType.LOAD_I, func.getCurrBlock());
				op.setSrcOperand(0, var);
				op.setSrcOperand(1, new Operand(Operand.OperandType.INTEGER, 0));
				op.setDestOperand(0, out);

				func.getCurrBlock().appendOper(op);

				return reg;
			}
			else {
				// Regular variables are stored in registers. Return this.
				return decl.getRegister();
			}
		}


		public void printTree(int level) {
			super.printTree(level);
			System.out.println("Var expression: " + name);
			if(index != null) { index.printTree(level+1); }
		}
	}

	public static class Assign extends Expression {
		public Var v;
		public Expression val;

		public Assign(Var vr, Expression e) {
			v = vr;
			val = e;
		}

		public Integer genLLCode(Function func, CompoundStatement scope) throws CodeGenerationException {
			Declaration.VarDecl decl = v.resolve(func, scope);

	        Integer vl_reg = val.genLLCode(func, scope);
			Operand vl = new Operand(Operand.OperandType.REGISTER, vl_reg);

			Operation op;
			if(decl.getLocation() == Declaration.VarDecl.VarLocation.GLOBAL) {
				// If we are assigning to a global or a param, then do a store.
				Operand out = new Operand(Operand.OperandType.STRING, decl.getName());

				op = new Operation(Operation.OperationType.STORE_I, func.getCurrBlock());

				op.setSrcOperand(0, vl);
		        op.setSrcOperand(1, out);
				op.setSrcOperand(2, new Operand(Operand.OperandType.INTEGER, 0));
			}
			else {
				// Else just do an assign.
				Operand out = new Operand(Operand.OperandType.REGISTER, decl.getRegister());

				op = new Operation(Operation.OperationType.ASSIGN, func.getCurrBlock());

				op.setSrcOperand(0, vl);
		        op.setDestOperand(0, out);
			}

	        func.getCurrBlock().appendOper(op);

			// After everything is done we return the value in the variable.
			return vl_reg;
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

		// Put the num into a register.
		public Integer genLLCode(Function func, CompoundStatement scope) throws CodeGenerationException {
			Integer reg = func.getNewRegNum();
			Operand out = new Operand(Operand.OperandType.REGISTER, reg);

			Operation op = new Operation(Operation.OperationType.ASSIGN, func.getCurrBlock());
      op.setSrcOperand(0, new Operand(Operand.OperandType.INTEGER, val));
          op.setDestOperand(0, out);

          func.getCurrBlock().appendOper(op);
      return reg;
    }

    public void printTree(int level) {
      super.printTree(level);
      System.out.println("Num expression: " + val.toString());
    }
  }
}