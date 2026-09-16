package calculator.core;

import calculator.errors.CalculatorException;
import calculator.operations.*;
import java.util.HashMap;
import java.util.Map;

public class CalculatorEngine {
    private double currentDisplay = 0.0;
    private Double firstOperand = null;
    private String pendingBinaryOp = null;
    private boolean useDegrees = true;

    private final MemoryUnit memoryUnit;
    private final BaseConverter baseConverter;

    private final Map<String, BinaryOperation> binaryOperations = new HashMap<>();
    private final Map<String, UnaryOperation> unaryOperations = new HashMap<>();

    public CalculatorEngine() {
        this.memoryUnit = new MemoryUnit();
        this.baseConverter = new BaseConverter();
        initOperations();
    }

    private void initOperations() {
        // Cerința a: Operații aritmetice de bază
        binaryOperations.put("+", new ArithmeticOperations.AddOperation());
        binaryOperations.put("-", new ArithmeticOperations.SubtractOperation());
        binaryOperations.put("*", new ArithmeticOperations.MultiplyOperation());
        binaryOperations.put("/", new ArithmeticOperations.DivideOperation());
        binaryOperations.put("mod", new ArithmeticOperations.ModuloOperation());

        // Cerința b: Operații adăugătoare
        unaryOperations.put("sqrt", new AdditionalOperations.SquareRootOperation());
        unaryOperations.put("cbrt", new AdditionalOperations.CubeRootOperation());
        unaryOperations.put("sqr", new AdditionalOperations.SquareOperation());
        binaryOperations.put("pow", new AdditionalOperations.PowerOperation());
        unaryOperations.put("pct", new AdditionalOperations.PercentageOperation());
        unaryOperations.put("recip", new AdditionalOperations.ReciprocalOperation());
        unaryOperations.put("abs", new AdditionalOperations.AbsoluteOperation());
        unaryOperations.put("fact", new AdditionalOperations.FactorialOperation());
        unaryOperations.put("neg", new AdditionalOperations.NegateOperation());

        // Cerința c: Trigonometrice
        unaryOperations.put("sin", new TrigOperations.SinOperation());
        unaryOperations.put("cos", new TrigOperations.CosOperation());
        unaryOperations.put("tan", new TrigOperations.TanOperation());
        unaryOperations.put("asin", new TrigOperations.AsinOperation());
        unaryOperations.put("acos", new TrigOperations.AcosOperation());
        unaryOperations.put("atan", new TrigOperations.AtanOperation());

        // Cerința c: Logaritmice
        unaryOperations.put("ln", new LogOperations.NaturalLogOperation());
        unaryOperations.put("log", new LogOperations.Log10Operation());
        unaryOperations.put("log2", new LogOperations.Log2Operation());
        unaryOperations.put("exp", new LogOperations.ExpOperation());
        unaryOperations.put("10x", new LogOperations.TenPowerOperation());
    }

    public void setDisplay(double val) {
        this.currentDisplay = val;
    }

    public double getDisplay() {
        return this.currentDisplay;
    }

    public void setUseDegrees(boolean useDegrees) {
        this.useDegrees = useDegrees;
        for (UnaryOperation op : unaryOperations.values()) {
            if (op instanceof TrigOperations.BaseTrigOperation) {
                ((TrigOperations.BaseTrigOperation) op).setUseDegrees(useDegrees);
            }
        }
    }

    public boolean isUseDegrees() {
        return useDegrees;
    }

    public void setBinaryOperator(String op) {
        this.firstOperand = this.currentDisplay;
        this.pendingBinaryOp = op;
    }

    public double executeEquals(double secondOperand) throws CalculatorException {
        if (this.pendingBinaryOp == null || this.firstOperand == null) {
            return this.currentDisplay;
        }

        BinaryOperation op = binaryOperations.get(this.pendingBinaryOp);
        if (op == null) {
            throw new CalculatorException("Operator necunoscut: " + this.pendingBinaryOp);
        }

        double result = op.execute(this.firstOperand, secondOperand);
        this.currentDisplay = result;
        this.firstOperand = null;
        this.pendingBinaryOp = null;
        return result;
    }

    public double executeUnaryOperation(String opKey) throws CalculatorException {
        UnaryOperation op = unaryOperations.get(opKey);
        if (op == null) {
            throw new CalculatorException("Operație unară necunoscută: " + opKey);
        }

        double result = op.execute(this.currentDisplay);
        this.currentDisplay = result;
        return result;
    }

    // Memorie (Cerința d)
    public MemoryUnit getMemory() {
        return this.memoryUnit;
    }

    // Baze (Cerința f)
    public BaseConverter getConverter() {
        return this.baseConverter;
    }

    public void clear() {
        this.currentDisplay = 0.0;
        this.firstOperand = null;
        this.pendingBinaryOp = null;
    }
}
