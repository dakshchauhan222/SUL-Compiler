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

    // We track the token we just successfully matched to provide context-aware hints.
    private Token previousToken;

    private static final String[] GENZ_KEYWORDS = {
        "sus", "yap", "aura", "fr", "clears", "flops", "gained", "lost", "drop", 
        "its giving :", "peace out", "no cap :"
    };

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
            previousToken = currentToken;
            Token matched = currentToken;
            currentToken = lexer.getNextToken(); // Move forward
            return matched;
        }
        
        String hint = getHintForExpectedToken(expected);
        
        // Proper Error Statement & Typo Detection
        // If we were expecting 'drop' (=) but failed, check if the previous word was a typo.
        if (expected == TokenType.ASSIGN && previousToken != null && previousToken.getType() == TokenType.IDENTIFIER) {
            String typoHint = checkForTypos(previousToken.getValue());
            if (typoHint != null) {
                hint = typoHint;
            }
        }
        // If the current token itself is an unrecognized word where a keyword was expected
        else if (currentToken.getType() == TokenType.IDENTIFIER) {
             String typoHint = checkForTypos(currentToken.getValue());
             if (typoHint != null) {
                 hint = typoHint;
             }
        }

        // Throws a helpful compilation error containing positional data.
        throw new RuntimeException(
            "Lexical/Syntax Error at line " + currentToken.getLine()
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
            case SEMICOLON: return "Line endings can be semicolons or just newlines!";
            case INDENT: return "This block needs to be indented (shifted right)!";
            case DEDENT: return "Block end expected.";
            case ASSIGN: return "Expected '=' for variable assignment. For example, 'aura x = 0'";
            case IDENTIFIER: return "We expected a variable name here.";
            case SLAY: return "Every GenZ program must slay before it ends!";
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

    // Parses the root node. A program now starts with 'its giving :' and ends with 'peace out'.
    public ProgramNode parse() {
        int line = currentToken.getLine();
        
        match(TokenType.PROGRAM_START);
        skipNewlines();
        
        boolean hasTopLevelIndent = false;
        if (currentToken.getType() == TokenType.INDENT) {
            match(TokenType.INDENT);
            hasTopLevelIndent = true;
        }
        
        List<StatementNode> statements = parseStatementList();
        
        if (hasTopLevelIndent) {
            match(TokenType.DEDENT);
        }
        
        // Final keyword 'peace out'
        if (currentToken.getType() == TokenType.PROGRAM_END) {
            match(TokenType.PROGRAM_END);
        }
        
        skipNewlines();
        match(TokenType.EOF); 
        return withLine(new ProgramNode(statements), line);
    }

    private void skipNewlines() {
        while (currentToken.getType() == TokenType.NEWLINE) {
            match(TokenType.NEWLINE);
        }
    }

    private void consumeTerminator() {
        if (currentToken.getType() == TokenType.SEMICOLON) {
            match(TokenType.SEMICOLON);
        } else if (currentToken.getType() == TokenType.NEWLINE) {
            match(TokenType.NEWLINE);
        } else if (currentToken.getType() == TokenType.EOF || currentToken.getType() == TokenType.DEDENT) {
            // OK
        } else {
             // Fallback: expect a newline
             match(TokenType.NEWLINE);
        }
    }

    // Iteratively builds a list of statement nodes as long as the current token indicates the start of one.
    private List<StatementNode> parseStatementList() {
        List<StatementNode> statements = new ArrayList<>();
        while (true) {
            skipNewlines();
            if (!canStartStatement()) break;
            
            StatementNode stmt = parseStatement();
            statements.add(stmt); 
            
            if (!(stmt instanceof IfNode)) {
                consumeTerminator();
            }
        }
        return statements;
    }

    private boolean canStartStatement() {
        switch (currentToken.getType()) {
            case IDENTIFIER:
            case AURA:      
            case PRINT:     
            case IF:        
                return true;
            default:
                return false;
        }
    }

    private StatementNode parseStatement() {
        switch (currentToken.getType()) {
            case AURA:
            case IDENTIFIER: return parseAssignment();
            case PRINT:      return parsePrintStmt();
            case IF:         return parseIfStmt();
            default:
                throw new RuntimeException("Unexpected token in statement: " + currentToken.getType());
        }
    }

    // Handler for an assignment statement. Expects: [aura] IDENTIFIER = EXPRESSION ;
    private AssignmentNode parseAssignment() {
        int line = currentToken.getLine();
        if (currentToken.getType() == TokenType.AURA) {
            match(TokenType.AURA);
        }
        Token id = match(TokenType.IDENTIFIER); 
        match(TokenType.ASSIGN);                
        ExpressionNode expr = parseExpression();
        return withLine(new AssignmentNode(id.getValue(), expr), line);
    }

    // Handler for a print statement. Expects: print ( EXPRESSION ) ;
    private PrintNode parsePrintStmt() {
        int line = currentToken.getLine();
        match(TokenType.PRINT);       
        ExpressionNode expr = parseExpression();
        return withLine(new PrintNode(expr), line);
    }

    // Handler for an if-else decision branching statement. Expects: if ( CONDITIONAL ) BLOCK [else BLOCK]
    private IfNode parseIfStmt() {
        int line = currentToken.getLine();
        match(TokenType.IF);
        ExpressionNode condition = parseExpression();

        // Optional colon after condition
        if (currentToken.getType() == TokenType.COLON) {
            match(TokenType.COLON);
        }

        BlockNode thenBlock = parseBlock();

        BlockNode elseBlock = null;
        if (currentToken.getType() == TokenType.ELSE) {
            match(TokenType.ELSE);
            // Optional colon after 'no cap :' (though Lexer might include it)
            if (currentToken.getType() == TokenType.COLON) {
                match(TokenType.COLON);
            }
            elseBlock = parseBlock(); 
        }

        return withLine(new IfNode(condition, thenBlock, elseBlock), line);
    }

    // Handler for blocks (used largely by if/else and while features). Expects: { STATEMENT_LIST }
    private BlockNode parseBlock() {
        int line = currentToken.getLine();
        // A block now starts with a newline and indentation.
        // We skip any trailing colon before the block starts.
        if (currentToken.getType() == TokenType.COLON) {
            match(TokenType.COLON);
        }
        consumeTerminator();
        match(TokenType.INDENT);
        List<StatementNode> stmts = parseStatementList();
        match(TokenType.DEDENT);
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

    // ────────────────────────────────────────────────────────
    // Typo Detection Utility Methods
    // ────────────────────────────────────────────────────────

    private String checkForTypos(String word) {
        String closest = null;
        int minDistance = 2; // Threshold for a typo

        for (String keyword : GENZ_KEYWORDS) {
            String baseKeyword = keyword.replace(" :", "").trim();
            int distance = calculateLevenshteinDistance(word.toLowerCase(), baseKeyword.toLowerCase());
            
            if (distance < minDistance) {
                minDistance = distance;
                closest = keyword;
            }
        }

        if (closest != null) {
            return "Unrecognized token '" + word + "'. Did you mean the keyword '" + closest + "'?";
        }
        return null;
    }

    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) dp[i][j] = j;
                else if (j == 0) dp[i][j] = i;
                else {
                    dp[i][j] = Math.min(Math.min(
                        dp[i - 1][j] + 1,
                        dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1)
                    );
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }
}
