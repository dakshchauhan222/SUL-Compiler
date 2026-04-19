package phase3;

import phase2.Lexer;

public class ParserTest {

    public static void main(String[] args) {

        String source =
              "lowkey Main {\n"
            + "  aura main_character() {\n"
            + "    // Variable assignment and arithmetic\n"
            + "    aura x = 5;\n"
            + "    aura y = 10;\n"
            + "    aura sum = x + y;\n"
            + "    aura result = x * 2 + y / 5;\n"
            + "\n"
            + "    // Sus-cap with relational condition\n"
            + "    sus (x < y) {\n"
            + "        yap(x);\n"
            + "    } cap {\n"
            + "        yap(y);\n"
            + "    }\n"
            + "\n"
            + "    // Sus with equality check (no cap)\n"
            + "    sus (x == y) {\n"
            + "        yap(0);\n"
            + "    }\n"
            + "\n"
            + "    // Bruh loop — factorial of 5\n"
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
        System.out.println("  SUL Parser — Phase 3 AST Output");
        System.out.println("=======================================");
        System.out.println();

        Lexer lexer = new Lexer(source);

        Parser parser = new Parser(lexer);
        ProgramNode ast = parser.parse();

        System.out.println(ast.toTreeString(""));

        System.out.println("=======================================");
        System.out.println("  Parsing completed successfully!");
        System.out.println("=======================================");
    }
}
