document.addEventListener('DOMContentLoaded', () => {
    const codeEditor = document.getElementById('codeEditor');
    const lineNumbers = document.getElementById('lineNumbers');
    const runBtn = document.getElementById('runBtn');
    const clearBtn = document.getElementById('clearBtn');
    const themeToggleBtn = document.getElementById('themeToggleBtn');
    const outputDisplay = document.getElementById('outputDisplay');
    const loadingIndicator = document.getElementById('loadingIndicator');
    const highlightLayer = document.getElementById('highlightLayer');

    // Aura system references
    const cursorPosEl = document.getElementById('cursorPos');
    const auraScoreEl = document.getElementById('auraScore');
    const auraFill = document.getElementById('auraFill');
    const auraMeterBtn = document.getElementById('auraMeterBtn');
    const helpBtn = document.getElementById('helpBtn');
    let currentAura = 0;

    // Cursor position tracker
    const updateCursorPos = () => {
        const text = codeEditor.value;
        const cursorPos = codeEditor.selectionStart;
        const lines = text.substring(0, cursorPos).split('\n');
        const line = lines.length;
        const col = lines[lines.length - 1].length + 1;
        if (cursorPosEl) cursorPosEl.innerText = `Ln ${line}, Col ${col}`;
    };
    codeEditor.addEventListener('keyup', updateCursorPos);
    codeEditor.addEventListener('click', updateCursorPos);

    // Aura logic
    const updateAura = (amt) => {
        currentAura = Math.max(0, Math.min(100, currentAura + amt));
        if (auraScoreEl) auraScoreEl.innerText = currentAura;
        if (auraFill) auraFill.style.width = currentAura + '%';
        if (currentAura >= 90 && amt > 0) {
            shootConfetti();
        }
    };

    const shootConfetti = () => {
        const colors = ['#fc7594', '#d8b4e2', '#4dc2f8', '#2bcbba', '#ffd700', '#3fb950'];
        for (let i = 0; i < 30; i++) {
            let conf = document.createElement('div');
            conf.className = 'confetti';
            conf.style.left = Math.random() * 100 + 'vw';
            conf.style.top = -10 + 'px';
            conf.style.backgroundColor = colors[Math.floor(Math.random() * colors.length)];
            conf.style.animationDuration = (Math.random() * 2 + 1.5) + 's';
            document.body.appendChild(conf);
            setTimeout(() => conf.remove(), 4000);
        }
    };

    const streakOverlay = document.getElementById('streakOverlay');
    const closeStreakBtn = document.getElementById('closeStreakBtn');
    const streakScoreDisplay = document.getElementById('streakScoreDisplay');
    const helpModal = document.getElementById('helpModal');
    const closeHelpBtn = document.getElementById('closeHelpBtn');

    if (auraMeterBtn && streakOverlay) {
        auraMeterBtn.addEventListener('click', () => {
            streakScoreDisplay.innerText = currentAura;
            streakOverlay.classList.remove('hidden');
            shootConfetti();
        });

        closeStreakBtn.addEventListener('click', () => {
            streakOverlay.classList.add('hidden');
        });
    }

    if (helpBtn && helpModal) {
        helpBtn.addEventListener('click', () => {
            helpModal.classList.remove('hidden');
        });

        closeHelpBtn.addEventListener('click', () => {
            helpModal.classList.add('hidden');
        });

        // Close on outside click
        helpModal.addEventListener('click', (e) => {
            if (e.target === helpModal) {
                helpModal.classList.add('hidden');
            }
        });
    }

    // Syntax Highlighting Engine
    const highlightCode = (text) => {
        let hl = text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");

        // GenZ Keywords & Operators (Handling multi-word keywords)
        const keywords = ['its giving :', 'peace out', 'no cap :', 'hits same', 'clears', 'flops', 'gained', 'lost', 'drop', 'aura', 'sus', 'yap', 'fr'];
        // Sort keywords by length descending to match longest phrases first
        keywords.sort((a, b) => b.length - a.length);

        const keywordRegex = new RegExp(`(${keywords.join('|')})`, 'g');
        hl = hl.replace(keywordRegex, (match) => {
            return `<span class='hl-keyword'>${match}</span>`;
        });

        // Strings (Strip any injected spans inside the string to keep it pure blue)
        hl = hl.replace(/"([^"\\]*(\\.[^"\\]*)*)"/g, (match) => {
            let pureText = match.replace(/<[^>]+>/g, '');
            return "<span class='hl-string'>" + pureText + "</span>";
        });

        // Numbers
        hl = hl.replace(/\b(\d+)\b/g, "<span class='hl-number'>$1</span>");

        // Single Line Comments (strip inner spans so keywords inside comments turn grey too)
        hl = hl.replace(/(\/\/.*)/g, (match) => {
            let pureText = match.replace(/<[^>]+>/g, '');
            return "<span class='hl-comment'>" + pureText + "</span>";
        });

        // Prevent collapsing of trailing newlines which misaligns textarea cursor
        if (hl.endsWith('\n')) hl += ' ';

        return hl;
    };

    const updateEditor = () => {
        // Update line numbers
        const lines = codeEditor.value.split('\n').length;
        lineNumbers.innerHTML = Array(lines).fill(0).map((_, i) => i + 1).join('<br>');
        // Update Syntax Highlight
        highlightLayer.innerHTML = highlightCode(codeEditor.value);
    };

    codeEditor.addEventListener('input', updateEditor);

    codeEditor.addEventListener('scroll', () => {
        lineNumbers.scrollTop = codeEditor.scrollTop;
        highlightLayer.scrollTop = codeEditor.scrollTop;
        highlightLayer.scrollLeft = codeEditor.scrollLeft;
    });

    updateEditor();

    // Handle Tab Key for indentation
    codeEditor.addEventListener('keydown', (e) => {
        if (e.key === 'Tab') {
            e.preventDefault();
            const start = codeEditor.selectionStart;
            const end = codeEditor.selectionEnd;
            codeEditor.value = codeEditor.value.substring(0, start) + '    ' + codeEditor.value.substring(end);
            codeEditor.selectionStart = codeEditor.selectionEnd = start + 4;
        }
    });

    const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));

    const escapeHtml = (unsafe) => {
        if (!unsafe) return '';
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;");
    };

    const appendToTerminal = (htmlStr) => {
        const span = document.createElement('span');
        span.innerHTML = htmlStr;
        outputDisplay.appendChild(span);
        outputDisplay.scrollTop = outputDisplay.scrollHeight;
    };

    const playSound = (type) => {
        const audio = new Audio(type === 'success' ? 'success.mp3' : 'error.mp3');
        audio.play().catch(e => console.log("Sound play failed (interaction required?):", e));
    };

    // AST Visualizer Engine
    const buildASTTree = (astString) => {
        const lines = astString.split('\n').filter(l => l.trim() !== '');
        if (lines.length === 0) return null;

        let root = { depth: -1, children: [] };
        let stack = [root];

        for (let line of lines) {
            const depth = line.search(/\S/);
            const content = line.trim();
            const node = { depth, content, children: [] };

            while (stack.length > 1 && stack[stack.length - 1].depth >= depth) {
                stack.pop();
            }
            stack[stack.length - 1].children.push(node);
            stack.push(node);
        }
        return root.children;
    };

    const renderASTHTML = (nodes) => {
        if (!nodes || nodes.length === 0) return '';
        let html = '<ul class="ast-tree">';
        for (let node of nodes) {
            html += '<li>';
            if (node.children.length > 0) {
                html += `<details open><summary><span class="ast-node">${escapeHtml(node.content)}</span></summary>`;
                html += renderASTHTML(node.children);
                html += `</details>`;
            } else {
                html += `<span class="ast-node leaf">${escapeHtml(node.content)}</span>`;
            }
            html += '</li>';
        }
        html += '</ul>';
        return html;
    };

    const runCompiler = async () => {
        const sourceCode = codeEditor.value;
        if (!sourceCode.trim()) {
            outputDisplay.innerHTML = '<span class="error">Error: Source code cannot be empty.</span>';
            return;
        }

        // Lock UI
        runBtn.classList.add('disabled');
        loadingIndicator.classList.remove('hidden');
        outputDisplay.innerHTML = ''; // clear output

        try {
            const response = await fetch('http://localhost:8080/compile', {
                method: 'POST',
                headers: { 'Content-Type': 'text/plain' },
                body: sourceCode
            });

            const data = await response.json();

            // Hide loading indicator as we start typing out the response
            loadingIndicator.classList.add('hidden');

            if (data.phases && data.phases.length > 0) {
                // Interactive Phase Output
                for (let i = 0; i < data.phases.length; i++) {
                    const phase = data.phases[i];

                    // Announce the phase
                    appendToTerminal(`<br><b style="color:var(--accent)">[Phase ${i + 1}/${data.phases.length}] Running ${phase.name}...</b><br>`);
                    await sleep(400); // Dramatic pause

                    // Show output
                    if (phase.status === 'success') {
                        if (phase.name === "Syntax Analysis (AST)") {
                            const astTreeHTML = renderASTHTML(buildASTTree(phase.output));
                            appendToTerminal(`<div class="ast-visualizer-container">${astTreeHTML}</div>`);
                        } else {
                            appendToTerminal(`<span class="code" style="opacity:0.8">${escapeHtml(phase.output)}<br></span>`);
                        }
                        await sleep(500);
                    } else {
                        let errorOutput = escapeHtml(phase.output);
                        if (errorOutput.includes("Hint:") || errorOutput.includes("Did you mean")) {
                            const hintText = errorOutput.includes("Hint:") ? "Hint:" : "Did you mean";
                            errorOutput = errorOutput.replace(hintText, '<br><br><strong style="color: var(--hint-color, #ffd700);">💡 Suggestion:</strong><span style="color: var(--hint-color, #ffd700);">') + '</span>';
                        }
                        appendToTerminal(`<span class="error"><b>[!] ERROR:</b><br>${errorOutput}<br></span>`);
                        playSound('error');
                        break; // Stop executing pipeline on error
                    }
                }

                if (data.success) {
                    await sleep(300);
                    appendToTerminal(`<br><b style="color:var(--success)">[+] Compilation Pipeline Completed Successfully!</b><br>`);
                    playSound('success');
                    updateAura(8);
                } else {
                    appendToTerminal(`<br><b style="color:var(--error)">[-] Compilation Aborted.</b><br>`);
                    playSound('error');
                    updateAura(-12);
                }
            } else if (!data.success) {
                appendToTerminal(`<span class="error">${escapeHtml(data.error)}</span>`);
                playSound('error');
                updateAura(-12);
            }

        } catch (error) {
            loadingIndicator.classList.add('hidden');
            appendToTerminal(`<span class="error">Fatal Error: Could not connect to backend.\\n${error.message}</span>`);
            playSound('error');
            updateAura(-12);
        } finally {
            runBtn.classList.remove('disabled');
        }
    };

    runBtn.addEventListener('click', runCompiler);

    clearBtn.addEventListener('click', () => {
        outputDisplay.innerHTML = '<span class="placeholder">Awaiting compilation...</span>';
    });

    const themes = ['default', 'hacker', 'vaporwave'];
    let currentThemeIndex = 0;
    themeToggleBtn.addEventListener('click', () => {
        currentThemeIndex = (currentThemeIndex + 1) % themes.length;
        const newTheme = themes[currentThemeIndex];
        document.body.setAttribute('data-theme', newTheme);
        themeToggleBtn.querySelector('span').innerText = 'Theme: ' + newTheme.charAt(0).toUpperCase() + newTheme.slice(1);
    });
});
