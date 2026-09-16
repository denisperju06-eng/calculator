package calculator.operations;

public abstract class AbstractOperation implements IOperation {
    protected final String name;
    protected final String symbol;

    public AbstractOperation(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSymbol() {
        return symbol;
    }
}
