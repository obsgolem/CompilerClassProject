package parser;

import java.util.ArrayList;

import lowlevel.*;

public abstract class Declaration {
	String name;
	Token.TokenType type;

	public Declaration(String s, Token.TokenType t) {
		name = s;
		type = t;
	}

	public String getName() {
		return name;
	}

	public abstract CodeItem genLLCodeForTopLevel() throws CodeGenerationException;

	public abstract CodeItem getCodeItem();

	public void printTree(int level) {
		for(int i = 0; i < level; i++) {
			System.out.print("\t");
		}
		System.out.print("Declaration "+ type.toString() + " " + name);
	}

	public static class FunDecl extends Declaration {
		public ArrayList<VarDecl> params;
		public CompoundStatement cpd_stmt;

		public Function funcitem;

		public FunDecl(String s, Token.TokenType t, ArrayList<VarDecl> p, CompoundStatement c) {
			super(s, t);
			params = p;
			cpd_stmt = c;
		}

		public CodeItem genLLCodeForTopLevel() throws CodeGenerationException {
			FuncParam first = null;
			FuncParam prev_param = null;
			for(VarDecl decl:params) {
				FuncParam next = new FuncParam(Data.TYPE_INT, decl.getName(), decl.getSize() != null);
				decl.setParam(next);
				if(first == null) {
					first = next;
					prev_param = first;
				}
				else {
					prev_param.setNextParam(next);
					prev_param = next;
				}
			}

			if(type == Token.TokenType.VOID) {
				funcitem = new Function(Data.TYPE_VOID, name, first);
			}
			else {
				funcitem = new Function(Data.TYPE_INT, name, first);
			}
			funcitem.createBlock0();
			funcitem.genReturnBlock();

			for(VarDecl decl:params) {
				cpd_stmt.addTableEntry(decl);
				decl.setRegister(funcitem.getNewRegNum());
				funcitem.getTable().put(decl.getName(), decl.getRegister());
			}

			BasicBlock block = new BasicBlock(funcitem);
			funcitem.appendBlock(block);
			funcitem.setCurrBlock(block);

			cpd_stmt.genLLCode(funcitem, null);

			// Fix up block stuff
			funcitem.appendBlock(funcitem.getReturnBlock());
			if(funcitem.getFirstUnconnectedBlock() != null) {
				funcitem.getReturnBlock().setNextBlock(funcitem.getFirstUnconnectedBlock());
				funcitem.getFirstUnconnectedBlock().setPrevBlock(funcitem.getReturnBlock());
			}

			return funcitem;
		}

		public CodeItem getCodeItem() {
			return funcitem;
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
		Integer array_size;

		public enum VarLocation {
			GLOBAL,
			PARAM,
			LOCAL,
		}

		VarLocation location;

		// Codegen
		FuncParam param;
		Data data;
		Integer reg;

		public VarDecl(String s, Token.TokenType t, Integer a, VarLocation l) {
			super(s, t);
			data = null;
			array_size = a;
			location = l;
		}

		public Integer getSize() {
			return array_size;
		}

		public VarLocation getLocation() {
			return location;
		}

		public Integer getRegister() {
			return reg;
		}
		public void setRegister(Integer r) {
			reg = r;
		}

		public FuncParam getParam() {
			return param;
		}
		public void setParam(FuncParam p) {
			param = p;
		}

		public CodeItem getCodeItem() {
			return data;
		}

		public CodeItem genLLCodeForTopLevel() throws CodeGenerationException {
			if(array_size == null) {
				data = new Data(Data.TYPE_INT, name);
			}
			else {
				data = new Data(Data.TYPE_INT, name, true, array_size);
			}

			compiler.CMinusCompiler.globalTable.put(name, this);

			return data;
		}


		public void printTree(int level) {
			super.printTree(level);
			System.out.println("(size = " + (array_size == null ? "Not Array" : array_size) + ")");
		}
	}
}