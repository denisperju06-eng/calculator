(() => {
    const _scope = typeof window !== 'undefined' ? window : (typeof global !== 'undefined' ? global : globalThis);

    class MemoryUnit {
    // Câmpuri private conform standardului modern ECMAScript OOP (#)
    #currentValue = 0;
    #hasStoredValue = false;
    #history = [];

    constructor() {
        this.clear();
    }

    /**
     * MC - Memory Clear: Resetează memoria la 0 și golește starea
     */
    clear() {
        this.#currentValue = 0;
        this.#hasStoredValue = false;
        this.#history = [];
        return 0;
    }

    /**
     * MR - Memory Recall: Returnează valoarea curentă din memorie
     * @returns {number}
     */
    recall() {
        return this.#currentValue;
    }

    /**
     * MS - Memory Store: Salvează o nouă valoare în memorie
     * @param {number} value
     */
    store(value) {
        if (!Number.isFinite(value)) {
            const { CalculatorError } = _scope.CalculatorErrors;
            throw new CalculatorError("Nu se poate salva în memorie o valoare invalidă!");
        }
        this.#currentValue = value;
        this.#hasStoredValue = true;
        this.#history.unshift({ action: 'MS', value, timestamp: new Date() });
        return this.#currentValue;
    }

    /**
     * M+ - Memory Add: Adaugă valoarea specificată la conținutul curent al memoriei
     * @param {number} value
     */
    add(value) {
        if (!Number.isFinite(value)) return this.#currentValue;
        this.#currentValue += value;
        this.#hasStoredValue = true;
        this.#history.unshift({ action: 'M+', value, total: this.#currentValue, timestamp: new Date() });
        return this.#currentValue;
    }

    /**
     * M- - Memory Subtract: Scade valoarea specificată din conținutul memoriei
     * @param {number} value
     */
    subtract(value) {
        if (!Number.isFinite(value)) return this.#currentValue;
        this.#currentValue -= value;
        this.#hasStoredValue = true;
        this.#history.unshift({ action: 'M-', value, total: this.#currentValue, timestamp: new Date() });
        return this.#currentValue;
    }

    /**
     * Verifică dacă există o valoare stocată în memorie (pentru activarea butonului MR/MC)
     * @returns {boolean}
     */
    hasValue() {
        return this.#hasStoredValue;
    }

    /**
     * Getter pentru valoarea curentă
     */
    getValue() {
        return this.#currentValue;
    }

    /**
     * Getter pentru istoricul memoriei
     */
    getHistory() {
        return [...this.#history];
    }
}

    _scope.MemoryUnit = MemoryUnit;
})();
