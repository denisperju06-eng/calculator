/**
 * Cerința (c): Minim 3 operații trigonometrice (sin, cos, tan, asin, acos, atan)
 * Suportă modurile de unghi DEG (grade) și RAD (radiani).
 */

(() => {
    const _scope = typeof window !== 'undefined' ? window : (typeof global !== 'undefined' ? global : globalThis);
    const BaseUnOp = _scope.UnaryOperation;

    class BaseTrigOperation extends BaseUnOp {
    constructor(name, symbol) {
        super(name, symbol);
        if (new.target === BaseTrigOperation) {
            throw new TypeError("Nu se poate instanția direct clasa abstractă BaseTrigOperation!");
        }
    }

    _toRadians(angle, unit) {
        if (unit === 'RAD') return angle;
        return (angle * Math.PI) / 180;
    }

    _fromRadians(rad, unit) {
        if (unit === 'RAD') return rad;
        return (rad * 180) / Math.PI;
    }
}

class SinOperation extends BaseTrigOperation {
    constructor() {
        super("Sinus", "sin");
    }

    execute(x, angleUnit = 'DEG') {
        const rad = this._toRadians(x, angleUnit);
        const res = Math.sin(rad);
        return Math.abs(res) < 1e-15 ? 0 : res;
    }
}

class CosOperation extends BaseTrigOperation {
    constructor() {
        super("Cosinus", "cos");
    }

    execute(x, angleUnit = 'DEG') {
        const rad = this._toRadians(x, angleUnit);
        const res = Math.cos(rad);
        return Math.abs(res) < 1e-15 ? 0 : res;
    }
}

class TanOperation extends BaseTrigOperation {
    constructor() {
        super("Tangentă", "tan");
    }

    execute(x, angleUnit = 'DEG') {
        const rad = this._toRadians(x, angleUnit);
        const cosVal = Math.cos(rad);
        if (Math.abs(cosVal) < 1e-12) {
            const { DomainMathError } = _scope.CalculatorErrors;
            throw new DomainMathError("tan", `Tangenta este nedefinită pentru unghiul ${x}° (cos = 0)!`);
        }
        const res = Math.tan(rad);
        return Math.abs(res) < 1e-15 ? 0 : res;
    }
}

class AsinOperation extends BaseTrigOperation {
    constructor() {
        super("Arcsinus", "sin⁻¹");
    }

    execute(x, angleUnit = 'DEG') {
        if (x < -1 || x > 1) {
            const { DomainMathError } = _scope.CalculatorErrors;
            throw new DomainMathError("sin⁻¹", "Valoarea pentru arcsin trebuie să fie în intervalul [-1, 1]!");
        }
        const rad = Math.asin(x);
        return this._fromRadians(rad, angleUnit);
    }
}

class AcosOperation extends BaseTrigOperation {
    constructor() {
        super("Arccosinus", "cos⁻¹");
    }

    execute(x, angleUnit = 'DEG') {
        if (x < -1 || x > 1) {
            const { DomainMathError } = _scope.CalculatorErrors;
            throw new DomainMathError("cos⁻¹", "Valoarea pentru arccos trebuie să fie în intervalul [-1, 1]!");
        }
        const rad = Math.acos(x);
        return this._fromRadians(rad, angleUnit);
    }
}

class AtanOperation extends BaseTrigOperation {
    constructor() {
        super("Arctangentă", "tan⁻¹");
    }

    execute(x, angleUnit = 'DEG') {
        const rad = Math.atan(x);
        return this._fromRadians(rad, angleUnit);
    }
}

    _scope.TrigOps = {
        SinOperation,
        CosOperation,
        TanOperation,
        AsinOperation,
        AcosOperation,
        AtanOperation
    };
})();
