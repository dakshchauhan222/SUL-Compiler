package phase4;

import phase2.Lexer;
import phase3.Parser;
import phase3.ProgramNode;

import java.util.List;

// Test driver verifying the SemanticAnalyzer catches logic flaws that the Lexer and Parser miss.
public class SemanticTest {

    public static void main(String[] args) {

        System.out.println("=======================================");
        System.out.println("  SUL Semantic Analyzer (Phase 4)");
        System.out.println("=======================================\n");

        String sourceValid = 
              "lowkey Main {\n"
            + "  aura main_character() {\n"
            + "    // This is perfectly valid code\n"
            + "    aura x = 10;\n"
            + "    aura y = 20;\n"
            + "    sus (x < y) {\n"
            + "        aura sum = x + y;\n"
            + "        yap(sum);\n"
            + "    }\n"
            + "    slay 0;\n"
            + "  }\n"
            + "}\n";

        System.out.println(">>> RUNNING TEST 1 (Valid Code) <<<");
        runSemanticAnalysis(sourceValid);
        System.out.println();


        String sourceUndefined = 
              "lowkey Main {\n"
            + "  aura main_character() {\n"
            + "    // This code tries to print Z before defining it\n"
            + "    aura x = 5;\n"
            + "    yap(z);\n" // Semantic target
            + "    aura z = 10;\n"
            + "    slay 0;\n"
            + "  }\n"
            + "}\n";

        System.out.println(">>> RUNNING TEST 2 (Used Before Assignment) <<<");
        runSemanticAnalysis(sourceUndefined);
        System.out.println();

        String sourceScope = 
              "lowkey Main {\n"
            + "  aura main_character() {\n"
            + "    // Variable scope test\n"
            + "    aura limit = 5;\n"
            + "    sus (limit > 0) {\n"
            + "        aura secret = 99; // Only exists inside this block!\n"
            + "        yap(secret);\n"
            + "    }\n"
            + "    yap(secret); // Target: Should fail because 'secret' died at the '}'\n"
            + "    slay 0;\n"
            + "  }\n"
            + "}\n";

        System.out.println(">>> RUNNING TEST 3 (Block Scoping Violation) <<<");
        runSemanticAnalysis(sourceScope);
        System.out.println();

        System.out.println("=======================================");
    }

    // Helper method seamlessly chaining phases 2, 3, and 4
    private static void runSemanticAnalysis(String source) {
        // Phase 2: Lexing
        Lexer lexer = new Lexer(source);
        
        // Phase 3: Parsing -> AST
        Parser parser = new Parser(lexer);
        ProgramNode ast;
        try {
            ast = parser.parse();
        } catch (Exception e) {
            System.out.println("Parse Error: " + e.getMessage());
            return;
        }

        // Phase 4: Semantic Analysis
        SemanticAnalyzer semantic = new SemanticAnalyzer();
        semantic.analyze(ast);

        List<String> errors = semantic.getErrors();

        // Print results
        if (errors.isEmpty()) {
            System.out.println("Result: No semantic errors");
        } else {
            for (String err : errors) {
                System.out.println("Result: " + err);
            }
        }
    }
}
