/**
 * CalculatorUI - Controller pentru interfața utilizator (DOM Controller)
 * Conectează acțiunile utilizatorului la motorul CalculatorEngine (Separation of Concerns).
 */

(() => {
    const _scope = typeof window !== 'undefined' ? window : (typeof global !== 'undefined' ? global : globalThis);

    class CalculatorUI {
        constructor(engine) {
        this.engine = engine;
        this.currentMode = 'scientific'; // 'standard', 'scientific', 'programmer'
        
        this.initDOMElements();
        this.bindEvents();
        this.bindKeyboard();
        this.switchMode(this.currentMode);
    }

    initDOMElements() {
        this.displayPrimary = document.getElementById('displayPrimary');
        this.displaySecondary = document.getElementById('displaySecondary');
        this.errorBanner = document.getElementById('errorBanner');
        this.errorMessage = document.getElementById('errorMessage');
        this.angleModeBtn = document.getElementById('angleModeBtn');
        this.memoryIndicator = document.getElementById('memoryIndicator');
        
        // Tab-uri moduri
        this.modeTabs = document.querySelectorAll('.mode-tab');
        this.keypadStandard = document.getElementById('keypadStandard');
        this.keypadScientific = document.getElementById('keypadScientific');
        this.keypadProgrammer = document.getElementById('keypadProgrammer');
        
        // Panou baze (Programator)
        this.baseRows = document.querySelectorAll('.base-row');
        this.baseHexVal = document.getElementById('baseHexVal');
        this.baseDecVal = document.getElementById('baseDecVal');
        this.baseOctVal = document.getElementById('baseOctVal');
        this.baseBinVal = document.getElementById('baseBinVal');

        // Istoric
        this.historyList = document.getElementById('historyList');
        this.historyModal = document.getElementById('historyModal');
        this.historyToggleBtn = document.getElementById('historyToggleBtn');
        this.clearHistoryBtn = document.getElementById('clearHistoryBtn');
    }

    bindEvents() {
        // Ascultători pentru toate butoanele calculatorului
        document.querySelectorAll('button[data-action]').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const action = btn.getAttribute('data-action');
                const val = btn.getAttribute('data-value');
                this.handleAction(action, val);
                this.createRipple(e, btn);
            });
        });

        // Schimbare mod (Standard / Științific / Programator)
        this.modeTabs.forEach(tab => {
            tab.addEventListener('click', () => {
                const targetMode = tab.getAttribute('data-mode');
                this.switchMode(targetMode);
            });
        });

        // Comutare bază în modul programator (HEX, DEC, OCT, BIN)
        this.baseRows.forEach(row => {
            row.addEventListener('click', () => {
                const base = row.getAttribute('data-base');
                this.engine.setBase(base);
                this.updateBaseRowActive(base);
                this.updateView();
            });
        });

        // Toggle DEG/RAD
        if (this.angleModeBtn) {
            this.angleModeBtn.addEventListener('click', () => {
                this.engine.toggleAngleMode();
                this.updateView();
            });
        }

        // Toggle Istoric
        if (this.historyToggleBtn && this.historyModal) {
            this.historyToggleBtn.addEventListener('click', () => {
                this.historyModal.classList.toggle('active');
            });
        }
        if (this.clearHistoryBtn) {
            this.clearHistoryBtn.addEventListener('click', () => {
                this.engine.clear();
                if (this.historyList) this.historyList.innerHTML = '<div class="empty-history">Nu există calcule recente</div>';
            });
        }

        // Închidere modal la click în afară
        document.addEventListener('click', (e) => {
            if (this.historyModal && this.historyModal.classList.contains('active')) {
                if (!this.historyModal.contains(e.target) && !this.historyToggleBtn.contains(e.target)) {
                    this.historyModal.classList.remove('active');
                }
            }
        });
    }

    handleAction(action, val) {
        switch (action) {
            case 'digit':
                this.engine.inputDigit(val);
                break;
            case 'decimal':
                this.engine.inputDecimal();
                break;
            case 'constant':
                this.engine.inputConstant(val);
                break;
            case 'binary-op':
                this.engine.setBinaryOperator(val);
                break;
            case 'unary-op':
                this.engine.applyUnaryOperator(val);
                break;
            case 'equals':
                this.engine.calculateEquals();
                break;
            case 'clear':
                this.engine.clear();
                break;
            case 'clear-entry':
                this.engine.clearEntry();
                break;
            case 'backspace':
                this.engine.backspace();
                break;
            case 'toggle-sign':
                this.engine.toggleSign();
                break;
            case 'mc':
                this.engine.memoryClear();
                break;
            case 'mr':
                this.engine.memoryRecall();
                break;
            case 'm-plus':
                this.engine.memoryAdd();
                break;
            case 'm-minus':
                this.engine.memorySubtract();
                break;
            case 'ms':
                this.engine.memoryStore();
                break;
        }
        this.updateView();
    }

    bindKeyboard() {
        window.addEventListener('keydown', (e) => {
            // Ignorăm tastele funcționale de sistem (F1-F12, etc.)
            if (e.ctrlKey || e.metaKey || e.altKey) return;

            const key = e.key;

            if (/^[0-9]$/.test(key)) {
                this.handleAction('digit', key);
            } else if (this.currentMode === 'programmer' && /^[a-fA-F]$/.test(key)) {
                this.handleAction('digit', key.toUpperCase());
            } else if (key === '.' || key === ',') {
                this.handleAction('decimal');
            } else if (key === '+') {
                this.handleAction('binary-op', '+');
            } else if (key === '-') {
                this.handleAction('binary-op', '-');
            } else if (key === '*') {
                this.handleAction('binary-op', '*');
            } else if (key === '/') {
                e.preventDefault();
                this.handleAction('binary-op', '/');
            } else if (key === '%') {
                this.handleAction('unary-op', 'pct');
            } else if (key === 'Enter' || key === '=') {
                e.preventDefault();
                this.handleAction('equals');
            } else if (key === 'Backspace') {
                this.handleAction('backspace');
            } else if (key === 'Escape') {
                this.handleAction('clear');
            }
        });
    }

    switchMode(mode) {
        this.currentMode = mode;
        this.modeTabs.forEach(t => {
            t.classList.toggle('active', t.getAttribute('data-mode') === mode);
        });

        // Comutare vizibilitate tastaturi
        if (this.keypadStandard) this.keypadStandard.classList.toggle('active', mode === 'standard');
        if (this.keypadScientific) this.keypadScientific.classList.toggle('active', mode === 'scientific');
        if (this.keypadProgrammer) this.keypadProgrammer.classList.toggle('active', mode === 'programmer');

        const basePanel = document.getElementById('programmerBasePanel');
        if (basePanel) {
            basePanel.style.display = (mode === 'programmer') ? 'flex' : 'none';
        }

        // Resetare bază dacă părăsim modul programator
        if (mode !== 'programmer') {
            this.engine.setBase('DEC');
        }

        this.updateView();
    }

    updateBaseRowActive(activeBase) {
        this.baseRows.forEach(r => {
            r.classList.toggle('active', r.getAttribute('data-base') === activeBase);
        });

        // Activare/dezactivare taste A-F în modul Programator
        const hexButtons = document.querySelectorAll('.btn-hex');
        const digitButtons = document.querySelectorAll('.btn-programmer-digit');
        
        hexButtons.forEach(btn => {
            btn.disabled = (activeBase !== 'HEX');
        });

        digitButtons.forEach(btn => {
            const digit = parseInt(btn.getAttribute('data-value'), 10);
            if (activeBase === 'BIN') {
                btn.disabled = digit > 1;
            } else if (activeBase === 'OCT') {
                btn.disabled = digit > 7;
            } else {
                btn.disabled = false;
            }
        });
    }

    updateView() {
        const state = this.engine.getState();

        // Afișaj principal
        if (this.displayPrimary) {
            this.displayPrimary.textContent = state.displayValue;
            this.adjustDisplayFontSize(state.displayValue);
        }

        // Afișaj expresie secundară
        if (this.displaySecondary) {
            this.displaySecondary.textContent = state.previousExpression || '\u00A0';
        }

        // Afișare erori (Cerința e)
        if (this.errorBanner) {
            if (state.hasError) {
                this.errorBanner.classList.add('visible');
                if (this.errorMessage) this.errorMessage.textContent = state.errorMessage;
            } else {
                this.errorBanner.classList.remove('visible');
            }
        }

        // Indicator unghi DEG / RAD
        if (this.angleModeBtn) {
            this.angleModeBtn.textContent = state.angleMode;
        }

        // Indicator memorie
        if (this.memoryIndicator) {
            this.memoryIndicator.classList.toggle('active', state.hasMemory);
            this.memoryIndicator.title = state.hasMemory ? `Valoare în memorie: ${state.memoryValue}` : 'Memorie liberă';
        }

        // Butoane memorie MC și MR active doar când există memorie
        const mcBtn = document.querySelectorAll('[data-action="mc"], [data-action="mr"]');
        mcBtn.forEach(btn => {
            btn.disabled = !state.hasMemory;
        });

        // Panou baze (HEX, DEC, OCT, BIN) - Mod Programator (Cerința f)
        if (this.baseHexVal) this.baseHexVal.textContent = state.baseValues.HEX;
        if (this.baseDecVal) this.baseDecVal.textContent = state.baseValues.DEC;
        if (this.baseOctVal) this.baseOctVal.textContent = state.baseValues.OCT;
        if (this.baseBinVal) this.baseBinVal.textContent = state.baseValues.BIN;

        // Actualizare istoric calcule
        this.renderHistory(state.history);
    }

    renderHistory(history) {
        if (!this.historyList) return;
        if (!history || history.length === 0) {
            this.historyList.innerHTML = '<div class="empty-history">Nu există calcule recente</div>';
            return;
        }

        this.historyList.innerHTML = history.slice(0, 20).map(item => `
            <div class="history-item">
                <div class="hist-expr">${this.escapeHTML(item.expression)}</div>
                <div class="hist-res">${this.escapeHTML(item.result)}</div>
            </div>
        `).join('');
    }

    adjustDisplayFontSize(text) {
        if (!this.displayPrimary) return;
        const len = text.length;
        if (len > 16) {
            this.displayPrimary.style.fontSize = '1.75rem';
        } else if (len > 11) {
            this.displayPrimary.style.fontSize = '2.25rem';
        } else if (len > 8) {
            this.displayPrimary.style.fontSize = '2.85rem';
        } else {
            this.displayPrimary.style.fontSize = '3.5rem';
        }
    }

    createRipple(event, button) {
        const circle = document.createElement('span');
        const diameter = Math.max(button.clientWidth, button.clientHeight);
        const radius = diameter / 2;
        const rect = button.getBoundingClientRect();

        circle.style.width = circle.style.height = `${diameter}px`;
        circle.style.left = `${event.clientX - rect.left - radius}px`;
        circle.style.top = `${event.clientY - rect.top - radius}px`;
        circle.classList.add('ripple');

        const ripple = button.getElementsByClassName('ripple')[0];
        if (ripple) ripple.remove();

        button.appendChild(circle);
    }

    escapeHTML(str) {
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');
    }
}

    _scope.CalculatorUI = CalculatorUI;
})();
