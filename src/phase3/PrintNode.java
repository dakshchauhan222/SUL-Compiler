package phase3;

// Represents a print statement in the AST.
// For example: "print(x + y);"
public class PrintNode extends StatementNode {

    // The expression inside the print statement's parentheses that needs to be evaluated and printed.
    private final ExpressionNode expression;

    // Constructor to initialize the print node with the expression it should print.
    public PrintNode(ExpressionNode expression) {
        this.expression = expression;
    }

    // Getter to retrieve the expression to be printed.
    public ExpressionNode getExpression() { return expression; }

    // Formats this print node for the AST tree output.
    @Override
    public String toTreeString(String indent) {
        StringBuilder sb = new StringBuilder();
        // Print the "Print" label with the current indentation.
        sb.append(indent).append("Print\n");
        // Recursively print the inner expression, indented further.
        sb.append(expression.toTreeString(indent + "  "));
        return sb.toString();
    }
}
