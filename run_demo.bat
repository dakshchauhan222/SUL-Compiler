@echo off
echo ========================================================
echo               COMPILING SUL COMPILER                    
echo ========================================================
echo Compiling Phase 2 (Lexer)...
javac -d out src\phase2\*.java
echo Compiling Phase 3 (Parser)...
javac -cp out -d out src\phase3\*.java
echo Compiling Phase 4 (Semantic Analyzer)...
javac -cp out -d out src\phase4\*.java
echo Compilation complete!
echo.
pause

cls
echo ========================================================
echo            PHASE 2 DEMONSTRATION: THE LEXER
echo ========================================================
echo Press any key to run the Lexer on the sample source code...
pause >nul
echo.
java -cp out phase2.LexerTest
echo.
echo Notice how the raw text was converted into individual Tokens.
echo.
pause

cls
echo ========================================================
echo            PHASE 3 DEMONSTRATION: THE PARSER
echo ========================================================
echo Press any key to feed the tokens into the Parser to build the AST...
pause >nul
echo.
java -cp out phase3.ParserTest
echo.
echo Notice how the flat tokens have been structured into a hierarchical tree,
echo respecting rules like operator precedence (e.g., multiplication before addition)
echo and nested blocks (if/while).
echo.
pause
cls
echo ========================================================
echo            PHASE 4 DEMONSTRATION: SEMANTIC ANALYSIS
echo ========================================================
echo Press any key to run Semantic Analysis to validate the AST...
pause >nul
echo.
java -cp out phase4.SemanticTest
echo.
echo Notice how the Analyzer catches undefined variables and enforces '{ }' block scopes!
echo.
pause
echo ========================================================
echo               DEMONSTRATION COMPLETE
echo ========================================================
