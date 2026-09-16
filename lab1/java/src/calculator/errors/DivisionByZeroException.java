package calculator.errors;

public class DivisionByZeroException extends CalculatorException {
    public DivisionByZeroException() {
        super("Nu se poate împărți la zero!");
    }
    
    public DivisionByZeroException(String message) {
        super(message);
    }
}
