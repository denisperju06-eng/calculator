/**
 * Clase de bază abstracte pentru Operații conform principiilor OOP:
 * - Abstractizare (Abstraction)
 * - Polimorfism (Polymorphism)
 */

(() => {
    const _scope = typeof window !== 'undefined' ? window : (typeof global !== 'undefined' ? global : globalThis);

    class AbstractOperation {
        constructor(name, symbol) {
            if (new.target === AbstractOperation) {
                throw new TypeError("Nu se poate instanția direct clasa abstractă AbstractOperation!");
            }
            this._name = name;
            this._symbol = symbol;
        }

        getName() {
            return this._name;
        }

        getSymbol() {
            return this._symbol;
        }

        /**
         * Metodă abstractă ce trebuie suprascrisă în clasele derivate
         */
        execute(...args) {
            throw new Error(`Metoda abstractă execute() trebuie implementată în subclasa ${this.constructor.name}!`);
        }
    }

    /**
     * Operație binară (necesită 2 operanzi: stânga și dreapta)
     */
    class BinaryOperation extends AbstractOperation {
        constructor(name, symbol) {
            super(name, symbol);
            if (new.target === BinaryOperation) {
                throw new TypeError("Nu se poate instanția direct clasa abstractă BinaryOperation!");
            }
        }

        /**
         * @param {number} left
         * @param {number} right
         * @returns {number}
         */
        execute(left, right) {
            throw new Error("Metoda execute(left, right) trebuie implementată de clasa derivată!");
        }
    }

    /**
     * Operație unară (se aplică pe un singur operand curent)
     */
    class UnaryOperation extends AbstractOperation {
        constructor(name, symbol) {
            super(name, symbol);
            if (new.target === UnaryOperation) {
                throw new TypeError("Nu se poate instanția direct clasa abstractă UnaryOperation!");
            }
        }

        /**
         * @param {number} operand
         * @param {string} [angleUnit='DEG'] (pentru funcții trigonometrice)
         * @returns {number}
         */
        execute(operand, angleUnit = 'DEG') {
            throw new Error("Metoda execute(operand) trebuie implementată de clasa derivată!");
        }
    }

    _scope.AbstractOperation = AbstractOperation;
    _scope.BinaryOperation = BinaryOperation;
    _scope.UnaryOperation = UnaryOperation;
})();
