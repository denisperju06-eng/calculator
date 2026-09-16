package calculator.operations;

import calculator.errors.CalculatorException;

public abstract class BinaryOperation extends AbstractOperation {
    public BinaryOperation(String name, String symbol) {
        super(name, symbol);
    }

    public abstract double execute(double a, double b) throws CalculatorException;
}
