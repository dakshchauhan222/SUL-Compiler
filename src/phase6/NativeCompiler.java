package phase6;

import phase3.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class NativeCompiler {
    private Map<String, Integer> env = new HashMap<>();
    private StringBuilder fallbackOutput = new StringBuilder();

    public String execute(ProgramNode root) {
        try {
            boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
            
            String[] nasmCmd = isWindows ? new String[]{"nasm", "-f", "win64", "output.asm"} : new String[]{"nasm", "-f", "elf64", "output.asm"};
            String[] linkCmd = isWindows ? new String[]{"gcc", "output.obj", "-o", "output.exe"} : new String[]{"ld", "output.o", "-o", "output"};
            String[] execCmd = isWindows ? new String[]{"cmd", "/c", "output.exe"} : new String[]{"./output"};

            // Execute NASM natively
            if (runProcessSilent(nasmCmd) != 0) {
                return executeFallback(root);
            }

            // Execute GCC Linker
            if (runProcessSilent(linkCmd) != 0) {
                return executeFallback(root);
            }

            // Execute Native Application
            return runProcessAndCapture(execCmd);

        } catch (java.io.IOException processError) {
            // Thrown if "nasm" or "gcc" is missing from PATH
            return executeFallback(root);
        } catch (Exception e) {
            return "Runtime Error: " + e.getMessage();
        }
    }

    private String executeFallback(ProgramNode root) {
        env.clear();
        fallbackOutput.setLength(0);
        try {
            for (StatementNode stmt : root.getStatements()) {
                executeStatement(stmt);
            }
            String result = fallbackOutput.toString().trim();
            return result.isEmpty() ? "(No output printed by program)" : result;
        } catch (Exception e) {
            return "Fallback Runtime Error: " + e.getMessage();
        }
    }

    private void executeStatement(StatementNode stmt) {
        if (stmt instanceof AssignmentNode) {
            AssignmentNode node = (AssignmentNode) stmt;
            env.put(node.getName(), evaluateExpression(node.getExpression()));
        } else if (stmt instanceof PrintNode) {
            PrintNode node = (PrintNode) stmt;
            if (node.getExpression() instanceof StringNode) {
                fallbackOutput.append(((StringNode) node.getExpression()).getValue()).append("\n");
            } else {
                fallbackOutput.append(evaluateExpression(node.getExpression())).append("\n");
            }
        } else if (stmt instanceof IfNode) {
            IfNode node = (IfNode) stmt;
            if (evaluateExpression(node.getCondition()) != 0) {
                executeStatement(node.getThenBlock());
            } else if (node.getElseBlock() != null) {
                executeStatement(node.getElseBlock());
            }
        } else if (stmt instanceof WhileNode) {
            WhileNode node = (WhileNode) stmt;
            while (evaluateExpression(node.getCondition()) != 0) {
                executeStatement(node.getBody());
            }
        } else if (stmt instanceof BlockNode) {
            for (StatementNode s : ((BlockNode) stmt).getStatements()) {
                executeStatement(s);
            }
        }
    }

    private int evaluateExpression(ExpressionNode expr) {
        if (expr instanceof NumberNode) {
            return ((NumberNode) expr).getValue();
        } else if (expr instanceof IdentifierNode) {
            return env.getOrDefault(((IdentifierNode) expr).getName(), 0);
        } else if (expr instanceof BinaryOpNode) {
            BinaryOpNode node = (BinaryOpNode) expr;
            int left = evaluateExpression(node.getLeft());
            int right = evaluateExpression(node.getRight());
            switch (node.getOperator()) {
                case "+": return left + right;
                case "-": return left - right;
                case "*": return left * right;
                case "/": 
                    if (right == 0) throw new ArithmeticException("Division by zero");
                    return left / right;
                case "==": return left == right ? 1 : 0;
                case "!=": return left != right ? 1 : 0;
                case "<":  return left < right ? 1 : 0;
                case ">":  return left > right ? 1 : 0;
                case "<=": return left <= right ? 1 : 0;
                case ">=": return left >= right ? 1 : 0;
                default: return 0;
            }
        } else if (expr instanceof UnaryOpNode) {
            UnaryOpNode node = (UnaryOpNode) expr;
            int val = evaluateExpression(node.getOperand());
            if ("-".equals(node.getOperator())) {
                return -val;
            }
            return val;
        }
        return 0;
    }

    private int runProcessSilent(String[] command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        process.waitFor();
        return process.exitValue();
    }

    private String runProcessAndCapture(String[] command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        StringBuilder out = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line).append("\n");
            }
        }
        
        process.waitFor();
        String result = out.toString().trim();
        return result.isEmpty() ? "(No output printed by program)" : result;
    }
}
