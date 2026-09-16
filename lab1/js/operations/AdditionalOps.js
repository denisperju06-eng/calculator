/**
 * Cerința (b): Minim 3 operații adăugătoare (radical, procente, x^2, 1/x, |x|, n!, x^y)
 * Toate operațiile moștenesc UnaryOperation sau BinaryOperation.
 */

(() => {
    const _scope = typeof window !== 'undefined' ? window : (typeof global !== 'undefined' ? global : globalThis);
    const BaseUnOp = _scope.UnaryOperation;
    const BaseBinOp = _scope.BinaryOperation;

    class SquareRootOperation extends BaseUnOp {
    constructor() {
        super("Radical de ordin 2", "√");
    }

    execute(x) {
        if (x < 0) {
            const { DomainMathError } = _scope.CalculatorErrors;
            throw new DomainMathError("Radical", "Numărul trebuie să fie mai mare sau egal cu 0 (√x, x ≥ 0)!");
        }
        return Math.sqrt(x);
    }
}

class CubeRootOperation extends BaseUnOp {
    constructor() {
        super("Radical de ordin 3", "∛");
    }

    execute(x) {
        return Math.cbrt(x);
    }
}

class SquareOperation extends BaseUnOp {
    constructor() {
        super("Ridicarea la pătrat", "x²");
    }

    execute(x) {
        const res = x * x;
        if (!Number.isFinite(res)) {
            const { OverflowError } = _scope.CalculatorErrors;
            throw new OverflowError();
        }
        return res;
    }
}

class CubeOperation extends BaseUnOp {
    constructor() {
        super("Ridicarea la cub", "x³");
    }

    execute(x) {
        const res = x * x * x;
        if (!Number.isFinite(res)) {
            const { OverflowError } = _scope.CalculatorErrors;
            throw new OverflowError();
        }
        return res;
    }
}

class PowerOperation extends BaseBinOp {
    constructor() {
        super("Ridicarea la putere", "xʸ");
    }

    execute(x, y) {
        if (x < 0 && !Number.isInteger(y)) {
            const { DomainMathError } = _scope.CalculatorErrors;
            throw new DomainMathError("xʸ", "Baza negativă ridicată la o putere fracționară nu produce un număr real!");
        }
        const res = Math.pow(x, y);
        if (!Number.isFinite(res)) {
            const { OverflowError } = _scope.CalculatorErrors;
            throw new OverflowError();
        }
        return res;
    }
}

class PercentageOperation extends BaseUnOp {
    constructor() {
        super("Procent", "%");
    }

    execute(x) {
        return x / 100;
    }
}

class ReciprocalOperation extends BaseUnOp {
    constructor() {
        super("Inversul numărului", "1/x");
    }

    execute(x) {
        if (x === 0) {
            const { DivisionByZeroError } = _scope.CalculatorErrors;
            throw new DivisionByZeroError("Nu se poate calcula inversul lui zero (1/0)!");
        }
        return 1 / x;
    }
}

class AbsoluteOperation extends BaseUnOp {
    constructor() {
        super("Valoare absolută", "|x|");
    }

    execute(x) {
        return Math.abs(x);
    }
}

class FactorialOperation extends BaseUnOp {
    constructor() {
        super("Factorial", "n!");
    }

    execute(x) {
        if (x < 0 || !Number.isInteger(x)) {
            const { DomainMathError } = _scope.CalculatorErrors;
            throw new DomainMathError("Factorial", "Factorialul este definit doar pentru numere întregi pozitive!");
        }
        if (x > 170) {
            const { OverflowError } = _scope.CalculatorErrors;
            throw new OverflowError("Factorialul depășește limita reprezentabilă pentru numere reale (x > 170)!");
        }
        let result = 1;
        for (let i = 2; i <= x; i++) {
            result *= i;
        }
        return result;
    }
}

class NegateOperation extends BaseUnOp {
    constructor() {
        super("Schimbare de semn", "±");
    }

    execute(x) {
        return -x;
    }
}

    _scope.AdditionalOps = {
        SquareRootOperation,
        CubeRootOperation,
        SquareOperation,
        CubeOperation,
        PowerOperation,
        PercentageOperation,
        ReciprocalOperation,
        AbsoluteOperation,
        FactorialOperation,
        NegateOperation
    };
})();
