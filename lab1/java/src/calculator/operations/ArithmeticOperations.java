package calculator.operations;

import calculator.errors.CalculatorException;
import calculator.errors.DivisionByZeroException;

public final class ArithmeticOperations {
    private ArithmeticOperations() {}

    public static class AddOperation extends BinaryOperation {
        public AddOperation() {
            super("Adunare", "+");
        }

        @Override
        public double execute(double a, double b) {
            return a + b;
        }
    }

    public static class SubtractOperation extends BinaryOperation {
        public SubtractOperation() {
            super("Scădere", "−");
        }

        @Override
        public double execute(double a, double b) {
            return a - b;
        }
    }

    public static class MultiplyOperation extends BinaryOperation {
        public MultiplyOperation() {
            super("Înmulțire", "×");
        }

        @Override
        public double execute(double a, double b) {
            return a * b;
        }
    }

    public static class DivideOperation extends BinaryOperation {
        public DivideOperation() {
            super("Împărțire", "÷");
        }

        @Override
        public double execute(double a, double b) throws CalculatorException {
            if (b == 0.0 || Math.abs(b) < 1e-15) {
                throw new DivisionByZeroException();
            }
            return a / b;
        }
    }

    public static class ModuloOperation extends BinaryOperation {
        public ModuloOperation() {
            super("Modulo", "mod");
        }

        @Override
        public double execute(double a, double b) throws CalculatorException {
            if (b == 0.0) {
                throw new DivisionByZeroException("Nu se poate calcula modulo cu împărțitor zero!");
            }
            return a % b;
        }
    }
}
