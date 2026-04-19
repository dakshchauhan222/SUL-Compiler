# Implementation Plan: 100% GenZ SUL Compiler (Mixed C/Java Grammar)

## Goal Description
The objective is to redesign the SUL language grammar to visually represent a mix between **C** and **Java**, while integrating the requested **Gen Z slang keywords** for types, structural elements, loops, and returns. (Mathematical and equality operators remain standard like `+`, `-`, `==`).

## Proposed Language Design

### Feature Mapping:
**Keywords & Structure:**
- `class` -> `lowkey`
- `int` -> `aura`
- `main()` -> `main_character()`
- `return` -> `slay`
- `if` -> `sus`
- `else` -> `cap`
- `while` -> `bruh`
- `print` -> `yap`

### Example SUL Program (Mixed Grammar):
```c
// Java-style class wrapper
lowkey Main {
    
    // C-style entry point
    aura main_character() {
        
        // Exact C/Java explicit variable typing
        aura x = 10;
        aura y = 3;
        aura sum = x + y; 
        aura diff = x - y;
        
        // GenZ if-else blocks
        sus (x == y) { 
            yap(x); // GenZ print
        } cap {
            yap(sum);
        }
        
        // GenZ while loop
        aura count = 5;
        bruh (count > 0) {
            count = count - 1; 
        }

        slay 0; // C-style exit code
    }
}
```

## Required Changes to Compiler
If this plan is approved, the following files will eventually be modified:

### 1. Lexer (`Lexer.java` & `TokenType.java`)
- Add new tokens for `LOWKEY`, `AURA`, `MAIN_CHARACTER`, `SLAY`.
- Swap keywords `if`, `else`, `while`, `print` for `sus`, `cap`, `bruh`, `yap`.

### 2. Parser (`Parser.java`)
- Modify the root parse rule to expect `lowkey IDENTIFIER { ... }`.
- Modify statement parsing to expect an `aura main_character() { ... }` block inside the class.
- Add an explicit type declaration rule (`aura IDENTIFIER = EXPRESSION ;`).
- Identify standard assignments vs new declarations.
- Add the `slay` return rule (`slay EXPRESSION ;`).
- Update error messages to reflect the new structure.

### 3. Grammar Documentation (`Phase1_Language_Design_and_Grammar.md`)
- Completely rewrite the grammar rules to account for the C/Java wrapper overhead, explicit typing, and Gen Z slang list.

### 4. Tests and Examples
- Refactor `test.sul` to strictly follow this new syntax.
- Refactor all internal Java test strings (`LexerTest`, `ParserTest`, etc.).
