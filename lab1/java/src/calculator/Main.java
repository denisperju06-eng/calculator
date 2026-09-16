package calculator;

import calculator.core.CalculatorEngine;
import calculator.errors.CalculatorException;

public class Main {
    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("   CALCULATOR DE BUZUNAR - PARADIGMA ORIENTATĂ PE OBIECTE (OOP) ");
        System.out.println("=================================================================");

        CalculatorEngine engine = new CalculatorEngine();

        // -------------------------------------------------------------
        // a. Operații aritmetice de bază pe numere reale
        // -------------------------------------------------------------
        System.out.println("\n--- [a] Operații aritmetice de bază pe numere reale ---");
        try {
            engine.setDisplay(12.5);
            engine.setBinaryOperator("+");
            double sum = engine.executeEquals(7.5);
            System.out.printf("  ✓ Adunare: 12.5 + 7.5 = %.2f\n", sum);

            engine.setDisplay(20.0);
            engine.setBinaryOperator("*");
            double prod = engine.executeEquals(3.0);
            System.out.printf("  ✓ Înmulțire: 20.0 * 3.0 = %.2f\n", prod);

            engine.setDisplay(60.0);
            engine.setBinaryOperator("/");
            double div = engine.executeEquals(4.0);
            System.out.printf("  ✓ Împărțire: 60.0 / 4.0 = %.2f\n", div);

            engine.setDisplay(15.0);
            engine.setBinaryOperator("-");
            double diff = engine.executeEquals(5.0);
            System.out.printf("  ✓ Scădere: 15.0 - 5.0 = %.2f\n", diff);

            engine.setDisplay(10.0);
            engine.setBinaryOperator("mod");
            double mod = engine.executeEquals(3.0);
            System.out.printf("  ✓ Modulo: 10 mod 3 = %.0f\n", mod);
        } catch (CalculatorException e) {
            System.err.println("  Eroare: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // b. Minim 3 operații adăugătoare (radical, procente, x^2 etc.)
        // -------------------------------------------------------------
        System.out.println("\n--- [b] Minim 3 operații adăugătoare ---");
        try {
            engine.setDisplay(16.0);
            double sqrt = engine.executeUnaryOperation("sqrt");
            System.out.printf("  ✓ 1. Radical: √16 = %.2f\n", sqrt);

            engine.setDisplay(5.0);
            double sqr = engine.executeUnaryOperation("sqr");
            System.out.printf("  ✓ 2. Pătrat: 5² = %.2f\n", sqr);

            engine.setDisplay(2.0);
            engine.setBinaryOperator("pow");
            double pow = engine.executeEquals(8.0);
            System.out.printf("  ✓ 3. Putere: 2^8 = %.2f\n", pow);

            engine.setDisplay(50.0);
            double pct = engine.executeUnaryOperation("pct");
            System.out.printf("  ✓ 4. Procent: 50%% = %.2f\n", pct);

            engine.setDisplay(4.0);
            double recip = engine.executeUnaryOperation("recip");
            System.out.printf("  ✓ 5. Invers: 1/4 = %.2f\n", recip);

            engine.setDisplay(5.0);
            double fact = engine.executeUnaryOperation("fact");
            System.out.printf("  ✓ 6. Factorial: 5! = %.0f\n", fact);
        } catch (CalculatorException e) {
            System.err.println("  Eroare: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // c. Minim 3 operații trigonometrice + Minim 3 operații logaritmice
        // -------------------------------------------------------------
        System.out.println("\n--- [c] Trigonometrice (DEG/RAD) & Logaritmice ---");
        try {
            engine.setUseDegrees(true);
            engine.setDisplay(90.0);
            System.out.printf("  ✓ sin(90°) = %.2f\n", engine.executeUnaryOperation("sin"));

            engine.setDisplay(0.0);
            System.out.printf("  ✓ cos(0°) = %.2f\n", engine.executeUnaryOperation("cos"));

            engine.setDisplay(45.0);
            System.out.printf("  ✓ tan(45°) = %.2f\n", engine.executeUnaryOperation("tan"));

            // Logaritmice
            engine.setDisplay(100.0);
            System.out.printf("  ✓ log10(100) = %.2f\n", engine.executeUnaryOperation("log"));

            engine.setDisplay(8.0);
            System.out.printf("  ✓ log2(8) = %.2f\n", engine.executeUnaryOperation("log2"));

            engine.setDisplay(Math.E);
            System.out.printf("  ✓ ln(e) = %.2f\n", engine.executeUnaryOperation("ln"));
        } catch (CalculatorException e) {
            System.err.println("  Eroare: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // d. Operații de lucru cu memoria (MC, MR, M+, M-, MS)
        // -------------------------------------------------------------
        System.out.println("\n--- [d] Operații de lucru cu memoria ---");
        var mem = engine.getMemory();
        mem.store(42.0);
        System.out.printf("  ✓ MS (Memory Store): stocat %.2f\n", mem.getValue());

        mem.add(8.0);
        System.out.printf("  ✓ M+ (Memory Add +8): %.2f\n", mem.getValue());

        mem.subtract(10.0);
        System.out.printf("  ✓ M- (Memory Subtract -10): %.2f\n", mem.getValue());

        System.out.printf("  ✓ MR (Memory Recall): %.2f\n", mem.recall());

        mem.clear();
        System.out.printf("  ✓ MC (Memory Clear): are valoare = %b\n", mem.hasValue());

        // -------------------------------------------------------------
        // e. Prelucrarea erorilor de calcule
        // -------------------------------------------------------------
        System.out.println("\n--- [e] Prelucrarea erorilor de calcule ---");

        // 1. Împărțire la zero
        try {
            engine.setDisplay(10.0);
            engine.setBinaryOperator("/");
            engine.executeEquals(0.0);
        } catch (CalculatorException e) {
            System.out.println("  ✓ Capturare excepție [Împărțire la zero]: " + e.getMessage());
        }

        // 2. Radical din număr negativ
        try {
            engine.setDisplay(-16.0);
            engine.executeUnaryOperation("sqrt");
        } catch (CalculatorException e) {
            System.out.println("  ✓ Capturare excepție [Radical din negativ]: " + e.getMessage());
        }

        // 3. Logaritm din non-pozitiv
        try {
            engine.setDisplay(0.0);
            engine.executeUnaryOperation("ln");
        } catch (CalculatorException e) {
            System.out.println("  ✓ Capturare excepție [Logaritm din zero]: " + e.getMessage());
        }

        // 4. Tangentă nedefinită (90 grade)
        try {
            engine.setUseDegrees(true);
            engine.setDisplay(90.0);
            engine.executeUnaryOperation("tan");
        } catch (CalculatorException e) {
            System.out.println("  ✓ Capturare excepție [Tangentă 90°]: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // f. Transformarea hexazecimala (sau din alt sistem)
        // -------------------------------------------------------------
        System.out.println("\n--- [f] Transformarea hexazecimală & alte sisteme ---");
        var conv = engine.getConverter();
        double decVal = 255.0;
        System.out.printf("  ✓ Valoare Zecimală: %.0f\n", decVal);
        System.out.println("    -> HEX: " + conv.toHex(decVal));
        System.out.println("    -> OCT: " + conv.toOct(decVal));
        System.out.println("    -> BIN: " + conv.toBin(decVal));

        try {
            double parsedHex = conv.parseToDecimal("1A3F", "HEX");
            System.out.printf("  ✓ Parse din HEX '1A3F' -> DEC: %.0f\n", parsedHex);
            double parsedBin = conv.parseToDecimal("10110", "BIN");
            System.out.printf("  ✓ Parse din BIN '10110' -> DEC: %.0f\n", parsedBin);
        } catch (CalculatorException e) {
            System.err.println("  Eroare conversie: " + e.getMessage());
        }

        System.out.println("\n=================================================================");
        System.out.println("  TOATE CELE 6 CERINȚE (a, b, c, d, e, f) SUNT IMPLEMENTATE 100%!");
        System.out.println("=================================================================");
    }
}
