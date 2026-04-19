package phase3;

import java.util.List;

// ProgramNode is the root (top-most) node of the entire AST tree.
// A program is essentially just a list of statements executed sequentially.
public class ProgramNode extends ASTNode {

    // A list holding all the top-level statements in the program (like assignments, if-statements, etc.)
    private final List<StatementNode> statements;

    // Constructor to initialize the ProgramNode with a list of parsed statements.
    public ProgramNode(List<StatementNode> statements) {
        this.statements = statements;
    }

    // Getter method to retrieve the list of statements when needed (e.g., during code generation).
    public List<StatementNode> getStatements() { return statements; }

    // Implements ASTNode's tree printing method.
    // This formats the program node and recursively prints all its child statements.
    @Override
    public String toTreeString(String indent) {
        StringBuilder sb = new StringBuilder(); // Used to efficiently build strings
        sb.append(indent).append("Program\n");  // Print the root label
        
        // Loop through every statement inside this program
        for (StatementNode stmt : statements) {
            // Recursively call toTreeString on each child, adding two spaces for indentation
            sb.append(stmt.toTreeString(indent + "  "));
        }
        return sb.toString(); // Return the complete formatted string
    }
}
