package calculator.operations;

import calculator.errors.CalculatorException;
import calculator.errors.DomainMathException;
import calculator.errors.OverflowException;

public final class LogOperations {
    private LogOperations() {}

    public static class NaturalLogOperation extends UnaryOperation {
        public NaturalLogOperation() {
            super("Logaritm Natural", "ln");
        }

        @Override
        public double execute(double x) throws CalculatorException {
            if (x <= 0) {
                throw new DomainMathException("ln", "Logaritmul natural este definit doar pentru numere strict pozitive (x > 0)!");
            }
            return Math.log(x);
        }
    }

    public static class Log10Operation extends UnaryOperation {
        public Log10Operation() {
            super("Logaritm Baza 10", "log₁₀");
        }

        @Override
        public double execute(double x) throws CalculatorException {
            if (x <= 0) {
                throw new DomainMathException("log₁₀", "Logaritmul în baza 10 este definit doar pentru numere strict pozitive (x > 0)!");
            }
            return Math.log10(x);
        }
    }

    public static class Log2Operation extends UnaryOperation {
        public Log2Operation() {
            super("Logaritm Baza 2", "log₂");
        }

        @Override
        public double execute(double x) throws CalculatorException {
            if (x <= 0) {
                throw new DomainMathException("log₂", "Logaritmul în baza 2 este definit doar pentru numere strict pozitive (x > 0)!");
            }
            return Math.log(x) / Math.log(2);
        }
    }

    public static class ExpOperation extends UnaryOperation {
        public ExpOperation() {
            super("Exponențială eˣ", "eˣ");
        }

        @Override
        public double execute(double x) throws CalculatorException {
            double res = Math.exp(x);
            if (Double.isInfinite(res)) {
                throw new OverflowException();
            }
            return res;
        }
    }

    public static class TenPowerOperation extends UnaryOperation {
        public TenPowerOperation() {
            super("10 la puterea x", "10ˣ");
        }

        @Override
        public double execute(double x) throws CalculatorException {
            double res = Math.pow(10, x);
            if (Double.isInfinite(res)) {
                throw new OverflowException();
            }
            return res;
        }
    }
}
