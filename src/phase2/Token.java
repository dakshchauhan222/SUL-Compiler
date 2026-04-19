package phase2;

// Represents a single token produced by the SUL lexer.
public class Token {

    private final TokenType type;
    private final String    value;
    private final int       line;
    private final int       column;

    public Token(TokenType type, String value, int line, int column) {
        this.type   = type;
        this.value  = value;
        this.line   = line;
        this.column = column;
    }

    public TokenType getType()   { return type;   }
    public String    getValue()  { return value;  }
    public int       getLine()   { return line;   }
    public int       getColumn() { return column; }

    // Format: TYPE(value)
    @Override
    public String toString() {
        return type + "(" + value + ")";
    }
}
