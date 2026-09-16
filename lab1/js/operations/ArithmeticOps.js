(() => {
    const _scope = typeof window !== 'undefined' ? window : (typeof global !== 'undefined' ? global : globalThis);
    const BaseBinOp = _scope.BinaryOperation;

    class AddOperation extends BaseBinOp {
        constructor() {
            super("Adunare", "+");
        }

        execute(a, b) {
            return a + b;
        }
    }

    class SubtractOperation extends BaseBinOp {
        constructor() {
            super("Scădere", "−");
        }

        execute(a, b) {
            return a - b;
        }
    }

    class MultiplyOperation extends BaseBinOp {
        constructor() {
            super("Înmulțire", "×");
        }

        execute(a, b) {
            return a * b;
        }
    }

    class DivideOperation extends BaseBinOp {
        constructor() {
            super("Împărțire", "÷");
        }

        execute(a, b) {
            if (Math.abs(b) < Number.EPSILON || b === 0) {
                const { DivisionByZeroError } = _scope.CalculatorErrors;
                throw new DivisionByZeroError("Nu se poate împărți la zero!");
            }
            return a / b;
        }
    }

    class ModuloOperation extends BaseBinOp {
        constructor() {
            super("Modulo", "mod");
        }

        execute(a, b) {
            if (b === 0) {
                const { DivisionByZeroError } = _scope.CalculatorErrors;
                throw new DivisionByZeroError("Nu se poate calcula modulo cu împărțitor zero!");
            }
            return a % b;
        }
    }

    _scope.ArithmeticOps = {
        AddOperation,
        SubtractOperation,
        MultiplyOperation,
        DivideOperation,
        ModuloOperation
    };
})();
