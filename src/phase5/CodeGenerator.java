package phase5;

import phase3.*;
import java.util.HashMap;
import java.util.Map;

/**
 * CodeGenerator translates an Abstract Syntax Tree (AST) into
 * x86-64 NASM (Intel syntax) assembly code with Educational format.
 */
public class CodeGenerator {

    private final StringBuilder asm;
    
    // Symbol table to track variables and assign them memory in the .bss section
    private final Map<String, String> symbolTable;
    private final Map<String, String> stringTable;
    
    // Counter to generate unique labels for control flow
    private int labelCounter;
    private int stringCounter;

    public CodeGenerator() {
        this.asm = new StringBuilder();
        this.symbolTable = new HashMap<>();
        this.stringTable = new HashMap<>();
        this.labelCounter = 1;
        this.stringCounter = 1;
    }

    private void emitHeader(String title) {
        asm.append("    ; --- [ INSTRUCTION: ").append(title).append(" ] ---\n");
    }

    private void emit(String instr, String comment) {
        if (comment == null || comment.isEmpty()) {
            asm.append("    ").append(instr).append("\n");
        } else {
            asm.append(String.format("    %-24s; %s\n", instr, comment));
        }
    }

    private void emitLine(String line) {
        asm.append(line).append("\n");
    }

    public String generate(ProgramNode program) {
        StringBuilder textSection = new StringBuilder();
        StringBuilder bssSection = new StringBuilder();

        bssSection.append("section .bss\n");
        
        textSection.append("section .text\n");
        textSection.append("global _start\n\n");
        textSection.append("_start:\n");

        generateProgram(program);

        if (!symbolTable.isEmpty()) {
            for (String varName : symbolTable.keySet()) {
                bssSection.append("    ").append(varName).append(" resq 1\n");
            }
            bssSection.append("\n");
        }

        StringBuilder dataSection = new StringBuilder();
        if (!stringTable.isEmpty()) {
            dataSection.append("section .data\n");
            for (Map.Entry<String, String> entry : stringTable.entrySet()) {
                dataSection.append("    ").append(entry.getKey()).append(" db `").append(entry.getValue().replace("`", "")).append("`, 10\n");
            }
            dataSection.append("\n");
        }

        return dataSection.toString() + bssSection.toString() + textSection.toString() + asm.toString() + generateExitSyscall() + generatePrintSubroutine();
    }

    private void generateProgram(ProgramNode node) {
        for (StatementNode stmt : node.getStatements()) {
            generateStatement(stmt);
        }
    }

    private void generateStatement(StatementNode node) {
        if (node instanceof AssignmentNode) {
            generateAssignment((AssignmentNode) node);
        } else if (node instanceof PrintNode) {
            generatePrint((PrintNode) node);
        } else if (node instanceof IfNode) {
            generateIf((IfNode) node);
        } else if (node instanceof WhileNode) {
            generateWhile((WhileNode) node);
        } else if (node instanceof BlockNode) {
            for (StatementNode stmt : ((BlockNode) node).getStatements()) {
                generateStatement(stmt);
            }
        } else {
            throw new UnsupportedOperationException("Unsupported statement type: " + node.getClass().getSimpleName());
        }
    }

    private void generateAssignment(AssignmentNode node) {
        String varName = node.getName();
        
        if (!symbolTable.containsKey(varName)) {
            symbolTable.put(varName, varName);
        }

        emitHeader("Initializing Variable '" + varName + "'");
        generateExpression(node.getExpression());

        emit("mov qword [rel " + varName + "], rax", "MEMORY: Write the number from 'rax' permanently into the 64-bit memory lane for '" + varName + "'");
        emitLine("");
    }

    private void generatePrint(PrintNode node) {
        if (node.getExpression() instanceof StringNode) {
            String value = ((StringNode) node.getExpression()).getValue();
            String label = "str_" + stringCounter++;
            stringTable.put(label, value);
            
            emitHeader("Printing String");
            emit("mov rax, 1", "KERNEL: Alert the system we want to write data (sys_write)");
            emit("mov rdi, 1", "KERNEL: Point the output stream to the standard computer screen (stdout)");
            emit("lea rsi, [rel " + label + "]", "CPU: Load the exact memory address where our custom string lives");
            emit("mov rdx, " + (value.length() + 1), "CPU: Tell the system exactly how many characters long the text is");
            emit("syscall", "EXECUTE: Tell Windows to physically print the text to the monitor");
            emitLine("");
        } else {
            emitHeader("Printing Integer");
            generateExpression(node.getExpression());
            emit("mov rdi, rax", "CPU: Move the evaluated number into 'rdi' to prepare for printing");
            emit("call print_int", "EXECUTE: Process the number physically to the screen via standard subroutine");
            emitLine("");
        }
    }

    private void generateIf(IfNode node) {
        int labelId = labelCounter++;
        String elseLabel = "L_else_" + labelId;
        String endLabel = "L_endIf_" + labelId;

        emitHeader("Evaluating If Condition");
        generateExpression(node.getCondition());

        emit("cmp rax, 0", "LOGIC: Compare the result of our condition against zero (false)");
        if (node.getElseBlock() != null) {
            emit("je " + elseLabel, "FLOW: If it is completely false, jump directly to the 'else' fallback block");
            emitLine("");
        } else {
            emit("je " + endLabel, "FLOW: If it is false, skip the if-block entirely and jump to the end");
            emitLine("");
        }

        emitHeader("Entering 'Then' Block");
        generateStatement(node.getThenBlock());

        if (node.getElseBlock() != null) {
            emit("jmp " + endLabel, "FLOW: We finished the 'then' block! Jump over the 'else' block so we don't accidentally run it");
            emitLine("");
            emitLine(elseLabel + ":");
            emitHeader("Entering 'Else' Block");
            generateStatement(node.getElseBlock());
        }

        emitLine(endLabel + ":");
        emitLine("");
    }

    private void generateWhile(WhileNode node) {
        int labelId = labelCounter++;
        String startLabel = "L_whileStart_" + labelId;
        String endLabel = "L_whileEnd_" + labelId;

        emitLine(startLabel + ":");
        emitHeader("Evaluating While Loop Condition");
        
        generateExpression(node.getCondition());
        
        emit("cmp rax, 0", "LOGIC: Check if our looping condition is zero (meaning 'false' or finished)");
        emit("je " + endLabel, "FLOW: If the condition is false, break out of the loop permanently");
        emitLine("");

        emitHeader("Executing While Loop Body");
        generateStatement(node.getBody());

        emit("jmp " + startLabel, "FLOW: Re-start the loop from the top!");
        emitLine(endLabel + ":");
        emitLine("");
    }

    private void generateExpression(ExpressionNode node) {
        if (node instanceof NumberNode) {
            int value = ((NumberNode) node).getValue();
            emit("mov rax, " + value, "CPU: Grab the raw number '" + value + "' and hold it in the 'rax' processing register");
        } else if (node instanceof IdentifierNode) {
            String varName = ((IdentifierNode) node).getName();
            emit("mov rax, [rel " + varName + "]", "CPU: Retrieve the current value mapped to '" + varName + "' from memory");
        } else if (node instanceof BinaryOpNode) {
            BinaryOpNode binNode = (BinaryOpNode) node;
            
            generateExpression(binNode.getLeft());
            emit("push rax", "STACK: Save our left-side calculation securely into temporary memory");
            
            generateExpression(binNode.getRight());
            
            emit("mov rbx, rax", "CPU: Move the right-side calculation into 'rbx' so we can compare the two");
            emit("pop rax", "STACK: Restore the left-side calculation back into 'rax'");
            
            switch (binNode.getOperator()) {
                case "+":
                case "gained":
                    emit("add rax, rbx", "MATH: Add both processing units together");
                    break;
                case "-":
                case "lost":
                    emit("sub rax, rbx", "MATH: Subtract the right side from the left side");
                    break;
                case "*":
                    emit("imul rax, rbx", "MATH: Multiply both values against each other");
                    break;
                case "/":
                    emit("cqo", "MATH: Stretch our positive/negative sign safely across the integer");
                    emit("idiv rbx", "MATH: Divide the left side by the right side securely");
                    break;
                case "==":
                case "hits same":
                case "!=":
                case "<":
                case "flops":
                case "<=":
                case ">":
                case "clears":
                case ">=":
                    generateRelationalOp(binNode.getOperator());
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported binary operator: " + binNode.getOperator());
            }
        } else {
            throw new UnsupportedOperationException("Unsupported expression type: " + node.getClass().getSimpleName());
        }
    }

    private void generateRelationalOp(String operator) {
        emit("cmp rax, rbx", "LOGIC: Directly contrast the two values against each other");
        switch (operator) {
            case "==":
            case "hits same": emit("sete al", "LOGIC: If they are strictly EQUAL, flag memory to '1'"); break;
            case "!=": emit("setne al", "LOGIC: If they are exactly NOT EQUAL, flag memory to '1'"); break;
            case "<":
            case "flops":  emit("setl al", "LOGIC: If the left is LESS, flag memory to '1'"); break;
            case "<=": emit("setle al", "LOGIC: If the left is LESS/EQUAL, flag memory to '1'"); break;
            case ">":
            case "clears":  emit("setg al", "LOGIC: If the left is GREATER, flag memory to '1'"); break;
            case ">=": emit("setge al", "LOGIC: If the left is GREATER/EQUAL, flag memory to '1'"); break;
        }
        emit("movzx rax, al", "CPU: Zero-extend the boolean 1 or 0 safely into the 64-bit 'rax' register");
    }

    private String generateExitSyscall() {
        return "    ; --- [ INSTRUCTION: Program Exit ] ---\n" +
               "    mov rax, 60             ; KERNEL: Prepare sequence 60 signifying end-of-program\n" +
               "    xor rdi, rdi            ; EXECUTE: Clean memory slate to 0 (all clear code)\n" +
               "    syscall                 ; EXECUTE: Terminate app and return control to OS\n\n";
    }

    private String generatePrintSubroutine() {
        return ";------------------------------------------\n" +
               "; Function: print_int\n" +
               "; Input: RDI = integer to print\n" +
               ";------------------------------------------\n" +
               "print_int:\n" +
               "    push rbp\n" +
               "    mov rbp, rsp\n" +
               "    sub rsp, 32         ; Reserve space on stack for string (20 digits max + newline)\n" +
               "    \n" +
               "    mov rax, rdi        ; Number to convert\n" +
               "    mov rcx, 10         ; Divisor\n" +
               "    lea rdi, [rbp-2]    ; Start writing from end of buffer, leave space for newline\n" +
               "    mov byte [rbp-1], 10; '\\n'\n" +
               "    \n" +
               "    ; Handle negative numbers\n" +
               "    mov r8, 0           ; Flag for negative\n" +
               "    test rax, rax\n" +
               "    jns .convert_loop\n" +
               "    neg rax             ; Make positive\n" +
               "    mov r8, 1           ; Set negative flag\n" +
               "    \n" +
               ".convert_loop:\n" +
               "    xor rdx, rdx\n" +
               "    div rcx             ; RDX:RAX / 10. RAX = quotient, RDX = remainder\n" +
               "    add dl, '0'         ; Convert remainder to ASCII\n" +
               "    mov [rdi], dl       ; Store character\n" +
               "    dec rdi             ; Move pointer backwards\n" +
               "    test rax, rax\n" +
               "    jnz .convert_loop   ; Continue if quotient != 0\n" +
               "    \n" +
               "    ; Add '-' if negative\n" +
               "    test r8, r8\n" +
               "    jz .print_str\n" +
               "    mov byte [rdi], '-'\n" +
               "    dec rdi\n" +
               "\n" +
               ".print_str:\n" +
               "    ; Calculate length\n" +
               "    inc rdi                 ; Point to first character\n" +
               "    lea rsi, [rbp]\n" +
               "    sub rsi, rdi            ; RSI = length = end - start\n" +
               "    \n" +
               "    ; Syscall write\n" +
               "    mov rax, 1              ; sys_write\n" +
               "    mov rdx, rsi            ; Length\n" +
               "    mov rsi, rdi            ; Buffer\n" +
               "    mov rdi, 1              ; stdout\n" +
               "    syscall\n" +
               "    \n" +
               "    leave\n" +
               "    ret\n";
    }
}
