/**
 * Punctul de intrare (Bootstrap) al aplicației Calculator.
 */
(() => {
    function initApp() {
        try {
            const engine = new window.CalculatorEngine();
            const ui = new window.CalculatorUI(engine);
            window.calculatorInstance = { engine, ui };
            console.log("Calculator OOP inițializat cu succes conform cerințelor!");
        } catch (e) {
            console.error("Eroare la inițializarea aplicației:", e);
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initApp);
    } else {
        initApp();
    }
})();
