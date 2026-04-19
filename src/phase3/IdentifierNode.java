package phase3;

// A leaf node in the AST representing a variable name reference (an identifier).
// Like NumberNode, this is a leaf node sitting at the bottom of the tree structure.
// E.g., if code says "x = y + 5", "y" is parsed as an IdentifierNode in the right-side expression.
public class IdentifierNode extends ExpressionNode {

    // The raw string name of the variable.
    private final String name;

    // Constructor holding onto the actual string name of the referenced variable.
    public IdentifierNode(String name) {
        this.name = name;
    }

    // Getter to retrieve the variable name, generally used later by the semantic analyzer/environment lookup.
    public String getName() { return name; }

    // Output formatting for the AST tree, displaying something like "  Identifier(myVar)"
    @Override
    public String toTreeString(String indent) {
        return indent + "Identifier(" + name + ")\n";
    }
}
