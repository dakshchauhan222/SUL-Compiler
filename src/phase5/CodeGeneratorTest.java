package phase5;

import phase3.*;
import java.util.Arrays;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

public class CodeGeneratorTest {
    public static void main(String[] args) {
        System.out.println("Testing Code Generator");

        // AST for:
        // x = 5;
        // y = 10;
        // while (x < y) { 
        //     print x;
        //     x = x + 1;
        // }
        // if (x == 10) { print 999; } else { print 0; }
        
        // Statements
        StatementNode s1 = new AssignmentNode("x", new NumberNode(5));
        StatementNode s2 = new AssignmentNode("y", new NumberNode(10));
        
        // While condition: x < y
        ExpressionNode whileCond = new BinaryOpNode("<", new IdentifierNode("x"), new IdentifierNode("y"));
        
        // While body
        StatementNode printX = new PrintNode(new IdentifierNode("x"));
        StatementNode incX = new AssignmentNode("x", new BinaryOpNode("+", new IdentifierNode("x"), new NumberNode(1)));
        BlockNode whileBody = new BlockNode(Arrays.asList(printX, incX));
        
        StatementNode s3 = new WhileNode(whileCond, whileBody);

        // If condition: x == 10
        ExpressionNode ifCond = new BinaryOpNode("==", new IdentifierNode("x"), new NumberNode(10));
        BlockNode thenBlock = new BlockNode(Arrays.asList(new PrintNode(new NumberNode(999))));
        BlockNode elseBlock = new BlockNode(Arrays.asList(new PrintNode(new NumberNode(0))));
        
        StatementNode s4 = new IfNode(ifCond, thenBlock, elseBlock);

        // Program
        List<StatementNode> statements = Arrays.asList(s1, s2, s3, s4);
        ProgramNode programNode = new ProgramNode(statements);
        
        CodeGenerator generator = new CodeGenerator();
        String assembly = generator.generate(programNode);
        
        System.out.println("--- Generated Assembly ---");
        System.out.println(assembly);
        
        try (FileWriter writer = new FileWriter("output.asm")) {
            writer.write(assembly);
            System.out.println("Wrote assembly to output.asm");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
