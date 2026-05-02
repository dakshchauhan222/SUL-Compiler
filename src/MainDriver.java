import phase2.Lexer;
import phase3.Parser;
import phase3.ProgramNode;
import phase4.SemanticAnalyzer;
import phase5.CodeGenerator;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;

public class MainDriver {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("==================================================");
        System.out.println("  SUL Compiler - Full Pipeline Integration");
        System.out.println("==================================================");
        System.out.println("Enter your source code (Ctrl+Z then Enter on Windows, or Ctrl+D on Linux to finish and compile):");
        System.out.println("--------------------------------------------------------------------------------");

        StringBuilder sourceCode = new StringBuilder();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            sourceCode.append(line).append("\n");
        }

        String code = sourceCode.toString();
        if (code.trim().isEmpty()) {
            System.out.println("No source code entered. Exiting.");
            return;
        }

        System.out.println("\n[Compiling...]");

        try {
            // -----------------------------------------------------------------
            // Phase 2: Lexical Analysis
            // -----------------------------------------------------------------
            System.out.println("[Phase 1/4] Running Lexer...");
            Lexer lexer = new Lexer(code);
            List<phase2.Token> tokens = lexer.tokenize();
            System.out.println("\n--- Tokens ---");
            for (phase2.Token t : tokens) {
                System.out.println(t);
            }
            
            // -----------------------------------------------------------------
            // Phase 3: Parsing & AST Construction
            // -----------------------------------------------------------------
            System.out.println("\n[Phase 2/4] Running Parser...");
            // Re-initialize lexer for the parser since tokenize() consumed the stream
            lexer = new Lexer(code);
            Parser parser = new Parser(lexer);
            ProgramNode astRoot = parser.parse();
            
            System.out.println("\n--- Abstract Syntax Tree (AST) ---");
            System.out.println(astRoot.toString());

            // -----------------------------------------------------------------
            // Phase 4: Semantic Analysis
            // -----------------------------------------------------------------
            System.out.println("[Phase 3/4] Running Semantic Analyzer...");
            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
            semanticAnalyzer.analyze(astRoot);
            
            List<String> semanticErrors = semanticAnalyzer.getErrors();
            if (!semanticErrors.isEmpty()) {
                System.err.println("\n[!] Semantic Errors Found:");
                for (String error : semanticErrors) {
                    System.err.println("  - " + error);
                }
                System.err.println("\nCompilation aborted due to semantic errors.");
                return;
            } else {
                System.out.println("\n--- Semantic Analysis Result ---");
                System.out.println("Success! No semantic errors found.\n");
            }

            // -----------------------------------------------------------------
            // Phase 5: Code Generation
            // -----------------------------------------------------------------
            System.out.println("[Phase 4/4] Running Code Generation...");
            CodeGenerator codeGenerator = new CodeGenerator();
            String assembly = codeGenerator.generate(astRoot);

            // -----------------------------------------------------------------
            // Output
            // -----------------------------------------------------------------
            try (FileWriter fileWriter = new FileWriter("output.asm")) {
                fileWriter.write(assembly);
            } catch (IOException e) {
                System.err.println("[!] Failed to write to output.asm: " + e.getMessage());
                return;
            }

            System.out.println("\n--- Assembly Generated ---\n");
            
            // -----------------------------------------------------------------
            // Native Compilation & Execution
            // -----------------------------------------------------------------
            boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
            
            String[] nasmCmd;
            String[] linkCmd;
            String[] execCmd;
            
            if (isWindows) {
                // Windows alternative (NASM + GCC)
                nasmCmd = new String[]{"nasm", "-f", "win64", "output.asm"};
                linkCmd = new String[]{"gcc", "output.obj", "-o", "output.exe"};
                execCmd = new String[]{"cmd", "/c", "output.exe"};
            } else {
                // Linux / WSL commands
                nasmCmd = new String[]{"nasm", "-f", "elf64", "output.asm"};
                linkCmd = new String[]{"ld", "output.o", "-o", "output"};
                execCmd = new String[]{"./output"};
            }

            // Execute NASM
            int nasmStatus = executeCommand(nasmCmd, false);
            if (nasmStatus != 0) {
                System.err.println("\n[!] NASM Assembly Failed! Ensure NASM is installed and in your PATH.");
                return;
            }

            // Execute Linker
            int linkStatus = executeCommand(linkCmd, false);
            if (linkStatus != 0) {
                System.err.println("\n[!] Linking Failed! Ensure GCC/LD is installed and in your PATH.");
                return;
            }

            // Execute Program
            System.out.println("[FINAL OUTPUT]:");
            int execStatus = executeCommand(execCmd, true);

            if (execStatus == 0) {
                System.out.println("\n[+] Compilation Pipeline Completed Successfully!");
            } else {
                System.out.println("\n[-] Program executed, but returned non-zero exit code: " + execStatus);
            }

        } catch (RuntimeException e) {
            System.err.println("\n[!] Compilation Error:");
            System.err.println(e.getMessage());
            System.err.println("Compilation aborted.");
        } catch (Exception e) {
            System.err.println("\n[!] An unexpected error occurred:");
            e.printStackTrace();
            System.err.println("Compilation aborted.");
        }
    }

    /**
     * Executes a system command using ProcessBuilder, safely captures output/errors,
     * and waits for it to complete.
     */
    private static int executeCommand(String[] command, boolean displayOutput) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true); // merge stdout and stderr
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (displayOutput) {
                        System.out.println(line);
                    } else {
                        // Print assembly/linker debug messages silently to err 
                        System.err.println("  " + line);
                    }
                }
            }
            
            // Wait for process to fully terminate
            process.waitFor();
            return process.exitValue();

        } catch (IOException | InterruptedException e) {
            // Note: Not throwing generic exceptions higher so execution fails gracefully handled inline
            System.err.println("Command failed to start/execute: " + String.join(" ", command));
            return -1;
        }
    }
}
