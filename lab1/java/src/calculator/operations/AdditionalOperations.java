package calculator.operations;

import calculator.errors.CalculatorException;
import calculator.errors.DivisionByZeroException;
import calculator.errors.DomainMathException;
import calculator.errors.OverflowException;

public final class AdditionalOperations {
    private AdditionalOperations() {}

    public static class SquareRootOperation extends UnaryOperation {
        public SquareRootOperation() {
            super("Radical de ordin 2", "√");
        }

        @Override
        public double execute(double x) throws CalculatorException {
            if (x < 0) {
                throw new DomainMathException("Radical", "Numărul trebuie să fie pozitiv sau zero (x >= 0)!");
            }
            return Math.sqrt(x);
        }
    }

    public static class CubeRootOperation extends UnaryOperation {
        public CubeRootOperation() {
            super("Radical de ordin 3", "∛");
        }

        @Override
        public double execute(double x) {
            return Math.cbrt(x);
        }
    }

    public static class SquareOperation extends UnaryOperation {
        public SquareOperation() {
            super("Ridicarea la pătrat", "x²");
        }

        @Override
        public double execute(double x) throws CalculatorException {
            double res = x * x;
            if (Double.isInfinite(res)) {
                throw new OverflowException();
            }
            return res;
        }
    }

    public static class PowerOperation extends BinaryOperation {
        public PowerOperation() {
            super("Ridicarea la putere", "xʸ");
        }

        @Override
        public double execute(double x, double y) throws CalculatorException {
            if (x < 0 && Math.floor(y) != y) {
                throw new DomainMathException("xʸ", "Baza negativă la exponent fracționar nu dă un număr real!");
            }
            double res = Math.pow(x, y);
            if (Double.isInfinite(res)) {
                throw new OverflowException();
            }
            return res;
        }
    }

    public static class PercentageOperation extends UnaryOperation {
        public PercentageOperation() {
            super("Procent", "%");
        }

        @Override
        public double execute(double x) {
            return x / 100.0;
        }
    }

    public static class ReciprocalOperation extends UnaryOperation {
        public ReciprocalOperation() {
            super("Invers", "1/x");
        }

        @Override
        public double execute(double x) throws CalculatorException {
            if (x == 0.0) {
                throw new DivisionByZeroException("Nu se poate calcula inversul lui zero (1/0)!");
            }
            return 1.0 / x;
        }
    }

    public static class AbsoluteOperation extends UnaryOperation {
        public AbsoluteOperation() {
            super("Modul", "|x|");
        }

        @Override
        public double execute(double x) {
            return Math.abs(x);
        }
    }

    public static class FactorialOperation extends UnaryOperation {
        public FactorialOperation() {
            super("Factorial", "n!");
        }

        @Override
        public double execute(double x) throws CalculatorException {
            if (x < 0 || Math.floor(x) != x) {
                throw new DomainMathException("Factorial", "Factorialul este definit doar pentru numere naturale întregi (n >= 0)!");
            }
            if (x > 170) {
                throw new OverflowException("Factorialul depășește limita reprezentabilă pentru tipul double (n > 170)!");
            }
            double res = 1.0;
            long n = (long) x;
            for (long i = 2; i <= n; i++) {
                res *= i;
            }
            return res;
        }
    }

    public static class NegateOperation extends UnaryOperation {
        public NegateOperation() {
            super("Schimbare semn", "±");
        }

        @Override
        public double execute(double x) {
            return -x;
        }
    }
}
