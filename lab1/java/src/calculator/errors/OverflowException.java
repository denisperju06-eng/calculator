package calculator.errors;

public class OverflowException extends CalculatorException {
    public OverflowException() {
        super("Rezultatul depășește limita numerică admisă (Overflow)!");
    }

    public OverflowException(String message) {
        super(message);
    }
}
