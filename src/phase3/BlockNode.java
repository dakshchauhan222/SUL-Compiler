package phase3;

import java.util.List;

// Represents a block of code wrapped in curly braces { ... }. 
// Used heavily by 'if', 'else', and 'while' nodes to group multiple statements.
public class BlockNode extends StatementNode {

    // The ordered sequence of statements inside the block.
    private final List<StatementNode> statements;

    // Constructor to initialize a block with its statements.
    public BlockNode(List<StatementNode> statements) {
        this.statements = statements;
    }

    // Getter to retrieve the statements inside the block whenever they need to be executed.
    public List<StatementNode> getStatements() { return statements; }

    // Formats the entire block node for the AST tree output.
    @Override
    public String toTreeString(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("Block\n"); // Print the "Block" label
        
        // Loop through and recursively print all the statements inside the braces
        for (StatementNode stmt : statements) {
            sb.append(stmt.toTreeString(indent + "  "));
        }
        return sb.toString();
    }
}
