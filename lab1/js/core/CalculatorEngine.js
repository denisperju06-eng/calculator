/**
 * CalculatorEngine - Nucleul aplicației bazat pe OOP.
 * Integrează:
 * - Registru de operații polimorfice (Strategy pattern)
 * - Gestionar de memorie încapsulat (MemoryUnit)
 * - Convertor de sisteme de numerație (BaseConverter)
 * - Ierarhie de tratare a erorilor de calcul (try/catch + CalculatorError)
 */

(() => {
    const _scope = typeof window !== 'undefined' ? window : (typeof global !== 'undefined' ? global : globalThis);

    class CalculatorEngine {
    #displayValue = '0';
    #previousExpression = '';
    #firstOperand = null;
    #pendingOperator = null;
    #waitingForSecondOperand = false;
    #hasError = false;
    #errorMessage = '';
    #angleMode = 'DEG'; // 'DEG' sau 'RAD'
    #currentBase = 'DEC'; // 'DEC', 'HEX', 'OCT', 'BIN'
    
    #memoryUnit;
    #baseConverter;
    #operations = new Map();
    #history = [];

    constructor() {
        this.#memoryUnit = new _scope.MemoryUnit();
        this.#baseConverter = new _scope.BaseConverter();
        this.#registerOperations();
    }

    /**
     * Înregistrează instanțele tuturor claselor de operații (Polimorfism)
     */
    #registerOperations() {
        const { AddOperation, SubtractOperation, MultiplyOperation, DivideOperation, ModuloOperation } = _scope.ArithmeticOps;
        const { SquareRootOperation, CubeRootOperation, SquareOperation, CubeOperation, PowerOperation,
                PercentageOperation, ReciprocalOperation, AbsoluteOperation, FactorialOperation, NegateOperation } = _scope.AdditionalOps;
        const { SinOperation, CosOperation, TanOperation, AsinOperation, AcosOperation, AtanOperation } = _scope.TrigOps;
        const { NaturalLogOperation, Log10Operation, Log2Operation, ExpOperation, TenPowerOperation } = _scope.LogOps;

        // Operații aritmetice (cerința a)
        this.#register('+', new AddOperation());
        this.#register('-', new SubtractOperation());
        this.#register('*', new MultiplyOperation());
        this.#register('/', new DivideOperation());
        this.#register('mod', new ModuloOperation());

        // Operații adăugătoare (cerința b)
        this.#register('sqrt', new SquareRootOperation());
        this.#register('cbrt', new CubeRootOperation());
        this.#register('sqr', new SquareOperation());
        this.#register('cube', new CubeOperation());
        this.#register('pow', new PowerOperation());
        this.#register('pct', new PercentageOperation());
        this.#register('recip', new ReciprocalOperation());
        this.#register('abs', new AbsoluteOperation());
        this.#register('fact', new FactorialOperation());
        this.#register('negate', new NegateOperation());

        // Operații trigonometrice (cerința c)
        this.#register('sin', new SinOperation());
        this.#register('cos', new CosOperation());
        this.#register('tan', new TanOperation());
        this.#register('asin', new AsinOperation());
        this.#register('acos', new AcosOperation());
        this.#register('atan', new AtanOperation());

        // Operații logaritmice (cerința c)
        this.#register('ln', new NaturalLogOperation());
        this.#register('log', new Log10Operation());
        this.#register('log2', new Log2Operation());
        this.#register('exp', new ExpOperation());
        this.#register('10x', new TenPowerOperation());
    }

    #register(key, operationInstance) {
        this.#operations.set(key, operationInstance);
    }

    /**
     * Adăugare cifră sau caracter (suportă și A-F pentru HEX)
     */
    inputDigit(digit) {
        if (this.#hasError) {
            this.clear();
        }

        const upperDigit = String(digit).toUpperCase();

        // Validare în funcție de baza curentă (Programmer mode)
        if (this.#currentBase === 'BIN' && !['0', '1'].includes(upperDigit)) return;
        if (this.#currentBase === 'OCT' && !['0', '1', '2', '3', '4', '5', '6', '7'].includes(upperDigit)) return;
        if (this.#currentBase === 'DEC' && !['0', '1', '2', '3', '4', '5', '6', '7', '8', '9'].includes(upperDigit)) return;
        if (this.#currentBase === 'HEX' && !['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'].includes(upperDigit)) return;

        if (this.#waitingForSecondOperand) {
            this.#displayValue = upperDigit;
            this.#waitingForSecondOperand = false;
        } else {
            this.#displayValue = (this.#displayValue === '0' || this.#displayValue === '-0') ? upperDigit : this.#displayValue + upperDigit;
        }
    }

    /**
     * Adăugare separator zecimal
     */
    inputDecimal() {
        if (this.#hasError) {
            this.clear();
        }
        if (this.#currentBase !== 'DEC') {
            // Modurile HEX/BIN/OCT lucrează de regulă cu întregi în calculatoare de buzunar
            return;
        }
        if (this.#waitingForSecondOperand) {
            this.#displayValue = '0.';
            this.#waitingForSecondOperand = false;
            return;
        }
        if (!this.#displayValue.includes('.')) {
            this.#displayValue += '.';
        }
    }

    /**
     * Inserare constantă (π sau e)
     */
    inputConstant(constName) {
        if (this.#hasError) this.clear();
        if (constName === 'pi') {
            this.#displayValue = String(Math.PI);
        } else if (constName === 'e') {
            this.#displayValue = String(Math.E);
        }
        this.#waitingForSecondOperand = false;
    }

    /**
     * Ștergere completă (C)
     */
    clear() {
        this.#displayValue = '0';
        this.#previousExpression = '';
        this.#firstOperand = null;
        this.#pendingOperator = null;
        this.#waitingForSecondOperand = false;
        this.#hasError = false;
        this.#errorMessage = '';
    }

    /**
     * Ștergere intrare curentă (CE)
     */
    clearEntry() {
        this.#displayValue = '0';
        this.#hasError = false;
        this.#errorMessage = '';
    }

    /**
     * Ștergere ultimul caracter (Backspace)
     */
    backspace() {
        if (this.#hasError) {
            this.clear();
            return;
        }
        if (this.#waitingForSecondOperand) return;
        
        if (this.#displayValue.length > 1) {
            this.#displayValue = this.#displayValue.slice(0, -1);
            if (this.#displayValue === '-' || this.#displayValue === '') {
                this.#displayValue = '0';
            }
        } else {
            this.#displayValue = '0';
        }
    }

    /**
     * Schimbare semn (+/-)
     */
    toggleSign() {
        if (this.#hasError) return;
        try {
            const op = this.#operations.get('negate');
            const currentNum = this.#getCurrentNumber();
            const result = op.execute(currentNum);
            this.#updateDisplayWithNumber(result);
        } catch (err) {
            this.#handleError(err);
        }
    }

    /**
     * Setare operator binar (+, -, *, /, mod, pow)
     */
    setBinaryOperator(opKey) {
        if (this.#hasError) return;
        
        const inputNum = this.#getCurrentNumber();
        const op = this.#operations.get(opKey);
        if (!op) return;

        if (this.#pendingOperator && !this.#waitingForSecondOperand) {
            // Executăm operația anterioară în lanț
            const success = this.#executePending();
            if (!success) return;
        } else {
            this.#firstOperand = inputNum;
        }

        this.#pendingOperator = opKey;
        this.#waitingForSecondOperand = true;
        this.#previousExpression = `${this.#formatNumber(this.#firstOperand)} ${op.getSymbol()}`;
    }

    /**
     * Egal (=)
     */
    calculateEquals() {
        if (this.#hasError || !this.#pendingOperator || this.#firstOperand === null) {
            return;
        }

        const secondOperand = this.#getCurrentNumber();
        const op = this.#operations.get(this.#pendingOperator);
        const expr = `${this.#formatNumber(this.#firstOperand)} ${op.getSymbol()} ${this.#formatNumber(secondOperand)}`;

        const success = this.#executePending();
        if (success) {
            this.#previousExpression = `${expr} =`;
            this.#history.unshift({
                expression: expr,
                result: this.#displayValue,
                timestamp: new Date()
            });
            this.#firstOperand = null;
            this.#pendingOperator = null;
            this.#waitingForSecondOperand = true;
        }
    }

    #executePending() {
        if (!this.#pendingOperator || this.#firstOperand === null) return true;

        try {
            const op = this.#operations.get(this.#pendingOperator);
            const secondOperand = this.#getCurrentNumber();
            
            // Apel polimorfic
            const result = op.execute(this.#firstOperand, secondOperand);
            
            this.#validateNumericResult(result);
            this.#firstOperand = result;
            this.#updateDisplayWithNumber(result);
            return true;
        } catch (err) {
            this.#handleError(err);
            return false;
        }
    }

    /**
     * Aplică o operație unară (ex: sqrt, sin, cos, ln, x^2, fact etc.)
     */
    applyUnaryOperator(opKey) {
        if (this.#hasError) return;

        try {
            const op = this.#operations.get(opKey);
            if (!op) return;

            const inputNum = this.#getCurrentNumber();
            let result;

            if (op instanceof _scope.TrigOps.SinOperation ||
                op instanceof _scope.TrigOps.CosOperation ||
                op instanceof _scope.TrigOps.TanOperation ||
                op instanceof _scope.TrigOps.AsinOperation ||
                op instanceof _scope.TrigOps.AcosOperation ||
                op instanceof _scope.TrigOps.AtanOperation) {
                // Trimitem unitatea de unghi curentă (DEG / RAD)
                result = op.execute(inputNum, this.#angleMode);
            } else {
                result = op.execute(inputNum);
            }

            this.#validateNumericResult(result);
            this.#previousExpression = `${op.getSymbol()}(${this.#formatNumber(inputNum)})`;
            this.#updateDisplayWithNumber(result);
            this.#waitingForSecondOperand = true;

            this.#history.unshift({
                expression: this.#previousExpression,
                result: this.#displayValue,
                timestamp: new Date()
            });
        } catch (err) {
            this.#handleError(err);
        }
    }

    /**
     * Prelucrarea erorilor de calcule (Cerința e)
     */
    #handleError(err) {
        this.#hasError = true;
        if (err instanceof _scope.CalculatorErrors.CalculatorError) {
            this.#errorMessage = err.message;
        } else {
            this.#errorMessage = err.message || "Eroare de calcul necunoscută!";
        }
        this.#displayValue = "Eroare";
    }

    #validateNumericResult(val) {
        if (Number.isNaN(val)) {
            const { DomainMathError } = _scope.CalculatorErrors;
            throw new DomainMathError("Calcul", "Rezultatul este nedefinit (NaN)!");
        }
        if (!Number.isFinite(val)) {
            const { OverflowError } = _scope.CalculatorErrors;
            throw new OverflowError();
        }
    }

    // --- OPERAȚII CU MEMORIA (Cerința d) ---

    memoryClear() {
        this.#memoryUnit.clear();
    }

    memoryRecall() {
        if (!this.#memoryUnit.hasValue()) return;
        const memVal = this.#memoryUnit.recall();
        this.#updateDisplayWithNumber(memVal);
        this.#waitingForSecondOperand = false;
    }

    memoryStore() {
        if (this.#hasError) return;
        const current = this.#getCurrentNumber();
        this.#memoryUnit.store(current);
    }

    memoryAdd() {
        if (this.#hasError) return;
        const current = this.#getCurrentNumber();
        this.#memoryUnit.add(current);
    }

    memorySubtract() {
        if (this.#hasError) return;
        const current = this.#getCurrentNumber();
        this.#memoryUnit.subtract(current);
    }

    // --- CONVERSIE BAZE (Cerința f) ---

    setBase(newBase) {
        if (this.#currentBase === newBase) return;
        try {
            // Convertim valoarea curentă în număr zecimal
            const decNum = this.#getCurrentNumber();
            this.#currentBase = newBase;
            
            // Actualizăm afișajul în noua bază
            if (newBase === 'HEX') {
                this.#displayValue = this.#baseConverter.toHex(decNum);
            } else if (newBase === 'OCT') {
                this.#displayValue = this.#baseConverter.toOct(decNum);
            } else if (newBase === 'BIN') {
                this.#displayValue = this.#baseConverter.toBin(decNum, false);
            } else {
                this.#displayValue = String(decNum);
            }
        } catch (err) {
            this.#handleError(err);
        }
    }

    toggleAngleMode() {
        this.#angleMode = this.#angleMode === 'DEG' ? 'RAD' : 'DEG';
        return this.#angleMode;
    }

    setAngleMode(mode) {
        if (['DEG', 'RAD'].includes(mode)) {
            this.#angleMode = mode;
        }
    }

    #getCurrentNumber() {
        if (this.#currentBase === 'DEC') {
            const num = parseFloat(this.#displayValue);
            return Number.isFinite(num) ? num : 0;
        } else {
            return this.#baseConverter.parseToDecimal(this.#displayValue, this.#currentBase);
        }
    }

    #updateDisplayWithNumber(num) {
        if (this.#currentBase === 'DEC') {
            // Formatare frumoasă pentru numere zecimale, evitând erorile de precizie plutitoare
            if (Math.abs(num) < 1e-12 && num !== 0) {
                this.#displayValue = '0';
            } else {
                // Păstrăm precizie maximă dar eliminăm zerourile redundante
                const rounded = parseFloat(num.toPrecision(12));
                this.#displayValue = String(rounded);
            }
        } else if (this.#currentBase === 'HEX') {
            this.#displayValue = this.#baseConverter.toHex(num);
        } else if (this.#currentBase === 'OCT') {
            this.#displayValue = this.#baseConverter.toOct(num);
        } else if (this.#currentBase === 'BIN') {
            this.#displayValue = this.#baseConverter.toBin(num, false);
        }
    }

    #formatNumber(num) {
        if (num === null || num === undefined) return '';
        const rounded = parseFloat(num.toPrecision(10));
        return String(rounded);
    }

    /**
     * Returnează starea curentă a calculatorului pentru UI
     */
    getState() {
        const currentDec = this.#hasError ? 0 : this.#getCurrentNumber();
        return {
            displayValue: this.#displayValue,
            previousExpression: this.#previousExpression,
            hasError: this.#hasError,
            errorMessage: this.#errorMessage,
            angleMode: this.#angleMode,
            currentBase: this.#currentBase,
            hasMemory: this.#memoryUnit.hasValue(),
            memoryValue: this.#memoryUnit.getValue(),
            baseValues: this.#baseConverter.getAllBases(currentDec),
            history: this.#history
        };
    }
}

    _scope.CalculatorEngine = CalculatorEngine;
})();
