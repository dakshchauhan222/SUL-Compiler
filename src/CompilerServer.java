import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import phase2.Lexer;
import phase2.Token;
import phase3.Parser;
import phase3.ProgramNode;
import phase4.SemanticAnalyzer;
import phase5.CodeGenerator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class CompilerServer {

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new StaticFileHandler());
        server.createContext("/compile", new CompileHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("SUL Compiler Web Server started at http://localhost:" + port);
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            File file = new File("public" + path);
            if (!file.exists() || file.isDirectory()) {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }
            String contentType = "text/plain";
            if (path.endsWith(".html")) contentType = "text/html";
            else if (path.endsWith(".css")) contentType = "text/css";
            else if (path.endsWith(".js")) contentType = "application/javascript";
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, file.length());
            OutputStream os = exchange.getResponseBody();
            Files.copy(file.toPath(), os);
            os.close();
        }
    }

    static class CompileHandler implements HttpHandler {
        private static int consecutiveErrors = 0;

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            InputStream is = exchange.getRequestBody();
            String sourceCode = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            StringBuilder json = new StringBuilder();
            json.append("{\n  \"phases\": [\n");
            boolean success = true;

            try {
                // Phase 1: Lexical Analysis
                Lexer tokenLexer = new Lexer(sourceCode);
                List<Token> tokens = tokenLexer.tokenize();
                StringBuilder tokenStr = new StringBuilder();
                for (Token t : tokens) {
                    tokenStr.append(t.getType());
                    if (!t.getValue().isEmpty()) {
                        tokenStr.append("('").append(t.getValue().replace("\n", "\\n").replace("\r", "\\r")).append("')");
                    }
                    tokenStr.append(" ");
                }
                json.append("    { \"name\": \"Lexical Analysis\", \"status\": \"success\", \"output\": \"").append(escapeJson(tokenStr.toString().trim())).append("\" }");

                // Phase 2: Syntax Analysis (AST)
                Lexer lexer = new Lexer(sourceCode);
                Parser parser = new Parser(lexer);
                ProgramNode astRoot = parser.parse();
                String astStr = astRoot.toTreeString("");
                json.append(",\n    { \"name\": \"Syntax Analysis (AST)\", \"status\": \"success\", \"output\": \"").append(escapeJson(astStr)).append("\" }");

                // Phase 3: Semantic Analysis
                SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
                semanticAnalyzer.analyze(astRoot);
                json.append(",\n    { \"name\": \"Semantic Analysis\", \"status\": \"success\", \"output\": \"Validated successfully. 0 scope/variable errors.\" }");
                    
                    // Phase 4: Code Generation
                    CodeGenerator codeGenerator = new CodeGenerator();
                    String assembly = codeGenerator.generate(astRoot);
                    json.append(",\n    { \"name\": \"Code Generation (x86-64 NASM)\", \"status\": \"success\", \"output\": \"").append(escapeJson(assembly)).append("\" }");
                    
                    // Always write out assembly for the user's reference locally
                    try (java.io.FileWriter fileWriter = new java.io.FileWriter("output.asm")) {
                        fileWriter.write(assembly);
                    }
                    // Phase 6: Program Execution (Native Compilation Pipeline)
                    phase6.NativeCompiler runner = new phase6.NativeCompiler();
                    String finalOutput = runner.execute(astRoot);
                    json.append(",\n    { \"name\": \"Program Runtime Execution (Native Binary execution via NASM/GCC)\", \"status\": \"success\", \"output\": \"").append(escapeJson(finalOutput)).append("\" }\n");
                    
                    // Reset errors on success
                    consecutiveErrors = 0;
            } catch (RuntimeException e) {
                consecutiveErrors++;
                String msg = e.getMessage() != null ? e.getMessage() : "";
                msg = getMoodPrefix() + msg;
                
                String phaseName = "Lexical Analysis";
                if (msg.contains("Parse")) phaseName = "Syntax Analysis (AST)";
                else if (msg.contains("Semantic")) phaseName = "Semantic Analysis";
                
                json.append(",\n    { \"name\": \"").append(phaseName).append("\", \"status\": \"error\", \"output\": \"").append(escapeJson(msg)).append("\" }\n");
                success = false;
            } catch (Exception e) {
                json.append(",\n    { \"name\": \"Compilation Pipeline\", \"status\": \"error\", \"output\": \"Unexpected server error: ").append(escapeJson(e.getMessage())).append("\" }\n");
                success = false;
            }

            json.append("  ],\n  \"success\": ").append(success).append("\n}");
            sendJsonResponse(exchange, 200, json.toString());
        }

        private void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        }

        private String getMoodPrefix() {
            if (consecutiveErrors >= 5) {
                return "Bruh, we just talked about this 10 seconds ago! You STILL have an error. Please fix it so we can execute!\\n\\n";
            }
            java.time.LocalTime time = java.time.LocalTime.now();
            java.time.DayOfWeek day = java.time.LocalDate.now().getDayOfWeek();

            if (day == java.time.DayOfWeek.SATURDAY || day == java.time.DayOfWeek.SUNDAY) {
                return "It's literally the weekend, why are we compiling SUL code right now? Go outside! Anyway, ";
            }

            int hour = time.getHour();
            if (hour >= 0 && hour < 5) {
                int min = time.getMinute();
                return "Bruh it's " + (hour == 0 ? 12 : hour) + ":" + (min < 10 ? "0" + min : min) + " AM, your brain is completely fried right now. Go to sleep! (Also, ";
            } else if (hour >= 5 && hour < 9) {
                return "Wake up! You haven't even had coffee yet, which is probably why you have this error: ";
            }
            return "";
        }

        private String escapeJson(String text) {
            if (text == null) return "";
            return text.replace("\\", "\\\\")
                       .replace("\"", "\\\"")
                       .replace("\n", "\\n")
                       .replace("\r", "\\r")
                       .replace("\t", "\\t");
        }
    }
}
