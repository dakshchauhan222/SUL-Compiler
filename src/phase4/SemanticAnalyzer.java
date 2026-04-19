package phase4;

import phase3.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

// The Semantic Analyzer validates the Abstract Syntax Tree (AST) before allowing code generation.
// It acts as Phase 4 in the compiler pipeline.
// 
// Its main responsibility in this language is scoping and variable validation.
// It ensures that variables are declared/assigned before they are used,
// and strictly enforces `{ }` block boundaries (scopes) so inner variables cannot leak out.
public class SemanticAnalyzer {

    // A stack of symbol tables. Each Set<String> represents variables declared within one specific block scope.
    // The stack allows nested scopes. For example: a while loop inside an if statement inside the global scope.
    private final Stack<Set<String>> scopeStack;

    // A list gathering all semantic errors found throughout the entire AST traversal.
    // We collect them instead of crashing immediately so the user sees ALL errors at once.
    private final List<String> errors;

    public SemanticAnalyzer() {
        this.scopeStack = new Stack<>();
        this.errors = new ArrayList<>();
    }

    // Returns the collected semantic errors. Should be checked after analyze() is called.
    public List<String> getErrors() {
        return errors;
    }


    // Initiates the semantic analysis on the root ProgramNode.
    public void analyze(ProgramNode program) {
        // Clear state in case of multiple runs
        scopeStack.clear();
        errors.clear();

        // Push the global scope onto the stack.
        // Variables defined at the top-level of the file live here.
        enterScope();

        // Recursively analyze every top-level statement in the program.
        for (StatementNode stmt : program.getStatements()) {
            visitStatement(stmt);
        }

        // Pop the global scope (good practice, making the stack empty again).
        leaveScope();
    }

    // Route statements to their specific visitors using runtime type checking.
    private void visitStatement(StatementNode stmt) {
        if (stmt instanceof AssignmentNode) {
            visitAssignment((AssignmentNode) stmt);
        } else if (stmt instanceof PrintNode) {
            visitPrint((PrintNode) stmt);
        } else if (stmt instanceof IfNode) {
            visitIf((IfNode) stmt);
        } else if (stmt instanceof WhileNode) {
            visitWhile((WhileNode) stmt);
        } else if (stmt instanceof BlockNode) {
            visitBlock((BlockNode) stmt);
        }
    }

    // Route expressions to their specific visitors.
    private void visitExpression(ExpressionNode expr) {
        if (expr instanceof BinaryOpNode) {
            visitBinaryOp((BinaryOpNode) expr);
        } else if (expr instanceof UnaryOpNode) {
            visitUnaryOp((UnaryOpNode) expr);
        } else if (expr instanceof IdentifierNode) {
            visitIdentifier((IdentifierNode) expr);
        } else if (expr instanceof NumberNode) {
            // Numbers are literals, they are always semantically valid. No action needed.
            visitNumber((NumberNode) expr);
        } else if (expr instanceof StringNode) {
            visitString((StringNode) expr);
        }
    }


    // Creates a new, isolated variable environment in memory.
    private void enterScope() {
        scopeStack.push(new HashSet<>());
    }

    // Destroys the current variable environment. Any variables created inside it are lost forever.
    private void leaveScope() {
        scopeStack.pop();
    }

    // Registers a newly assigned variable into the current (top-most) scope, 
    // UNLESS it already exists in an outer scope (in which case we are just updating an existing variable).
    private void declareOrUpdateVariable(String name) {
        if (!isVariableDeclared(name)) { // Only add if it's genuinely new
            scopeStack.peek().add(name);
        }
    }

    // Checks if the variable exists by searching from the innermost scope (top of stack) 
    // down to the global scope (bottom of stack).
    private boolean isVariableDeclared(String name) {
        // Iterate backwards from the top of the stack downwards.
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            if (scopeStack.get(i).contains(name)) {
                return true;
            }
        }
        return false;
    }


    private void visitAssignment(AssignmentNode node) {
        // 1. Analyze the right-side expression FIRST. The values being used must be valid.
        visitExpression(node.getExpression());

        // 2. Only after right-side is validated, we officially register the left-side variable into memory scope.
        // This prevents things like "x = x + 1" working if "x" wasn't already declared beforehand.
        declareOrUpdateVariable(node.getName());
    }

    private void visitPrint(PrintNode node) {
        // The thing being printed must be semantically valid.
        visitExpression(node.getExpression());
    }

    private void visitBlock(BlockNode node) {
        // A { } block opens an entirely new scope. 
        enterScope();

        // Process all inner statements.
        for (StatementNode stmt : node.getStatements()) {
            visitStatement(stmt);
        }

        // The } closes the scope. Any variables declared strictly inside this block are thrown away.
        leaveScope();
    }

    private void visitIf(IfNode node) {
        // The condition expression must be valid.
        visitExpression(node.getCondition());

        // Process the active branch code. (Blocks natively handle their own scoping via visitBlock)
        visitBlock(node.getThenBlock());

        if (node.getElseBlock() != null) {
            visitBlock(node.getElseBlock());
        }
    }

    private void visitWhile(WhileNode node) {
        // The condition expression must be valid.
        visitExpression(node.getCondition());

        // Process looping body code. (Blocks natively handle their own scoping via visitBlock)
        visitBlock(node.getBody());
    }

    private void visitBinaryOp(BinaryOpNode node) {
        // Both sides of an operation must be valid. e.g. "a + b" means both 'a' and 'b' must exist.
        visitExpression(node.getLeft());
        visitExpression(node.getRight());
    }

    private void visitUnaryOp(UnaryOpNode node) {
        // The negative target must exist. e.g. "-x" means 'x' must exist.
        visitExpression(node.getOperand());
    }

    // **CRITICAL CHECK**: When a variable is requested, does it actually exist in scope?
    private void visitIdentifier(IdentifierNode node) {
        String name = node.getName();
        if (!isVariableDeclared(name)) {
            // Adds a clean error message detailing exactly which variable triggered the crash, including line numbers.
            int line = node.getLine();
            String location = (line != -1) ? " at line " + line : "";
            throw new RuntimeException("Semantic Error at line " + line + ". Hint: The variable '" + name + "' does not exist. You need to create it first! (e.g., 'aura " + name + " = 0;')");
        }
    }

    private void visitNumber(NumberNode node) {
        // Intentionally empty. A raw number (e.g. '5') inherently has no semantic requirements.
    }

    private void visitString(StringNode node) {
        // Intentionally empty. A raw string inherently has no semantic requirements.
    }
}
