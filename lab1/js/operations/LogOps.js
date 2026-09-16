/**
 * Cerința (c): Minim 3 operații logaritmice (log10, ln, log2, plus 10^x, e^x)
 * Moștenire din UnaryOperation.
 */

(() => {
    const _scope = typeof window !== 'undefined' ? window : (typeof global !== 'undefined' ? global : globalThis);
    const BaseUnOp = _scope.UnaryOperation;

    class NaturalLogOperation extends BaseUnOp {
    constructor() {
        super("Logaritm Natural", "ln");
    }

    execute(x) {
        if (x <= 0) {
            const { DomainMathError } = _scope.CalculatorErrors;
            throw new DomainMathError("ln", "Logaritmul natural este definit doar pentru numere strict pozitive (x > 0)!");
        }
        return Math.log(x);
    }
}

class Log10Operation extends BaseUnOp {
    constructor() {
        super("Logaritm Baza 10", "log₁₀");
    }

    execute(x) {
        if (x <= 0) {
            const { DomainMathError } = _scope.CalculatorErrors;
            throw new DomainMathError("log₁₀", "Logaritmul în baza 10 este definit doar pentru numere strict pozitive (x > 0)!");
        }
        return Math.log10(x);
    }
}

class Log2Operation extends BaseUnOp {
    constructor() {
        super("Logaritm Baza 2", "log₂");
    }

    execute(x) {
        if (x <= 0) {
            const { DomainMathError } = _scope.CalculatorErrors;
            throw new DomainMathError("log₂", "Logaritmul în baza 2 este definit doar pentru numere strict pozitive (x > 0)!");
        }
        return Math.log2(x);
    }
}

class ExpOperation extends BaseUnOp {
    constructor() {
        super("Exponențială eˣ", "eˣ");
    }

    execute(x) {
        const res = Math.exp(x);
        if (!Number.isFinite(res)) {
            const { OverflowError } = _scope.CalculatorErrors;
            throw new OverflowError("Rezultatul eˣ depășește limita numerică!");
        }
        return res;
    }
}

class TenPowerOperation extends BaseUnOp {
    constructor() {
        super("10 la puterea x", "10ˣ");
    }

    execute(x) {
        const res = Math.pow(10, x);
        if (!Number.isFinite(res)) {
            const { OverflowError } = _scope.CalculatorErrors;
            throw new OverflowError("Rezultatul 10ˣ depășește limita numerică!");
        }
        return res;
    }
}

    _scope.LogOps = {
        NaturalLogOperation,
        Log10Operation,
        Log2Operation,
        ExpOperation,
        TenPowerOperation
    };
})();
