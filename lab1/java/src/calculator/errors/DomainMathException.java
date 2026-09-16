package calculator.errors;

public class DomainMathException extends CalculatorException {
    private final String operation;
    private final String reason;

    public DomainMathException(String operation, String reason) {
        super("Domeniu invalid pentru [" + operation + "]: " + reason);
        this.operation = operation;
        this.reason = reason;
    }

    public String getOperation() {
        return operation;
    }

    public String getReason() {
        return reason;
    }
}
