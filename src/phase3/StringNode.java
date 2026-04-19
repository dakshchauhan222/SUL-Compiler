package phase3;

/**
 * An AST node representing a literal string.
 */
public class StringNode extends ExpressionNode {
    private final String value;

    public StringNode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "StringNode(\"" + value + "\")";
    }

    @Override
    public String toTreeString(String indent) {
        return indent + "StringNode: \"" + value + "\"\n";
    }
}
