# 🚀 SUL Compiler: Simple Utility Language

[![Java Version](https://img.shields.io/badge/Java-11%2B-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Success-brightgreen.svg)]()

**SUL (Simple Utility Language)** is a powerful, multi-phase compiler designed to bridge the gap between high-level logic and native machine code. It translates a custom language—blending C-style syntax with Java-like structure—into optimized **x86-64 Assembly**, providing a deep-dive into the internals of compiler design.

---

## 💅 The GenZ "No-Cap" Mode
SUL features a unique **GenZ Slang mapping**, allowing you to write code that actually speaks your language. Forget boring keywords; embrace the aura.

| Standard Keyword | SUL "GenZ" Slang | Purpose |
| :--- | :--- | :--- |
| `class` | `lowkey` | Class/Module entry |
| `int` | `aura` | 64-bit Integer type |
| `main()` | `main_character()` | Program Entry Point |
| `if` | `sus` | Conditional check |
| `else` | `cap` | Alternative branch |
| `while` | `bruh` | Iterative loop |
| `print` | `yap` | Standard output |
| `return` | `slay` | Exit / Return value |

---

## 🛠️ Architecture: The Pipeline
The SUL Compiler implements a classic five-stage pipeline to ensure robust code generation:

1. **Lexical Analysis (Lexer)**: Tokenizes source code using regex and custom finite state logic.
2. **Parsing (Parser)**: An **LL(1) Recursive Descent Parser** that constructs a typed Abstract Syntax Tree (AST).
3. **Semantic Analysis**: Performs type checking and ensures variable scoping/declarations are valid.
4. **Code Generation**: Translates AST nodes into **NASM-compatible x86-64 Assembly**.
5. **Native Execution**: Automatically invokes `nasm` and `gcc`/`ld` to produce a runnable binary.

---

## 🚀 Getting Started

### Prerequisites
To run the SUL compiler and execute the generated code, you need:
- **Java 11 or higher**
- **NASM** (The Netwide Assembler)
- **GCC** (or `ld` for linking)

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/SUL-compiler.git
   cd SUL-compiler
   ```
2. Compile the Java source:
   ```bash
   javac -d bin src/**/*.java
   ```

### Running the Compiler
You can run the compiler in interactive mode or through the web interface.

**Interactive CLI:**
```bash
java -cp bin MainDriver
```

**Web Interface:**
```bash
java -cp bin CompilerServer
```
*Then navigate to `http://localhost:8080` in your browser.*

---

## 📝 Example Script (`test.sul`)
Witness the majesty of a SUL program:

```c
lowkey Main {
    aura main_character() {
        aura x = 10;
        aura y = 3;
        
        sus (x > y) {
            yap(x);
        } cap {
            yap(y);
        }
        
        aura count = 5;
        bruh (count > 0) {
            yap(count);
            count = count - 1;
        }

        slay 0;
    }
}
```

---

## 📂 Project Structure
```text
SUL-compiler/
├── src/                # Java Source Code
│   ├── phase2/         # Lexer & Tokens
│   ├── phase3/         # Parser & AST
│   ├── phase4/         # Semantic Analyzer
│   ├── phase5/         # Code Generator (x86-64)
│   └── MainDriver.java # Entry Point
├── public/             # Web Frontend Assets
├── test.sul            # Sample SUL Program
└── output.asm          # Generated Assembly (Build byproduct)
```

---

## 🤝 Contributing
Contributions are lowkey welcome! If you find a bug or want to suggest a new slang keyword, feel free to open a PR.

**Stay Slay.** 💅
