package calculator.core;

import calculator.errors.CalculatorException;
import calculator.operations.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Clasa CalculatorEngine reprezintă motorul principal de procesare al aplicației (POO).
 * Rolul său este să centralizeze și să gestioneze executarea operațiilor matematice
 * (binare, unare, trigonometrice, logaritmice), sistemul de memorie și convertorul de baze.
 * Instanțiază dinamic operațiile într-o colecție map pentru a asigura polimorfismul.
 */
public class CalculatorEngine {
    // Variabile care stochează starea curentă a afișajului și a operației în așteptare
    private double currentDisplay = 0.0;
    private Double firstOperand = null;
    private String pendingBinaryOp = null;
    private boolean useDegrees = true;

    // Componente pentru lucrul cu memoria (MC, MR, M+, etc.) și conversii (HEX/DEC/OCT/BIN)
    private final MemoryUnit memoryUnit;
    private final BaseConverter baseConverter;

    // Dicționare ce mapează simbolurile operațiilor la obiectele operațiilor specifice
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

    /**
     * Stabilește operatorul binar și reține valoarea primului operand din afișaj.
     *
     * @param op Simbolul operatorului binar în așteptare (ex: "+", "-", "*")
     */
    public void setBinaryOperator(String op) {
        this.firstOperand = this.currentDisplay;
        this.pendingBinaryOp = op;
    }

    /**
     * Execută operația binară în așteptare cu al doilea operand și restabilește starea.
     *
     * @param secondOperand Al doilea operand (numărul curent afișat)
     * @return Rezultatul operației
     * @throws CalculatorException Dacă operatorul este invalid sau apare o eroare de calcul
     */
    public double executeEquals(double secondOperand) throws CalculatorException {
        // Dacă nu avem o operație salvată, rezultatul este valoarea curentă
        if (this.pendingBinaryOp == null || this.firstOperand == null) {
            return this.currentDisplay;
        }

        // Extrage obiectul operației pe baza polimorfismului
        BinaryOperation op = binaryOperations.get(this.pendingBinaryOp);
        if (op == null) {
            throw new CalculatorException("Operator necunoscut: " + this.pendingBinaryOp);
        }

        // Execută operația și setează noul display
        double result = op.execute(this.firstOperand, secondOperand);
        this.currentDisplay = result;
        
        // Golește starea operației în așteptare
        this.firstOperand = null;
        this.pendingBinaryOp = null;
        return result;
    }

    /**
     * Execută o operație unară (cu un singur operand) pe afișajul curent.
     *
     * @param opKey Numele/simbolul operației (ex: "sqrt", "sin")
     * @return Rezultatul operației unare
     * @throws CalculatorException Dacă operația eșuează din cauza domeniului matematic etc.
     */
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
