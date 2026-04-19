package phase2;
import java.util.ArrayList;
import java.util.List;

// Hand-written lexical analyser for the SUL language.
public class Lexer {

    private final String source;   // entire source code as one string
    private int    pos;        // current position in string
    private int    line;       // current line number, error reporting
    private int    column;     // current column number, error reporting

    public Lexer(String source) {
        this.source = source;
        this.pos    = 0;
        this.line   = 1;
        this.column = 1;
    }

    // Returns the current character without consuming it. the use of this function is to check the next character without consuming it because the parser needs to look ahead to determine the type of token.
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(pos);
    }

    // Returns the character one position ahead without consuming. The use of this function is to check the next character without consuming it because the parser needs to look ahead to determine the type of token.
    //the diff btw peek() and peekNext() is that peek() returns the current character and peekNext() returns the next character.Used for multi-character tokens like == and !=
    private char peekNext() {
        int nextPos = pos + 1;
        if (nextPos >= source.length()) return '\0';
        return source.charAt(nextPos);
    }

    // Consumes the current character and advances the position. The use of this function is to consume the current character and advance the position because we need to move to the next character to continue scanning. Used for safety check
    private char advance() {
        if(isAtEnd()) return '\0';
        //Move forward → consume character
        char ch = source.charAt(pos);
        pos++;
        //Track position
        if (ch == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return ch;
    }

    // Returns true if the lexer has reached the end of the source code.
    private boolean isAtEnd() {
        return pos >= source.length();
    }

    // Skips whitespace and single-line comments (// … end-of-line).
    private void skipWhitespace() {
        while (!isAtEnd()) {
            char ch = peek();

            if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') {
                advance();
                continue;
            }

            if (ch == '/' && peekNext() == '/') {
                while (!isAtEnd() && peek() != '\n') {
                    advance();
                }
                continue;
            }

            break;
        }
    }
    // Returns true if the character is an alphabet.
    private boolean isAlpha(char ch) {
        return (ch >= 'a' && ch <= 'z')
            || (ch >= 'A' && ch <= 'Z')
            || ch == '_';
    }

    // Returns true if the character is a digit.
    private boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    // Returns true if the character is an alphabet or a digit.
    private boolean isAlphaNumeric(char ch) {
        return isAlpha(ch) || isDigit(ch);
    }

    // Scans a NUMBER token (one or more digits).
    private Token scanNumber(int startLine, int startColumn) {
        StringBuilder sb = new StringBuilder();
        while (!isAtEnd() && isDigit(peek())) {
            sb.append(advance());
        }
        return new Token(TokenType.NUMBER, sb.toString(), startLine, startColumn);
    }

    // Scans a STRING token.
    private Token scanString(int startLine, int startColumn) {
        StringBuilder sb = new StringBuilder();
        while (!isAtEnd() && peek() != '"') {
            if (peek() == '\n') {
                line++;
                column++;
            }
            sb.append(advance());
        }
        
        if (isAtEnd()) {
            throw new RuntimeException(
                "Lexer error at line " + startLine + ", column " + startColumn
                + ": Unterminated string literal. Hint: Looks like you forgot to close your string! Make sure you add a closing quote (\") at the end of your text."
            );
        }
        
        advance(); // Consume the closing quote
        return new Token(TokenType.STRING, sb.toString(), startLine, startColumn);
    }

    // Scans an IDENTIFIER or KEYWORD token.
    private Token scanIdentifierOrKeyword(int startLine, int startColumn) {
        StringBuilder sb = new StringBuilder();
        while (!isAtEnd() && isAlphaNumeric(peek())) {
            sb.append(advance());
        }
        String word = sb.toString();
        TokenType type = lookupKeyword(word);
        return new Token(type, word, startLine, startColumn);
    }

    // Returns the keyword TokenType for the given word, or IDENTIFIER if not a keyword.
    private TokenType lookupKeyword(String word) {
        switch (word) {
            case "sus":    return TokenType.IF;
            case "cap":  return TokenType.ELSE;
            case "bruh": return TokenType.WHILE;
            case "yap": return TokenType.PRINT;
            case "lowkey": return TokenType.LOWKEY;
            case "aura": return TokenType.AURA;
            case "main_character": return TokenType.MAIN_CHARACTER;
            case "slay": return TokenType.SLAY;
            default:      return TokenType.IDENTIFIER;
        }
    }

    // Returns the next token from the input.
    public Token getNextToken() {
        skipWhitespace();

        if (isAtEnd()) {
            return new Token(TokenType.EOF, "", line, column);
        }

        int startLine   = line;
        int startColumn = column;
        char ch = peek();

        if (isDigit(ch)) {
            return scanNumber(startLine, startColumn);
        }

        if (isAlpha(ch)) {
            return scanIdentifierOrKeyword(startLine, startColumn);
        }

        advance();

        switch (ch) {
            case '"': return scanString(startLine, startColumn);
            case '+': return new Token(TokenType.PLUS,   "+", startLine, startColumn);
            case '-': return new Token(TokenType.MINUS,  "-", startLine, startColumn);
            case '*': return new Token(TokenType.STAR,   "*", startLine, startColumn);
            case '/': return new Token(TokenType.SLASH,  "/", startLine, startColumn);

            case '<':
                    if (peek() == '=') {
                        advance();
                        return new Token(TokenType.LE, "<=", startLine, startColumn);
                    }
                    return new Token(TokenType.LT, "<", startLine, startColumn);

            case '>':
                if (peek() == '=') {
                    advance();
                    return new Token(TokenType.GE, ">=", startLine, startColumn);
                }
                return new Token(TokenType.GT, ">", startLine, startColumn);

            case '=':
                if (peek() == '=') {
                    advance();
                    return new Token(TokenType.EQ, "==", startLine, startColumn);
                }
                return new Token(TokenType.ASSIGN, "=", startLine, startColumn);

            case '!':
                if (peek() == '=') {
                    advance();
                    return new Token(TokenType.NEQ, "!=", startLine, startColumn);
                }
                throw new RuntimeException(
                    "Lexer error at line " + startLine + ", column " + startColumn
                    + ": unexpected character '!'. Hint: A single '!' doesn't do anything by itself! If you want to check if two things are 'not equal', use '!=' instead."
                );

            case '(': return new Token(TokenType.LPAREN,    "(", startLine, startColumn);
            case ')': return new Token(TokenType.RPAREN,    ")", startLine, startColumn);
            case '{': return new Token(TokenType.LBRACE,    "{", startLine, startColumn);
            case '}': return new Token(TokenType.RBRACE,    "}", startLine, startColumn);
            case ';': return new Token(TokenType.SEMICOLON, ";", startLine, startColumn);

            default:
                throw new RuntimeException(
                    "Lexer error at line " + startLine + ", column " + startColumn
                    + ": unexpected character '" + ch + "'. Hint: Oops, the character '" + ch + "' is not allowed in SUL! Double-check your typing."
                );
        }
    }

    // Lookahead (VERY IMPORTANT for parser)
    public Token peekToken() {
        int savedPos = pos;
        int savedLine = line;
        int savedColumn = column;

        Token token = getNextToken();

        pos = savedPos;
        line = savedLine;
        column = savedColumn;

        return token;
    }

    // Tokenises the entire source and returns all tokens (including EOF).
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        Token token;
        do {
            token = getNextToken();
            tokens.add(token);
        } while (token.getType() != TokenType.EOF);
        return tokens;
    }
}
//"The lexer scans the input character-by-character using a pointer. It uses lookahead to distinguish multi-character operators like == and !=, skips whitespace and comments, and generates tokens using a combination of pattern matching and state-based scanning."
