package calculator.operations;

import calculator.errors.CalculatorException;

public abstract class UnaryOperation extends AbstractOperation {
    public UnaryOperation(String name, String symbol) {
        super(name, symbol);
    }

    public abstract double execute(double x) throws CalculatorException;
}
