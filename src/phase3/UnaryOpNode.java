package phase3;

// Represents a unary operation in the AST (e.g., negation: -5 or -x).
// A unary operator acts on a single operand, unlike BinaryOpNode which needs two.
public class UnaryOpNode extends ExpressionNode {

    // The unary operator symbol (currently only "-" for negation).
    private final String operator;

    // The expression the operator applies to (e.g., in "-x", the operand is IdentifierNode("x")).
    private final ExpressionNode operand;

    // Constructor to initialize the unary operation with its operator and target expression.
    public UnaryOpNode(String operator, ExpressionNode operand) {
        this.operator = operator;
        this.operand  = operand;
    }

    // Getter for the operator symbol.
    public String  getOperator() { return operator; }

    // Getter for the operand expression.
    public ExpressionNode getOperand()  { return operand;  }

    // Formats this unary operation for the AST tree output.
    @Override
    public String toTreeString(String indent) {
        StringBuilder sb = new StringBuilder();
        // Displays like "UnaryOp(-)"
        sb.append(indent).append("UnaryOp(").append(operator).append(")\n");
        // Recursively prints the operand, indented further.
        sb.append(operand.toTreeString(indent + "  "));
        return sb.toString();
    }
}