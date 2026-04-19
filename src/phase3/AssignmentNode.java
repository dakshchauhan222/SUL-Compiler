package phase3;

// Represents a variable assignment in the AST. 
// For example: "x = 5 + 3;"
public class AssignmentNode extends StatementNode {

    // The name of the variable being assigned to (e.g., "x").
    private final String  name;
    
    // The expression on the right-hand side of the '=' sign (e.g., "5 + 3").
    private final ExpressionNode expression;

    // Constructor to initialize the assignment node with the variable name and the evaluated expression.
    public AssignmentNode(String name, ExpressionNode expression) {
        this.name       = name;
        this.expression = expression;
    }

    // Getter to retrieve the variable name.
    public String  getName()       { return name;       }
    
    // Getter to retrieve the right-hand side expression.
    public ExpressionNode getExpression() { return expression; }

    // Formats this assignment node for the AST tree output.
    @Override
    public String toTreeString(String indent) {
        StringBuilder sb = new StringBuilder();
        // Print "Assign(variableName)" with the current indentation level.
        sb.append(indent).append("Assign(").append(name).append(")\n");
        // Recursively print the right-hand side expression, indented further.
        sb.append(expression.toTreeString(indent + "  "));
        return sb.toString();
    }
}
