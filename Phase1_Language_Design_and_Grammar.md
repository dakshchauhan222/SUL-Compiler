# Phase 1: Language Design and Grammar Definition

## SUL — Simple Utility Language

**Project Title:** Code Generator from a Custom Language (SUL) to x86-64 Assembly  
**Phase:** 1 — Language Design and Grammar Definition  
**Objective:** Define the SUL language syntax, specify all tokens, and derive an LL(1) grammar suitable for recursive descent parsing.

---

## 1. Language Overview

**SUL (Simple Utility Language)** is a minimal imperative programming language designed as the source language for a multi-phase compiler targeting x86-64 assembly. It supports:

| Feature               | Description                                    |
| --------------------- | ---------------------------------------------- |
| Data type             | `int` only (implicit declaration on first use) |
| Assignment            | `x = expression;`                              |
| Arithmetic            | `+`, `-`, `*`, `/`                             |
| Relational operators  | `>`, `<`, `==`, `!=`                           |
| Conditionals          | `if` and `if-else`                             |
| Loops                 | `while`                                        |
| Blocks                | Curly braces `{ … }`                           |
| Output                | `print(expression);`                           |

All variables hold 64-bit signed integer values. A variable is declared implicitly the first time it appears on the left-hand side of an assignment.

---

## 2. Sample SUL Program

The following program demonstrates every language feature:

```sul
// Arithmetic and assignment
x = 10;
y = 3;
sum = x + y;
diff = x - y;
prod = x * y;
quot = x / y;

// Print results
print(sum);
print(diff);
print(prod);
print(quot);

// Relational operators and if-else
if (x > y) {
    max = x;
} else {
    max = y;
}
print(max);

// Equality / inequality checks
if (x == y) {
    print(1);
}

if (x != y) {
    print(0);
}

// While loop — compute factorial of 5
n = 5;
fact = 1;
while (n > 0) {
    fact = fact * n;
    n = n - 1;
}
print(fact);
```

---

## 3. Token Definitions

Every lexeme in SUL falls into exactly one of the following token categories.

### 3.1 Keywords

| Lexeme   | Token Name |
| -------- | ---------- |
| `if`     | `IF`       |
| `else`   | `ELSE`     |
| `while`  | `WHILE`    |
| `print`  | `PRINT`    |

### 3.2 Operators

| Lexeme | Token Name | Category   |
| ------ | ---------- | ---------- |
| `+`    | `PLUS`     | Arithmetic |
| `-`    | `MINUS`    | Arithmetic |
| `*`    | `STAR`     | Arithmetic |
| `/`    | `SLASH`    | Arithmetic |
| `=`    | `ASSIGN`   | Assignment |
| `>`    | `GT`       | Relational |
| `<`    | `LT`       | Relational |
| `==`   | `EQ`       | Relational |
| `!=`   | `NEQ`      | Relational |

### 3.3 Symbols (Delimiters)

| Lexeme | Token Name |
| ------ | ---------- |
| `(`    | `LPAREN`   |
| `)`    | `RPAREN`   |
| `{`    | `LBRACE`   |
| `}`    | `RBRACE`   |
| `;`    | `SEMI`     |

### 3.4 Identifiers

```
IDENTIFIER  →  letter ( letter | digit )*
letter      →  'a'-'z' | 'A'-'Z' | '_'
digit       →  '0'-'9'
```

An identifier must not collide with a keyword.

### 3.5 Integer Literals

```
NUMBER  →  digit+
digit   →  '0'-'9'
```

Negative numbers are expressed as unary minus applied to a literal (e.g., `0 - 5`).

### 3.6 Special Tokens

| Token Name | Description                   |
| ---------- | ----------------------------- |
| `EOF`      | End of input                  |
| (comments) | `//` to end-of-line (skipped) |

---

## 4. Grammar — Stage A: Initial Readable Grammar

Below is a natural, readable grammar that mirrors the language structure directly. At this stage we deliberately allow left recursion and ambiguity to keep things intuitive.

```
Program         →  StmtList

StmtList        →  Stmt StmtList
                |   ε

Stmt            →  AssignStmt
                |   PrintStmt
                |   IfStmt
                |   WhileStmt
                |   Block

AssignStmt      →  IDENTIFIER '=' Expr ';'

PrintStmt       →  'print' '(' Expr ')' ';'

IfStmt          →  'if' '(' Condition ')' Block
                |   'if' '(' Condition ')' Block 'else' Block

WhileStmt       →  'while' '(' Condition ')' Block

Block           →  '{' StmtList '}'

Condition       →  Expr RelOp Expr

RelOp           →  '>'  |  '<'  |  '=='  |  '!='

Expr            →  Expr '+' Term              ← left recursive
                |   Expr '-' Term              ← left recursive
                |   Term

Term            →  Term '*' Factor             ← left recursive
                |   Term '/' Factor             ← left recursive
                |   Factor

Factor          →  NUMBER
                |   IDENTIFIER
                |   '(' Expr ')'
```

### Problems with this grammar

1. **Left recursion** in `Expr` and `Term` — a recursive descent parser would enter infinite recursion.
2. **Ambiguity** in `IfStmt` — two alternatives begin with the same prefix (`if ( Condition ) Block`), so the parser cannot decide which production to use by looking at only one token ahead.

---

## 5. Grammar — Stage B: LL(1) Grammar

### 5.1 Why Left Recursion Is a Problem

In a recursive descent parser each non-terminal is implemented as a function that calls itself. Consider:

```
Expr  →  Expr '+' Term  |  Term
```

The function for `Expr` would immediately call `Expr` again *without consuming any input*, leading to **infinite recursion** and a stack overflow. Left recursion must therefore be eliminated before the grammar can drive a recursive descent parser.

### 5.2 Removing Left Recursion

The standard transformation replaces a left-recursive rule of the form:

```
A  →  A α  |  β
```

with the equivalent pair:

```
A   →  β A'
A'  →  α A'  |  ε
```

**Applying this to `Expr`:**

Original:
```
Expr  →  Expr '+' Term  |  Expr '-' Term  |  Term
```

Transformed:
```
Expr   →  Term Expr'
Expr'  →  '+' Term Expr'
       |   '-' Term Expr'
       |   ε
```

**Applying this to `Term`:**

Original:
```
Term  →  Term '*' Factor  |  Term '/' Factor  |  Factor
```

Transformed:
```
Term   →  Factor Term'
Term'  →  '*' Factor Term'
       |   '/' Factor Term'
       |   ε
```

### 5.3 Resolving the `IfStmt` Ambiguity (Left Factoring)

The original `IfStmt` has two alternatives sharing the prefix `if ( Condition ) Block`:

```
IfStmt  →  'if' '(' Condition ')' Block
        |   'if' '(' Condition ')' Block 'else' Block
```

**Left factoring** extracts the common prefix:

```
IfStmt    →  'if' '(' Condition ')' Block ElsePart
ElsePart  →  'else' Block
          |   ε
```

Now the parser can parse the common prefix first and then decide based on whether the next token is `else`.

### 5.4 Final LL(1) Grammar

```
Program     →  StmtList EOF

StmtList    →  Stmt StmtList
            |   ε

Stmt        →  IDENTIFIER '=' Expr ';'
            |   'print' '(' Expr ')' ';'
            |   'if' '(' Condition ')' Block ElsePart
            |   'while' '(' Condition ')' Block
            |   Block

ElsePart    →  'else' Block
            |   ε

Block       →  '{' StmtList '}'

Condition   →  Expr RelOp Expr

RelOp       →  '>'  |  '<'  |  '=='  |  '!='

Expr        →  Term Expr'

Expr'       →  '+' Term Expr'
            |   '-' Term Expr'
            |   ε

Term        →  Factor Term'

Term'       →  '*' Factor Term'
            |   '/' Factor Term'
            |   ε

Factor      →  NUMBER
            |   IDENTIFIER
            |   '(' Expr ')'
```

---

## 6. LL(1) Suitability Analysis

An LL(1) grammar must satisfy two conditions for every non-terminal with multiple alternatives:

1. **No two alternatives can begin with the same terminal** (disjoint FIRST sets).
2. **If one alternative can derive ε, the FIRST set of the other alternatives must be disjoint from the FOLLOW set** of the non-terminal.

### Verification by inspection

| Non-terminal | Alternatives begin with              | Disjoint? |
| ------------ | ------------------------------------ | --------- |
| `StmtList`   | `IDENTIFIER`, `print`, `if`, `while`, `{` vs. ε (followed by `}` or `EOF`) | ✔ Yes |
| `Stmt`       | `IDENTIFIER` / `print` / `if` / `while` / `{` | ✔ Yes |
| `ElsePart`   | `else` vs. ε (followed by next stmt or `}`) | ✔ Yes |
| `Expr'`      | `+` / `-` vs. ε (followed by `)` or `;` or `RelOp`) | ✔ Yes |
| `Term'`      | `*` / `/` vs. ε (followed by `+`, `-`, `)`, `;`, `RelOp`) | ✔ Yes |
| `Factor`     | `NUMBER` / `IDENTIFIER` / `(`       | ✔ Yes |
| `RelOp`      | `>` / `<` / `==` / `!=`             | ✔ Yes |

Every non-terminal's alternatives can be distinguished by examining exactly **one** lookahead token. The grammar is therefore **LL(1)** and can be directly implemented with a recursive descent parser.

> **Note:** A formal FIRST/FOLLOW table is *not* computed here because the disjointness is evident from inspection. The table can be constructed in a later phase if required.

---

## 7. Summary

| Deliverable              | Status     |
| ------------------------ | ---------- |
| Language specification   | ✅ Complete |
| Sample SUL program       | ✅ Complete |
| Token definitions        | ✅ Complete |
| Initial readable grammar | ✅ Complete |
| Left recursion removal   | ✅ Complete |
| Left factoring           | ✅ Complete |
| Final LL(1) grammar      | ✅ Complete |
| LL(1) justification      | ✅ Complete |

**Next phase (Phase 2):** Implement the lexer in Java to tokenise SUL source files according to the token definitions above.
