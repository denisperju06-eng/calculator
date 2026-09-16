package calculator.operations;

import calculator.errors.CalculatorException;
import calculator.errors.DomainMathException;

public final class TrigOperations {
    private TrigOperations() {}

    public abstract static class BaseTrigOperation extends UnaryOperation {
        private boolean useDegrees = true;

        public BaseTrigOperation(String name, String symbol) {
            super(name, symbol);
        }

        public void setUseDegrees(boolean useDegrees) {
            this.useDegrees = useDegrees;
        }

        public boolean isUseDegrees() {
            return useDegrees;
        }

        protected double toRadians(double angle) {
            return useDegrees ? Math.toRadians(angle) : angle;
        }

        protected double fromRadians(double rad) {
            return useDegrees ? Math.toDegrees(rad) : rad;
        }
    }

    public static class SinOperation extends BaseTrigOperation {
        public SinOperation() {
            super("Sinus", "sin");
        }

        @Override
        public double execute(double x) {
            double rad = toRadians(x);
            double res = Math.sin(rad);
            return Math.abs(res) < 1e-15 ? 0.0 : res;
        }
    }

    public static class CosOperation extends BaseTrigOperation {
        public CosOperation() {
            super("Cosinus", "cos");
        }

        @Override
        public double execute(double x) {
            double rad = toRadians(x);
            double res = Math.cos(rad);
            return Math.abs(res) < 1e-15 ? 0.0 : res;
        }
    }

    public static class TanOperation extends BaseTrigOperation {
        public TanOperation() {
            super("Tangentă", "tan");
        }

        @Override
        public double execute(double x) throws CalculatorException {
            double rad = toRadians(x);
            double cosVal = Math.cos(rad);
            if (Math.abs(cosVal) < 1e-12) {
                throw new DomainMathException("tan", "Tangenta este nedefinită pentru unghiul " + x + (isUseDegrees() ? "°" : " rad") + " (cosinus = 0)!");
            }
            double res = Math.tan(rad);
            return Math.abs(res) < 1e-15 ? 0.0 : res;
        }
    }

    public static class AsinOperation extends BaseTrigOperation {
        public AsinOperation() {
            super("Arcsinus", "sin⁻¹");
        }

        @Override
        public double execute(double x) throws CalculatorException {
            if (x < -1.0 || x > 1.0) {
                throw new DomainMathException("sin⁻¹", "Valoarea trebuie să fie în intervalul [-1, 1]!");
            }
            return fromRadians(Math.asin(x));
        }
    }

    public static class AcosOperation extends BaseTrigOperation {
        public AcosOperation() {
            super("Arccosinus", "cos⁻¹");
        }

        @Override
        public double execute(double x) throws CalculatorException {
            if (x < -1.0 || x > 1.0) {
                throw new DomainMathException("cos⁻¹", "Valoarea trebuie să fie în intervalul [-1, 1]!");
            }
            return fromRadians(Math.acos(x));
        }
    }

    public static class AtanOperation extends BaseTrigOperation {
        public AtanOperation() {
            super("Arctangentă", "tan⁻¹");
        }

        @Override
        public double execute(double x) {
            return fromRadians(Math.atan(x));
        }
    }
}
