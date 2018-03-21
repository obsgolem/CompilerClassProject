package compiler;

public abstract class Statement {
    // The abstract Statement class contains a nextSibling ref
    // All other statements inherit this from Statement
    // Or better yet … ArrayList<Statement>

	public void printTree(int level) {
		for(int i = 0; i < level; i++) {
            System.out.print("\t");
        }
	}
}
