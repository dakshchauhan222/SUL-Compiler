package phase2;

// Enumeration of every token type in the SUL language.
public enum TokenType {

    // Keywords
    IF, ELSE, WHILE, PRINT,
    LOWKEY, AURA, MAIN_CHARACTER, SLAY,

    // Arithmetic Operators
    PLUS, MINUS, STAR, SLASH,

    // Assignment Operator
    ASSIGN,

    // Relational Operators
    GT, LT, EQ, NEQ, LE, GE,

    // Symbols / Delimiters
    LPAREN, RPAREN, LBRACE, RBRACE, SEMICOLON,

    // Identifiers & Literals
    IDENTIFIER, NUMBER, STRING,

    // Special
    EOF
}
