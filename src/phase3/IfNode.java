package phase3;

// Represents an 'if' statement in the AST, optionally with an 'else' clause.
// For example: "if (x > 0) { print(x); } else { print(0); }"
public class IfNode extends StatementNode {

    // The boolean condition expression inside the parentheses that decides which block runs.
    private final ExpressionNode condition;
    
    // The block of statements to execute if the condition evaluates to true.
    private final BlockNode thenBlock;
    
    // The block of statements to execute if the condition evaluates to false.
    // This can be null if the if-statement doesn't have an else clause.
    private final BlockNode elseBlock;

    // Constructor to initialize an if node with its condition, then-block, and optional else-block.
    public IfNode(ExpressionNode condition, BlockNode thenBlock, BlockNode elseBlock) {
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
    }

    // Getter for the boolean condition expression.
    public ExpressionNode getCondition() { return condition; }
    
    // Getter for the 'then' block.
    public BlockNode getThenBlock() { return thenBlock; }
    
    // Getter for the 'else' block (may return null).
    public BlockNode getElseBlock() { return elseBlock; }

    // Formats this if-node for the AST tree output, showing condition and branch structures clearly.
    @Override
    public String toTreeString(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("If\n"); // Print the "If" root label

        // Print the condition
        sb.append(indent).append("  [condition]\n");
        sb.append(condition.toTreeString(indent + "    ")); // Indent further for the condition AST

        // Print the then-block
        sb.append(indent).append("  [then]\n");
        sb.append(thenBlock.toTreeString(indent + "    "));

        // Print the else-block, but only if one actually exists in the source code
        if (elseBlock != null) {
            sb.append(indent).append("  [else]\n");
            sb.append(elseBlock.toTreeString(indent + "    "));
        }
        return sb.toString();
    }
}
