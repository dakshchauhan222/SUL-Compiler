package phase3;

// Abstract base class for all AST (Abstract Syntax Tree) nodes.
// In compiler design, an AST represents the hierarchical structure of the source code.
public abstract class ASTNode {

    // Abstract method that every specific node type must implement.
    // It takes an 'indent' string (like spaces) to format the tree structure neatly.
    // This allows us to print the AST in a readable format for debugging.
    public abstract String toTreeString(String indent);

    private int line = -1;

    public void setLine(int line) { this.line = line; }
    public int getLine() { return line; }

    // Overrides the default toString method provided by Java.
    // Whenever we try to print an ASTNode, it will call toTreeString with no initial indent.
    @Override
    public String toString() {
        return toTreeString("");
    }
}
