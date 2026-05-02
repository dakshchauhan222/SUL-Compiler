#  SUL Compiler: The Ultimate GenZ Language

[![Java Version](https://img.shields.io/badge/Java-11%2B-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Peak-brightgreen.svg)]()

**SUL (Simple Utility Language)** has evolved into the ultimate GenZ-native, unstructured, and bracket-free programming language. It strips away the rigid boilerplate of traditional languages and replaces it with a stream-of-consciousness syntax that compiles directly to optimized **x86-64 Assembly**.

---

##  The "Peak" GenZ Syntax
Forget semicolons and curly braces. SUL uses indentation and pure vibes to define code blocks.

| Standard Operation | GenZ Slang | Purpose |
| :--- | :--- | :--- |
| `Program Start` | `its giving :` | The entry point vibe |
| `Program End` | `peace out` | Wrapping it up |
| `Declaration` | `aura` | Defining a variable |
| `Assignment` | `drop` | Assigning a value (=) |
| `Print` | `yap` | Sending it to the screen |
| `Terminator` | `fr` | Optional statement end |
| `If` | `sus` | Looking at the condition |
| `Else` | `no cap :` | The fallback plan |
| `Error Sound` | `Faaahhhh` | Audio feedback for glitches |
| `Success Sound` | `Oh My God Wow` | Audio feedback for peak code |
| `Greater Than` | `clears` | Comparing vibes (>) |
| `Less Than` | `flops` | Comparing vibes (<) |
| `Equal To` | `hits same` | Identical energy (==) |
| `Addition` | `gained` | Increasing the stack (+) |
| `Subtraction` | `lost` | Decreasing the stack (-) |

---

##  Architecture: The Pipeline
The SUL Compiler implements a professional five-stage pipeline:

1. **Lexical Analysis (Lexer)**: Indentation-aware tokenization that turns slangs into logic.
2. **Parsing (Parser)**: An **LL(1) Recursive Descent Parser** representing "unstructured" code in a structured AST.
3. **Semantic Analysis**: Ensures your variables have the right energy (type checking).
4. **Code Generation**: Translates AST nodes into **NASM-compatible x86-64 Assembly**.
5. **Native Execution**: Invokes `nasm` and `gcc`/`ld` to produce a runnable binary.

---

##  Getting Started

### Prerequisites
- **Java 11 or higher**
- **NASM** (Optional, for native binary generation)
- **GCC** (Optional, for linking)

### Installation & Run
1. Clone the repo and compile:
   ```bash
   javac -d bin src/**/*.java
   ```
2. Run the interactive CLI:
   ```bash
   java -cp bin MainDriver
   ```

---

## Example Script (`test.sul`)
Witness the state-of-the-art unstructured syntax:

```python
its giving :
    aura marks drop 85
    aura passing drop 40

    yap "Student Grade Checker fr"

    sus marks clears passing :
        yap "Distinction no cap 🔥"
    no cap :
        yap "mid performance ngl"

    aura bonus drop marks gained 5
    yap bonus fr
peace out
```

---

## Project Structure
```text
SUL-compiler/
├── src/                # Java Source Code
│   ├── phase2/         # Indentation-aware Lexer
│   ├── phase3/         # Unstructured Parser & AST
│   ├── phase4/         # Semantic Analysis (Vibe Check)
│   ├── phase5/         # x86-64 Code Generator
│   └── MainDriver.java # Pipeline Runner
├── test.sul            # Sample GenZ Script
└── output.asm          # Generated Assembly
```

---

## Contributing
Contributions are lowkey welcome! If you have better slangs, open a PR.

**Stay Slay.** 
