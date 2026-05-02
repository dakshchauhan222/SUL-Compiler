package phase2;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

// Hand-written lexical analyser for the SUL language.
// Updated to support Unstructured GenZ syntax (Indentation-based).
public class Lexer {

    private final String source;   // entire source code as one string
    private int    pos;        // current position in string
    private int    line;       // current line number, error reporting
    private int    column;     // current column number, error reporting

    private Stack<Integer> indentLevels;
    private Queue<Token> pendingTokens;
    private boolean atStartOfLine;

    public Lexer(String source) {
        this.source = source;
        this.pos    = 0;
        this.line   = 1;
        this.column = 1;
        this.indentLevels = new Stack<>();
        this.indentLevels.push(0); // Initial indent level
        this.pendingTokens = new LinkedList<>();
        this.atStartOfLine = true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(pos);
    }

    private char peekNext() {
        int nextPos = pos + 1;
        if (nextPos >= source.length()) return '\0';
        return source.charAt(nextPos);
    }

    private char advance() {
        if(isAtEnd()) return '\0';
        char ch = source.charAt(pos);
        pos++;
        if (ch == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return ch;
    }

    private boolean isAtEnd() {
        return pos >= source.length();
    }

    private void skipLine() {
        while (!isAtEnd() && peek() != '\n') {
            advance();
        }
    }

    private boolean isAlpha(char ch) {
        return (ch >= 'a' && ch <= 'z')
            || (ch >= 'A' && ch <= 'Z')
            || ch == '_';
    }

    private boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private boolean isAlphaNumeric(char ch) {
        return isAlpha(ch) || isDigit(ch);
    }

    private Token scanNumber(int startLine, int startColumn) {
        StringBuilder sb = new StringBuilder();
        while (!isAtEnd() && isDigit(peek())) {
            sb.append(advance());
        }
        return new Token(TokenType.NUMBER, sb.toString(), startLine, startColumn);
    }

    private Token scanString(int startLine, int startColumn) {
        StringBuilder sb = new StringBuilder();
        while (!isAtEnd() && peek() != '"') {
            sb.append(advance());
        }
        
        if (isAtEnd()) {
            throw new RuntimeException("Unterminated string at line " + startLine);
        }
        
        advance(); // consume closing quote
        return new Token(TokenType.STRING, sb.toString(), startLine, startColumn);
    }

    private Token scanIdentifierOrKeyword(int startLine, int startColumn) {
        StringBuilder sb = new StringBuilder();
        while (!isAtEnd() && isAlphaNumeric(peek())) {
            sb.append(advance());
        }
        String word = sb.toString();
        TokenType type = lookupKeyword(word);
        return new Token(type, word, startLine, startColumn);
    }

    private TokenType lookupKeyword(String word) {
        switch (word) {
            case "sus":    return TokenType.IF;
            case "yap":    return TokenType.PRINT;
            case "aura":   return TokenType.AURA;
            case "fr":     return TokenType.NEWLINE; 
            case "clears": return TokenType.GT;
            case "flops":  return TokenType.LT;
            case "gained": return TokenType.PLUS;
            case "lost":   return TokenType.MINUS;
            case "drop":   return TokenType.ASSIGN;
            default:       return TokenType.IDENTIFIER;
        }
    }

    public Token getNextToken() {
        if (!pendingTokens.isEmpty()) return pendingTokens.poll();

        while (!isAtEnd()) {
            if (atStartOfLine) {
                atStartOfLine = false;
                int spaces = 0;
                while (!isAtEnd() && (peek() == ' ' || peek() == '\t')) {
                    if (peek() == '\t') spaces += 4; else spaces++;
                    advance();
                }

                if (isAtEnd()) break;
                if (peek() == '\n' || peek() == '\r') {
                    advance();
                    if (peek() == '\n') advance();
                    atStartOfLine = true;
                    continue;
                }
                if (peek() == '/' && peekNext() == '/') {
                    skipLine();
                    atStartOfLine = true;
                    continue;
                }

                int prevIndent = indentLevels.peek();
                if (spaces > prevIndent) {
                    indentLevels.push(spaces);
                    return new Token(TokenType.INDENT, "", line, column);
                } else if (spaces < prevIndent) {
                    while (!indentLevels.isEmpty() && spaces < indentLevels.peek()) {
                        indentLevels.pop();
                        pendingTokens.add(new Token(TokenType.DEDENT, "", line, column));
                    }
                    if (indentLevels.isEmpty() || spaces != indentLevels.peek()) {
                        throw new RuntimeException("Inconsistent indentation at line " + line);
                    }
                    if (!pendingTokens.isEmpty()) return pendingTokens.poll();
                }
            }

            // Normal tokenization
            while (!isAtEnd() && (peek() == ' ' || peek() == '\t')) advance();

            if (isAtEnd()) break;

            int startLine = line;
            int startColumn = column;

            // Check for multi-word keywords
            String remaining = source.substring(pos).toLowerCase();
            if (remaining.startsWith("its giving :")) {
                for (int i=0; i < "its giving :".length(); i++) advance();
                return new Token(TokenType.PROGRAM_START, "its giving :", startLine, startColumn);
            }
            if (remaining.startsWith("peace out")) {
                for (int i=0; i < "peace out".length(); i++) advance();
                return new Token(TokenType.PROGRAM_END, "peace out", startLine, startColumn);
            }
            if (remaining.startsWith("no cap :")) {
                for (int i=0; i < "no cap :".length(); i++) advance();
                return new Token(TokenType.ELSE, "no cap :", startLine, startColumn);
            }
            if (remaining.startsWith("hits same")) {
                for (int i=0; i < "hits same".length(); i++) advance();
                return new Token(TokenType.EQ, "hits same", startLine, startColumn);
            }

            char ch = peek();

            if (ch == '\n' || ch == '\r') {
                advance();
                if (ch == '\r' && peek() == '\n') advance();
                atStartOfLine = true;
                return new Token(TokenType.NEWLINE, "\n", startLine, startColumn);
            }

            if (ch == '/' && peekNext() == '/') {
                skipLine();
                atStartOfLine = true;
                return new Token(TokenType.NEWLINE, "\n", startLine, startColumn);
            }

            if (isDigit(ch)) return scanNumber(startLine, startColumn);
            if (isAlpha(ch)) return scanIdentifierOrKeyword(startLine, startColumn);

            advance();
            switch (ch) {
                case '"': return scanString(startLine, startColumn);
                case '+': return new Token(TokenType.PLUS, "+", startLine, startColumn);
                case '-': return new Token(TokenType.MINUS, "-", startLine, startColumn);
                case '*': return new Token(TokenType.STAR, "*", startLine, startColumn);
                case '/': return new Token(TokenType.SLASH, "/", startLine, startColumn);
                case ':': return new Token(TokenType.COLON, ":", startLine, startColumn);
                case ';': return new Token(TokenType.SEMICOLON, ";", startLine, startColumn);
                case '=':
                    if (peek() == '=') { advance(); return new Token(TokenType.EQ, "==", startLine, startColumn); }
                    return new Token(TokenType.ASSIGN, "=", startLine, startColumn);
                case '<':
                    if (peek() == '=') { advance(); return new Token(TokenType.LE, "<=", startLine, startColumn); }
                    return new Token(TokenType.LT, "<", startLine, startColumn);
                case '>':
                    if (peek() == '=') { advance(); return new Token(TokenType.GE, ">=", startLine, startColumn); }
                    return new Token(TokenType.GT, ">", startLine, startColumn);
                case '!':
                    if (peek() == '=') { advance(); return new Token(TokenType.NEQ, "!=", startLine, startColumn); }
                    throw new RuntimeException("Unexpected ! at line " + startLine);
                case '(': return new Token(TokenType.LPAREN, "(", startLine, startColumn);
                case ')': return new Token(TokenType.RPAREN, ")", startLine, startColumn);
                default:
                    throw new RuntimeException("Unexpected character " + ch + " at line " + startLine);
            }
        }

        // EOF: Close all open indentations
        while (indentLevels.size() > 1) {
            indentLevels.pop();
            pendingTokens.add(new Token(TokenType.DEDENT, "", line, column));
        }
        pendingTokens.add(new Token(TokenType.EOF, "", line, column));
        return pendingTokens.poll();
    }

    public Token peekToken() {
        // Save state
        int savedPos = pos;
        int savedLine = line;
        int savedColumn = column;
        boolean savedAtStartOfLine = atStartOfLine;
        Stack<Integer> savedIndents = new Stack<>();
        savedIndents.addAll(indentLevels);
        Queue<Token> savedPending = new LinkedList<>(pendingTokens);

        Token token = getNextToken();

        // Restore state
        pos = savedPos;
        line = savedLine;
        column = savedColumn;
        atStartOfLine = savedAtStartOfLine;
        indentLevels = savedIndents;
        pendingTokens = savedPending;

        return token;
    }

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
