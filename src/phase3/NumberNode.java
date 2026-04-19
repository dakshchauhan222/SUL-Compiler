package phase3;

// A leaf node in the AST representing an integer literal number (like 5 or 42).
// Leaf nodes don't have any child nodes; they reside at the bottom of the tree.
public class NumberNode extends ExpressionNode {

    // The actual raw numerical value.
    private final int value;

    // Constructor storing the raw integer value parsed from the source code.
    public NumberNode(int value) {
        this.value = value;
    }

    // Getter returning the numeric value needed during arithmetic or output evaluation.
    public int getValue() { return value; }

    // Output representation for the AST tree, simple enough that no child iteration is required.
    // E.g., prints "  Number(42)"
    @Override
    public String toTreeString(String indent) {
        return indent + "Number(" + value + ")\n";
    }
}
