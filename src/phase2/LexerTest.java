package phase2;

import java.util.List;

// Test driver for the SUL lexer.
public class LexerTest {

    public static void main(String[] args) {

        // Sample SUL program
        String source =
              "lowkey Main {\n"
            + "  aura main_character() {\n"
            + "    // Variable assignment\n"
            + "    aura x = 5;\n"
            + "    aura y = 10;\n"
            + "\n"
            + "    // Arithmetic\n"
            + "    aura sum = x + y;\n"
            + "    aura diff = x - y;\n"
            + "    aura prod = x * y;\n"
            + "    aura quot = x / y;\n"
            + "\n"
            + "    // Relational & sus-cap\n"
            + "    sus (x < y) {\n"
            + "        yap(x);\n"
            + "    } cap {\n"
            + "        yap(y);\n"
            + "    }\n"
            + "\n"
            + "    // Equality and inequality\n"
            + "    sus (x == y) {\n"
            + "        yap(1);\n"
            + "    }\n"
            + "    sus (x != y) {\n"
            + "        yap(0);\n"
            + "    }\n"
            + "\n"
            + "    // Bruh loop\n"
            + "    aura n = 5;\n"
            + "    aura fact = 1;\n"
            + "    bruh (n > 0) {\n"
            + "        fact = fact * n;\n"
            + "        n = n - 1;\n"
            + "    }\n"
            + "    yap(fact);\n"
            + "    slay 0;\n"
            + "  }\n"
            + "}\n";

        System.out.println("=======================================");
        System.out.println("  SUL Lexer - Phase 2 Token Output");
        System.out.println("=======================================");
        System.out.println();
        // call the lexer to tokenize the source code
        Lexer lexer = new Lexer(source);
        // get the list of tokens produced by the lexer
        List<Token> tokens = lexer.tokenize();

        for (Token token : tokens) {
            System.out.println(token);
        }

        System.out.println();
        System.out.println("Total tokens: " + tokens.size());
        System.out.println("=======================================");
    }
}

//"Here is a sample program written in our SUL language containing assignments, arithmetic, if-else, and while loops. First, our Lexer reads this raw text, strips out comments and whitespace, and packages the characters into meaningful 'Tokens'."