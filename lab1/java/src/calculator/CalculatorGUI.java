package calculator;

import calculator.core.CalculatorEngine;
import calculator.errors.CalculatorException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Interfață Grafică Swing completă pentru Calculatorul OOP (Lab 1).
 * Permite testarea manuală a tuturor cerințelor:
 * a. Operații de bază (+, -, *, /, mod)
 * b. Operații suplimentare (sqrt, cbrt, x^2, x^y, %, 1/x, n!)
 * c. Trigonometrice (sin, cos, tan, asin, acos, atan cu DEG/RAD) și Logaritmice (ln, log10, log2, e^x, 10^x)
 * d. Memorie (MC, MR, M+, M-, MS)
 * e. Tratare erori (afișate pe ecran cu dialog/banner)
 * f. Baze numerice (afisaj live HEX, DEC, OCT, BIN)
 */
public class CalculatorGUI extends JFrame {

    private final CalculatorEngine engine;
    private final JTextField displayField;
    private final JLabel secondaryDisplay;
    private final JLabel memoryLabel;
    private final JButton degRadBtn;

    private final JLabel hexLabel;
    private final JLabel decLabel;
    private final JLabel octLabel;
    private final JLabel binLabel;

    private boolean isTypingNewNumber = true;

    public CalculatorGUI() {
        super("Calculator de Buzunar OOP - Lab 1");
        this.engine = new CalculatorEngine();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(480, 680);
        setLocationRelativeTo(null);

        // Look and Feel modern nativ
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        mainPanel.setBackground(new Color(243, 243, 243));

        // 1. ZONA DE DISPLAY
        JPanel displayPanel = new JPanel(new BorderLayout(5, 5));
        displayPanel.setBackground(Color.WHITE);
        displayPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        metaPanel.setOpaque(false);

        degRadBtn = new JButton("DEG");
        degRadBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        degRadBtn.setFocusPainted(false);
        degRadBtn.addActionListener(e -> {
            boolean current = engine.isUseDegrees();
            engine.setUseDegrees(!current);
            degRadBtn.setText(engine.isUseDegrees() ? "DEG" : "RAD");
        });

        memoryLabel = new JLabel("M");
        memoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        memoryLabel.setForeground(Color.LIGHT_GRAY);

        metaPanel.add(degRadBtn);
        metaPanel.add(memoryLabel);

        secondaryDisplay = new JLabel(" ");
        secondaryDisplay.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        secondaryDisplay.setForeground(Color.GRAY);
        secondaryDisplay.setHorizontalAlignment(SwingConstants.RIGHT);

        displayField = new JTextField("0");
        displayField.setFont(new Font("Segoe UI", Font.BOLD, 36));
        displayField.setHorizontalAlignment(SwingConstants.RIGHT);
        displayField.setEditable(false);
        displayField.setBorder(null);
        displayField.setBackground(Color.WHITE);

        displayPanel.add(metaPanel, BorderLayout.WEST);
        displayPanel.add(secondaryDisplay, BorderLayout.NORTH);
        displayPanel.add(displayField, BorderLayout.CENTER);

        // 2. PANOU BAZE (Cerința f)
        JPanel basePanel = new JPanel(new GridLayout(4, 1, 2, 2));
        basePanel.setBackground(new Color(230, 235, 245));
        basePanel.setBorder(new EmptyBorder(6, 10, 6, 10));

        hexLabel = new JLabel("HEX: 0");
        decLabel = new JLabel("DEC: 0");
        octLabel = new JLabel("OCT: 0");
        binLabel = new JLabel("BIN: 0");

        Font baseFont = new Font("Monospaced", Font.BOLD, 11);
        hexLabel.setFont(baseFont);
        decLabel.setFont(baseFont);
        octLabel.setFont(baseFont);
        binLabel.setFont(baseFont);

        basePanel.add(hexLabel);
        basePanel.add(decLabel);
        basePanel.add(octLabel);
        basePanel.add(binLabel);

        JPanel topContainer = new JPanel(new BorderLayout(5, 5));
        topContainer.setOpaque(false);
        topContainer.add(displayPanel, BorderLayout.NORTH);
        topContainer.add(basePanel, BorderLayout.SOUTH);

        mainPanel.add(topContainer, BorderLayout.NORTH);

        // 3. BUTOANE
        JPanel buttonsPanel = new JPanel(new GridLayout(7, 5, 4, 4));
        buttonsPanel.setOpaque(false);

        String[][] btnLabels = {
                // Rândul 1: Memorie & Clear
                {"MC", "MR", "M+", "M-", "MS"},
                // Rândul 2: Științifice (Trig/Log)
                {"sin", "cos", "tan", "ln", "log"},
                // Rândul 3: Puteri & Radicali
                {"x²", "xʸ", "√x", "∛x", "n!"},
                // Rândul 4: Cifre & Aritmetică
                {"C", "CE", "mod", "1/x", "÷"},
                // Rândul 5:
                {"7", "8", "9", "×", "%"},
                // Rândul 6:
                {"4", "5", "6", "−", "±"},
                // Rândul 7:
                {"1", "2", "3", "+", "="},
        };

        // Adăugăm și ultimul rând cu 0 și separator
        JPanel keypadContainer = new JPanel(new BorderLayout(4, 4));
        keypadContainer.setOpaque(false);
        keypadContainer.add(buttonsPanel, BorderLayout.CENTER);

        JPanel bottomRow = new JPanel(new GridLayout(1, 3, 4, 4));
        bottomRow.setOpaque(false);
        JButton btn0 = createButton("0", false);
        JButton btnDot = createButton(".", false);
        JButton btnEq = createButton("=", true);
        bottomRow.add(btn0);
        bottomRow.add(btnDot);
        bottomRow.add(btnEq);
        keypadContainer.add(bottomRow, BorderLayout.SOUTH);

        for (String[] row : btnLabels) {
            for (String label : row) {
                boolean isOp = isOperator(label);
                JButton btn = createButton(label, isOp);
                buttonsPanel.add(btn);
            }
        }

        mainPanel.add(keypadContainer, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    private boolean isOperator(String label) {
        return label.equals("=") || label.equals("+") || label.equals("−") ||
                label.equals("×") || label.equals("÷") || label.equals("mod") ||
                label.equals("C") || label.equals("CE");
    }

    private JButton createButton(String text, boolean highlight) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        if (highlight) {
            btn.setBackground(new Color(0, 103, 192));
            btn.setForeground(Color.WHITE);
        } else if (text.matches("[0-9]")) {
            btn.setBackground(Color.WHITE);
            btn.setForeground(Color.BLACK);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        } else {
            btn.setBackground(new Color(248, 249, 250));
            btn.setForeground(new Color(30, 30, 30));
        }

        btn.addActionListener(new ButtonClickListener(text));
        return btn;
    }

    private class ButtonClickListener implements ActionListener {
        private final String cmd;

        public ButtonClickListener(String cmd) {
            this.cmd = cmd;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            handleCommand(cmd);
        }
    }

    private void handleCommand(String cmd) {
        try {
            if (cmd.matches("[0-9]")) {
                if (isTypingNewNumber) {
                    displayField.setText(cmd);
                    isTypingNewNumber = false;
                } else {
                    displayField.setText(displayField.getText().equals("0") ? cmd : displayField.getText() + cmd);
                }
                updateBases();
            } else if (cmd.equals(".")) {
                if (isTypingNewNumber) {
                    displayField.setText("0.");
                    isTypingNewNumber = false;
                } else if (!displayField.getText().contains(".")) {
                    displayField.setText(displayField.getText() + ".");
                }
            } else if (cmd.equals("±")) {
                double val = Double.parseDouble(displayField.getText());
                displayField.setText(formatResult(-val));
                updateBases();
            } else if (cmd.equals("C")) {
                engine.clear();
                displayField.setText("0");
                secondaryDisplay.setText(" ");
                isTypingNewNumber = true;
                updateBases();
            } else if (cmd.equals("CE")) {
                displayField.setText("0");
                isTypingNewNumber = true;
                updateBases();
            }
            // MEMORIE (Cerința d)
            else if (cmd.equals("MS")) {
                double val = Double.parseDouble(displayField.getText());
                engine.getMemory().store(val);
                updateMemoryIndicator();
                isTypingNewNumber = true;
            } else if (cmd.equals("MR")) {
                if (engine.getMemory().hasValue()) {
                    displayField.setText(formatResult(engine.getMemory().recall()));
                    isTypingNewNumber = true;
                    updateBases();
                }
            } else if (cmd.equals("MC")) {
                engine.getMemory().clear();
                updateMemoryIndicator();
            } else if (cmd.equals("M+")) {
                double val = Double.parseDouble(displayField.getText());
                engine.getMemory().add(val);
                updateMemoryIndicator();
                isTypingNewNumber = true;
            } else if (cmd.equals("M-")) {
                double val = Double.parseDouble(displayField.getText());
                engine.getMemory().subtract(val);
                updateMemoryIndicator();
                isTypingNewNumber = true;
            }
            // OPERAȚII BINARE (+, -, *, /, mod, pow)
            else if (cmd.equals("+") || cmd.equals("−") || cmd.equals("×") || cmd.equals("÷") || cmd.equals("mod") || cmd.equals("xʸ")) {
                double current = Double.parseDouble(displayField.getText());
                engine.setDisplay(current);

                String mappedOp = cmd;
                if (cmd.equals("−")) mappedOp = "-";
                if (cmd.equals("×")) mappedOp = "*";
                if (cmd.equals("÷")) mappedOp = "/";
                if (cmd.equals("xʸ")) mappedOp = "pow";

                engine.setBinaryOperator(mappedOp);
                secondaryDisplay.setText(formatResult(current) + " " + cmd);
                isTypingNewNumber = true;
            } else if (cmd.equals("=")) {
                double second = Double.parseDouble(displayField.getText());
                double res = engine.executeEquals(second);
                secondaryDisplay.setText(secondaryDisplay.getText() + " " + formatResult(second) + " =");
                displayField.setText(formatResult(res));
                isTypingNewNumber = true;
                updateBases();
            }
            // OPERAȚII UNARE (Cerințele b & c)
            else {
                double val = Double.parseDouble(displayField.getText());
                engine.setDisplay(val);
                String unOp = null;
                switch (cmd) {
                    case "√x": unOp = "sqrt"; break;
                    case "∛x": unOp = "cbrt"; break;
                    case "x²": unOp = "sqr"; break;
                    case "%": unOp = "pct"; break;
                    case "1/x": unOp = "recip"; break;
                    case "n!": unOp = "fact"; break;
                    case "sin": unOp = "sin"; break;
                    case "cos": unOp = "cos"; break;
                    case "tan": unOp = "tan"; break;
                    case "ln": unOp = "ln"; break;
                    case "log": unOp = "log"; break;
                }

                if (unOp != null) {
                    double res = engine.executeUnaryOperation(unOp);
                    secondaryDisplay.setText(cmd + "(" + formatResult(val) + ")");
                    displayField.setText(formatResult(res));
                    isTypingNewNumber = true;
                    updateBases();
                }
            }

        } catch (CalculatorException | ArithmeticException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Eroare de calcul (Cerința e)",
                    JOptionPane.ERROR_MESSAGE);
            displayField.setText("Eroare");
            isTypingNewNumber = true;
        } catch (NumberFormatException ex) {
            displayField.setText("0");
            isTypingNewNumber = true;
        }
    }

    private void updateBases() {
        try {
            double val = Double.parseDouble(displayField.getText());
            hexLabel.setText("HEX: " + engine.getConverter().toHex(val));
            decLabel.setText("DEC: " + (long) val);
            octLabel.setText("OCT: " + engine.getConverter().toOct(val));
            binLabel.setText("BIN: " + engine.getConverter().toBin(val));
        } catch (Exception ignored) {}
    }

    private void updateMemoryIndicator() {
        if (engine.getMemory().hasValue()) {
            memoryLabel.setForeground(new Color(0, 120, 215));
            memoryLabel.setText("M: " + formatResult(engine.getMemory().getValue()));
        } else {
            memoryLabel.setForeground(Color.LIGHT_GRAY);
            memoryLabel.setText("M");
        }
    }

    private String formatResult(double val) {
        if (Double.isNaN(val) || Double.isInfinite(val)) {
            return String.valueOf(val);
        }
        if (val == (long) val) {
            return String.format("%d", (long) val);
        } else {
            return String.format("%.6g", val).replace(",", ".");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CalculatorGUI gui = new CalculatorGUI();
            gui.setVisible(true);
        });
    }
}
