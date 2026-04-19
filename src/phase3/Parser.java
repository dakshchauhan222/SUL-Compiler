package phase3;

import phase2.Lexer;
import phase2.Token;
import phase2.TokenType;

import java.util.ArrayList;
import java.util.List;

// The Parser class orchestrates syntax analysis using a technique called "Recursive Descent".
// It takes tokens given by the Lexer and determines if they follow the grammatical rules.
// As it verifies grammar, it builds an Abstract Syntax Tree (AST) representing the program's logic hierarchy.
public class Parser {

    // Reference to the Lexer to ask it for a stream of tokens.
    private final Lexer lexer;
    
    // The current lookahead token being examined to decide which grammatical rule applies.
    private Token currentToken;

    // The parser initializes by requesting the very first token from the Lexer.
    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.currentToken = lexer.getNextToken(); 
    }

    // A crucial helper method: "match".
    // It verifies that the current token is what the parser expects it to be (like a SEMICOLON or RPAREN).
    // If it is, the parser eats/consumes that token and fetches the next one in the stream.
    // If it's NOT what was expected, it throws a syntax error immediately, citing line and column numbers.
    private Token match(TokenType expected) {
        if (currentToken.getType() == expected) {
            Token matched = currentToken;
            currentToken = lexer.getNextToken(); // Move forward
            return matched;
        }
        
        String hint = getHintForExpectedToken(expected);
        
        // Throws a helpful compilation error containing positional data.
        throw new RuntimeException(
            "Parse error at line " + currentToken.getLine()
            + ", column " + currentToken.getColumn()
            + ": expected " + expected
            + " but found " + currentToken.getType()
            + "('" + currentToken.getValue() + "'). "
            + "Hint: " + hint
        );
    }
    
    // Provides helpful hints for common syntax errors.
    private String getHintForExpectedToken(TokenType expected) {
        switch(expected) {
            case SEMICOLON: return "Every statement in SUL needs to end with a semicolon ';'. It's like a period at the end of a sentence! Example: x = 5;";
            case LPAREN: return "Missing '('. You need an opening parenthesis here.";
            case RPAREN: return "Missing ')'. Make sure you close all your parentheses like this: ( )";
            case LBRACE: return "Missing '{'. Blocks of code need to be wrapped inside curly braces. Example: { yap(1); }";
            case RBRACE: return "Missing '}'. It looks like you opened a '{' block but forgot to close it! Add a '}' to finish the block.";
            case ASSIGN: return "Expected '=' for variable assignment. SUL requires you to initialize variables immediately! For example, try 'aura x = 0;' instead of just 'aura x;'";
            case IDENTIFIER: return "We expected a variable name here. Variables must start with a letter (e.g., 'aura myVariable = 5;')";
            case LOWKEY: return "Remember, every SUL program must be wrapped inside a 'lowkey ClassName { ... }' block!";
            case MAIN_CHARACTER: return "The entry point of your program must be exactly: 'aura main_character() { ... }'. Did you spell it right?";
            case SLAY: return "Did you forget to 'slay 0;' at the end of main_character? Every GenZ program must slay before it ends!";
            default: return "Double-check the GenZ syntax rules for this line! Something looks a bit off.";
        }
    }

    // Helper to log line number for AST nodes to support clear semantic tracebacks
    private <T extends ASTNode> T withLine(T node, int line) {
        node.setLine(line);
        return node;
    }

    // ────────────────────────────────────────────────────────
    // Below are the Top-Down Recursive Descent Parsing rules.
    // Each method maps directly to a high-level rule in the language's grammar.
    // ────────────────────────────────────────────────────────

    // Parses the root node. A program must be wrapped in `lowkey X { aura main_character() { ... slay 0; } }`
    public ProgramNode parse() {
        int line = currentToken.getLine();
        
        // Parse GenZ wrapper:
        match(TokenType.LOWKEY);
        match(TokenType.IDENTIFIER);
        match(TokenType.LBRACE);
        
        match(TokenType.AURA);
        match(TokenType.MAIN_CHARACTER);
        match(TokenType.LPAREN);
        match(TokenType.RPAREN);
        match(TokenType.LBRACE);
        
        List<StatementNode> statements = parseStatementList();
        
        match(TokenType.SLAY);
        parseExpression(); // Parse return value, e.g., 0
        match(TokenType.SEMICOLON);
        
        match(TokenType.RBRACE); // Close main_character
        match(TokenType.RBRACE); // Close lowkey
        
        match(TokenType.EOF); // If there are leftover non-EOF tokens, it represents bad syntax.
        return withLine(new ProgramNode(statements), line);
    }

    // Iteratively builds a list of statement nodes as long as the current token indicates the start of one.
    private List<StatementNode> parseStatementList() {
        List<StatementNode> statements = new ArrayList<>();
        while (canStartStatement()) {
            statements.add(parseStatement()); // Add parsed valid statements to the list
        }
        return statements;
    }

    // Helps parser decide whether the current loop in "parseStatementList" should continue running.
    // Returning true means the current token is a keyword or identifier beginning a valid statement type.
    private boolean canStartStatement() {
        switch (currentToken.getType()) {
            case IDENTIFIER: // Could be starting an assignment: "x = 5;"
            case AURA:       // Could be starting an assignment with typing: "aura x = 5;"
            case PRINT:      // Could be starting a print statement: "yap(x);"
            case IF:         // Could be an if-block start: "sus(..)"
            case WHILE:      // Could be a loop start: "bruh(..)"
            case LBRACE:     // Could be starting an anonymous standalone block "{ .. }"
                return true;
            default:
                return false;
        }
    }

    // Looks at the current token to make an 'LL(1) lookahead' decision. 
    // It branches execution to the specific handler matching the statement type it suspects.
    private StatementNode parseStatement() {
        switch (currentToken.getType()) {
            case AURA:
            case IDENTIFIER: if (lexer.peekToken().getType() == TokenType.ASSIGN || currentToken.getType() == TokenType.AURA) {
                return parseAssignment();
            } else {
                throw new RuntimeException(
                    "Parse error at line " + currentToken.getLine() + ", column " 
                    + currentToken.getColumn() + ": Invalid statement starting with identifier. Hint: Ensure variables are assigned correctly (e.g., aura x = 5; or x = 5;)."
                );
            }
            case PRINT:      return parsePrintStmt();
            case IF:         return parseIfStmt();
            case WHILE:      return parseWhileStmt();
            case LBRACE:     return parseBlock();
            default:
                // Throws an error explaining a statement wasn't provided where required.
                throw new RuntimeException(
                    "Parse error at line " + currentToken.getLine()
                    + ", column " + currentToken.getColumn()
                    + ": unexpected token " + currentToken.getType()
                    + "('" + currentToken.getValue() + "'). "
                    + "Hint: Expected a valid statement keyword like 'sus', 'bruh', 'yap', or an 'aura' variable assignment."
                );
        }
    }

    // Handler for an assignment statement. Expects: [aura] IDENTIFIER = EXPRESSION ;
    private AssignmentNode parseAssignment() {
        int line = currentToken.getLine();
        if (currentToken.getType() == TokenType.AURA) {
            match(TokenType.AURA);
        }
        Token id = match(TokenType.IDENTIFIER); // Variable name
        match(TokenType.ASSIGN);                // "=" sign
        ExpressionNode expr = parseExpression();       // Complex logical/arithmetic expression on the right side
        match(TokenType.SEMICOLON);             // Terminator
        return withLine(new AssignmentNode(id.getValue(), expr), line);
    }

    // Handler for a print statement. Expects: print ( EXPRESSION ) ;
    private PrintNode parsePrintStmt() {
        int line = currentToken.getLine();
        match(TokenType.PRINT);       // 'print' Keyword
        match(TokenType.LPAREN);      // Open parenthesis
        ExpressionNode expr = parseExpression();
        match(TokenType.RPAREN);      // Close parenthesis
        match(TokenType.SEMICOLON);   // Terminator
        return withLine(new PrintNode(expr), line);
    }

    // Handler for an if-else decision branching statement. Expects: if ( CONDITIONAL ) BLOCK [else BLOCK]
    private IfNode parseIfStmt() {
        int line = currentToken.getLine();
        match(TokenType.IF);
        match(TokenType.LPAREN);
        ExpressionNode condition = parseExpression();
        match(TokenType.RPAREN);

        // Branching executed when expression yields "true" 
        BlockNode thenBlock = parseBlock();

        // Null until proven otherwise. We use a 1-token lookahead to check if the programmer typed "else".
        BlockNode elseBlock = null;
        if (currentToken.getType() == TokenType.ELSE) {
            match(TokenType.ELSE);
            elseBlock = parseBlock(); // Parses the block linked to the else fallback
        }

        return withLine(new IfNode(condition, thenBlock, elseBlock), line);
    }

    // Handler for while-loops. Expects: while ( EXPRESSION ) BLOCK
    private WhileNode parseWhileStmt() {
        int line = currentToken.getLine();
        match(TokenType.WHILE);
        match(TokenType.LPAREN);
        ExpressionNode condition = parseExpression(); // Expression resulting in true/false
        match(TokenType.RPAREN);

        BlockNode body = parseBlock(); // Repeated body logic

        return withLine(new WhileNode(condition, body), line);
    }

    // Handler for blocks (used largely by if/else and while features). Expects: { STATEMENT_LIST }
    private BlockNode parseBlock() {
        int line = currentToken.getLine();
        match(TokenType.LBRACE); // Opening brace
        List<StatementNode> stmts = parseStatementList(); // Plurality of valid statement lines
        match(TokenType.RBRACE); // Closing brace
        return withLine(new BlockNode(stmts), line);
    }

    // ────────────────────────────────────────────────────────
    // Mathematical & Relational Expression resolution follows below.
    // Notice how precedence dictates hierarchy: Highest priority operations (like * and /)
    // are located deeper down the call chain!
    // ────────────────────────────────────────────────────────

    // Begins expression interpretation. Relays to the lowest priority check.
    private ExpressionNode parseExpression() {
        return parseEquality(); 
    }

    // Level 1: Handles Equality checking operators "==" and "!="
    private ExpressionNode parseEquality() {
        ExpressionNode left = parseComparison(); // Resolves higher priority comparisons first 

        // Uses a loop to handle chain logic, e.g., "x == y == z"
        while (currentToken.getType() == TokenType.EQ
            || currentToken.getType() == TokenType.NEQ) {
            int line = currentToken.getLine();
            String op = currentToken.getValue();
            match(currentToken.getType());
            ExpressionNode right = parseComparison(); // Grab right child element
            left = withLine(new BinaryOpNode(op, left, right), line); // Links them into a logical tree segment
        }
        return left;
    }

    // Level 2: Handles relational greater/less checks "<" and ">"
    private ExpressionNode parseComparison() {
        ExpressionNode left = parseTerm(); // Resolves math first before checking comparison

        while (currentToken.getType() == TokenType.LT || 
               currentToken.getType() == TokenType.GT ||
               currentToken.getType() == TokenType.LE ||
               currentToken.getType() == TokenType.GE) {
            int line = currentToken.getLine();
            String op = currentToken.getValue();
            match(currentToken.getType());
            ExpressionNode right = parseTerm();
            left = withLine(new BinaryOpNode(op, left, right), line);
        }
        return left;
    }

    // Level 3: Handles lowest-level math (addition, subtraction) "+" and "-"
    private ExpressionNode parseTerm() {
        ExpressionNode left = parseFactor(); // Resolves high priority math (Multiplication) BEFORE addition

        while (currentToken.getType() == TokenType.PLUS
            || currentToken.getType() == TokenType.MINUS) {
            int line = currentToken.getLine();
            String op = currentToken.getValue();
            match(currentToken.getType());
            ExpressionNode right = parseFactor();
            left = withLine(new BinaryOpNode(op, left, right), line);
        }
        return left;
    }

    // Level 4: Handles highest-level math (multiplication, division) "*" and "/"
    private ExpressionNode parseFactor() {
        ExpressionNode left = parseUnary(); // Resolve unary operators before attempting multiplication/division

        while (currentToken.getType() == TokenType.STAR
            || currentToken.getType() == TokenType.SLASH) {
            int line = currentToken.getLine();
            String op = currentToken.getValue();
            match(currentToken.getType());
            ExpressionNode right = parseUnary();
            left = withLine(new BinaryOpNode(op, left, right), line);
        }
        return left;
    }

    // Level 5: Handles unary operators (currently only negation: "-")
    // Grammar rule: Unary → MINUS Unary | Primary
    // The recursive definition allows chained negation like "--x" (double negative).
    private ExpressionNode parseUnary() {
        if (currentToken.getType() == TokenType.MINUS) {
            int line = currentToken.getLine();
            match(TokenType.MINUS);
            ExpressionNode operand = parseUnary(); // Recursive call allows chaining: -(-x)
            return withLine(new UnaryOpNode("-", operand), line);
        }
        return parsePrimary(); // If no unary operator, just parse a primary value
    }

    // Level 6: The "Primary" level is the very bottom of an expression branch.
    // At this depth, we shouldn't find operators. We're looking for raw values, variables, or (...) groupings.
    private ExpressionNode parsePrimary() {
        int line = currentToken.getLine();
        switch (currentToken.getType()) {
            case NUMBER: {
                Token token = match(TokenType.NUMBER);
                return withLine(new NumberNode(Integer.parseInt(token.getValue())), line); // Converts the raw string to an Integer
            }
            case STRING: {
                Token strToken = match(TokenType.STRING);
                return withLine(new StringNode(strToken.getValue()), line);
            }
            case IDENTIFIER: {
                Token token = match(TokenType.IDENTIFIER);
                return withLine(new IdentifierNode(token.getValue()), line); // Logs memory variable tag
            }
            case LPAREN: {
                // If an opening parenthesis is found, treat its inner contents as a fresh top-level expression.
                // This breaks the precedence rules hierarchy deliberately to prioritize bracketed logic.
                match(TokenType.LPAREN);
                ExpressionNode expr = parseExpression(); 
                match(TokenType.RPAREN);
                return withLine(expr, line);
            }
            case SEMICOLON:
                throw new RuntimeException(
                    "Parse error at line " + currentToken.getLine()
                    + ", column " + currentToken.getColumn()
                    + ": unexpected token " + currentToken.getType()
                    + "('" + currentToken.getValue() + "'). "
                    + "Hint: You left an expression empty! For example, if you meant to initialize a variable, write '0' before the semicolon (e.g., aura x = 0;)"
                );
            case RPAREN:
                throw new RuntimeException(
                    "Parse error at line " + currentToken.getLine()
                    + ", column " + currentToken.getColumn()
                    + ": unexpected token " + currentToken.getType()
                    + "('" + currentToken.getValue() + "'). "
                    + "Hint: You placed empty parentheses '()'. You must put a number or variable inside them (e.g., yap(x);)"
                );
            default:
                // Catches syntax invalidity, e.g. typing "x = 5 + return;"
                throw new RuntimeException(
                    "Parse error at line " + currentToken.getLine()
                    + ", column " + currentToken.getColumn()
                    + ": unexpected token " + currentToken.getType()
                    + "('" + currentToken.getValue() + "'). "
                    + "Hint: Expected a primary value like a NUMBER, IDENTIFIER (variable), or parenthesized expression '('."
                );
        }
    }
}
