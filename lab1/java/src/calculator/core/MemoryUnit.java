package calculator.core;

/**
 * Cerința (d): Operații de lucru cu memoria (MC, MR, M+, M-, MS)
 * Demonstrează principiul de Încapsulare (Encapsulation).
 */
public class MemoryUnit {
    private double currentMemory = 0.0;
    private boolean hasStoredValue = false;

    public MemoryUnit() {
        clear();
    }

    public void clear() {
        this.currentMemory = 0.0;
        this.hasStoredValue = false;
    }

    public double recall() {
        return this.currentMemory;
    }

    public void store(double value) {
        this.currentMemory = value;
        this.hasStoredValue = true;
    }

    public void add(double value) {
        this.currentMemory += value;
        this.hasStoredValue = true;
    }

    public void subtract(double value) {
        this.currentMemory -= value;
        this.hasStoredValue = true;
    }

    public boolean hasValue() {
        return this.hasStoredValue;
    }

    public double getValue() {
        return this.currentMemory;
    }
}
