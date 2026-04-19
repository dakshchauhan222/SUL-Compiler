package phase3;

// A node that represents any binary operation. This means an operator that acts on two pieces of data.
// Examples of binary operators include arithmetic (+, -, *, /) and relational/comparison (==, <, >) operators.
// So, "5 + 3" or "x < 10" will both be structured as a BinaryOpNode.
public class BinaryOpNode extends ExpressionNode {

    // The symbol representing the operation (e.g., "+", "==", "<").
    private final String  operator;
    
    // The expression node on the left side of the operator.
    private final ExpressionNode left;
    
    // The expression node on the right side of the operator.
    private final ExpressionNode right;

    // Constructor to link the left operand, the right operand, and the operator uniting them.
    public BinaryOpNode(String operator, ExpressionNode left, ExpressionNode right) {
        this.operator = operator;
        this.left     = left;
        this.right    = right;
    }

    // Getter for the operator string symbol.
    public String  getOperator() { return operator; }
    
    // Getter for the left hand expression side.
    public ExpressionNode getLeft()     { return left;     }
    
    // Getter for the right hand expression side.
    public ExpressionNode getRight()    { return right;    }

    // Formats the binary operation logic clearly for the AST tree output, 
    // placing the operator at the "top" and the operands slightly indented underneath.
    @Override
    public String toTreeString(String indent) {
        StringBuilder sb = new StringBuilder();
        // Displays like "BinaryOp(+)"
        sb.append(indent).append("BinaryOp(").append(operator).append(")\n");
        // Displays the left branch recursively under the operator node.
        sb.append(left.toTreeString(indent + "  "));
        // Displays the right branch recursively under the operator node.
        sb.append(right.toTreeString(indent + "  "));
        return sb.toString();
    }
}
