# STEP 1: ABSTRACT SYNTAX TREE (AST)

**Goal:**
Convert the simple parser from direct execution/printing into AST construction.

---

## 1. Concept Explanation

Currently, a basic Recursive Descent Parser likely executes code or prints output *while* generating tokens. While simple, this makes advanced features like optimization, type-checking, and code generation very difficult.

An **Abstract Syntax Tree (AST)** is a tree data structure that represents the logical hierarchy of your source code. Instead of executing the code immediately, the parser's new job is to build and return this tree. Each node in the tree represents a construct in your language (like a number, an assignment, or a math operation). Later phases (semantic analysis, code generation) will simply traverse this tree.

---

## 2. Required Code Additions: AST Class Definitions

First, we define a common interface (or abstract class) for all nodes, and then branch out into Statements and Expressions.

```java
// Base Node
public abstract class ASTNode { }

// High-level grouping nodes
public abstract class StatementNode extends ASTNode { }
public abstract class ExpressionNode extends ASTNode { }

// --- ROOT NODE ---
public class ProgramNode extends ASTNode {
    public List<StatementNode> statements;
    public ProgramNode(List<StatementNode> statements) {
        this.statements = statements;
    }
}

// --- STATEMENT NODES ---
public class AssignmentNode extends StatementNode {
    public String identifier;
    public ExpressionNode expression;
    
    public AssignmentNode(String identifier, ExpressionNode expression) {
        this.identifier = identifier;
        this.expression = expression;
    }
}

public class PrintNode extends StatementNode {
    public ExpressionNode expression;
    
    public PrintNode(ExpressionNode expression) {
        this.expression = expression;
    }
}

// --- EXPRESSION NODES ---
public class BinaryExpressionNode extends ExpressionNode {
    public String operator;
    public ExpressionNode left;
    public ExpressionNode right;
    
    public BinaryExpressionNode(String operator, ExpressionNode left, ExpressionNode right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }
}

public class IdentifierNode extends ExpressionNode {
    public String name;
    public IdentifierNode(String name) { this.name = name; }
}

public class NumberNode extends ExpressionNode {
    public int value;
    public NumberNode(int value) { this.value = value; }
}
```

---

## 3. Required Code Changes: Updated Parser Functions

Now we modify the recursive descent parser. Instead of calculating results, the methods will construct and return the AST nodes we just created.

```java
public class Parser {
    // ... Lexer and match() logic remains the same ...

    // Parse entire program
    public ProgramNode parse() {
        List<StatementNode> statements = new ArrayList<>();
        while (currentToken.getType() != TokenType.EOF) {
            statements.add(parseStatement());
        }
        return new ProgramNode(statements);
    }

    // Example of a statement: Assignment (x = 5 + 3;)
    private AssignmentNode parseAssignment() {
        Token id = match(TokenType.IDENTIFIER); 
        match(TokenType.ASSIGN);                
        ExpressionNode expr = parseExpression(); // Right hand side      
        match(TokenType.SEMICOLON);             
        
        // Return an AST Node representing the assignment!
        return new AssignmentNode(id.getValue(), expr);
    }

    // Standard recursive descent logic parsing addition/subtraction
    private ExpressionNode parseTerm() {
        ExpressionNode left = parsePrimary(); // Assuming parsePrimary handles numbers/IDs
        
        while (currentToken.getType() == TokenType.PLUS || currentToken.getType() == TokenType.MINUS) {
            String op = currentToken.getValue();
            match(currentToken.getType());
            ExpressionNode right = parsePrimary();
            
            // Link the left and right sides together into a binary tree node
            left = new BinaryExpressionNode(op, left, right);
        }
        return left;
    }
    
    // Lowest level logic (handling a raw number or variable name)
    private ExpressionNode parsePrimary() {
        if (currentToken.getType() == TokenType.NUMBER) {
            Token numToken = match(TokenType.NUMBER);
            return new NumberNode(Integer.parseInt(numToken.getValue()));
        } else if (currentToken.getType() == TokenType.IDENTIFIER) {
            Token idToken = match(TokenType.IDENTIFIER);
            return new IdentifierNode(idToken.getValue());
        }
        // ... exception handling omitted for brevity
        return null;
    }
}
```

---

## 4. Example AST Structure

Let's look at what the parser now outputs for the input code:
`x = 5 + 3;`

**Output Tree Structure (Conceptual representation):**

```text
ProgramNode
└── AssignmentNode (identifier: "x")
    └── BinaryExpressionNode (operator: "+")
        ├── NumberNode (value: 5)
        └── NumberNode (value: 3)
```
