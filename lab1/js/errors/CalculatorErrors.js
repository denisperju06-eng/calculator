/**
 * Ierarhia de erori OOP pentru Calculator
 * Demonstrează principiul de Moștenire (Inheritance) și Polimorfism.
 */

(() => {
    const _scope = typeof window !== 'undefined' ? window : (typeof global !== 'undefined' ? global : globalThis);

    class CalculatorError extends Error {
        constructor(message) {
            super(message);
            this.name = this.constructor.name;
            if (Error.captureStackTrace) {
                Error.captureStackTrace(this, this.constructor);
            }
        }

        getUserFriendlyMessage() {
            return `Eroare: ${this.message}`;
        }
    }

    class DivisionByZeroError extends CalculatorError {
        constructor(message = "Împărțirea la zero nu este permisă!") {
            super(message);
        }
    }

    class DomainMathError extends CalculatorError {
        constructor(operation, reason) {
            super(`Domeniu invalid pentru [${operation}]: ${reason}`);
            this.operation = operation;
            this.reason = reason;
        }
    }

    class OverflowError extends CalculatorError {
        constructor(message = "Rezultatul depășește limita numerică admisă (Overflow)!") {
            super(message);
        }
    }

    class InvalidBaseConversionError extends CalculatorError {
        constructor(value, base) {
            super(`Valoarea "${value}" nu este validă în baza ${base}!`);
        }
    }

    _scope.CalculatorError = CalculatorError;
    _scope.DivisionByZeroError = DivisionByZeroError;
    _scope.DomainMathError = DomainMathError;
    _scope.OverflowError = OverflowError;
    _scope.InvalidBaseConversionError = InvalidBaseConversionError;
    _scope.CalculatorErrors = {
        CalculatorError,
        DivisionByZeroError,
        DomainMathError,
        OverflowError,
        InvalidBaseConversionError
    };
})();
