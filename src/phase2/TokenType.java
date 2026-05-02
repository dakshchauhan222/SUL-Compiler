package phase2;

// Enumeration of every token type in the SUL language.
public enum TokenType {

    // Keywords
    IF, ELSE, PRINT, AURA, SLAY,
    PROGRAM_START, PROGRAM_END,

    // Arithmetic Operators (GenZ Version)
    PLUS, MINUS, STAR, SLASH,

    // Assignment Operator
    ASSIGN,

    // Relational Operators
    GT, LT, EQ, NEQ, LE, GE,

    // Symbols / Delimiters
    LPAREN, RPAREN, LBRACE, RBRACE, SEMICOLON, COLON,

    // Identifiers & Literals
    IDENTIFIER, NUMBER, STRING,

    // Indentation & Newlines
    INDENT, DEDENT, NEWLINE,

    // Special
    EOF
}
