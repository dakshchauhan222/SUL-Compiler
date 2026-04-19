package phase3;

// Represents a 'while' loop block in the AST.
// For example: "while (x > 0) { x = x - 1; }"
public class WhileNode extends StatementNode {

    // The boolean condition expression that determines whether the loop continues running.
    private final ExpressionNode condition;
    
    // The block consisting of one or more statements to repeatedly execute.
    private final BlockNode body;

    // Constructor to initialize the while-node with its condition and body.
    public WhileNode(ExpressionNode condition, BlockNode body) {
        this.condition = condition;
        this.body      = body;
    }

    // Getter for the loop's boolean condition.
    public ExpressionNode getCondition() { return condition; }
    
    // Getter for the loop's executing body.
    public BlockNode getBody()      { return body;      }

    // Formats this while-node for AST tree output, showing its condition and repeating body components.
    @Override
    public String toTreeString(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("While\n"); // Print the "While" root label

        // Print the loop condition
        sb.append(indent).append("  [condition]\n");
        sb.append(condition.toTreeString(indent + "    ")); // Indent further for readability

        // Print the loop body
        sb.append(indent).append("  [body]\n");
        sb.append(body.toTreeString(indent + "    "));

        return sb.toString();
    }
}