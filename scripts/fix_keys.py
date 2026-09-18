import re

with open('lab1/java/src/calculator/CalculatorGUI.java', 'r') as f:
    content = f.read()

# Add setupKeyBindings call in constructor
content = content.replace("setVisible(true);", "setupKeyBindings();\n        setVisible(true);")

# Add the setupKeyBindings method before the ButtonClickListener class
key_bindings = """
    private void setupKeyBindings() {
        JRootPane rootPane = this.getRootPane();
        InputMap im = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = rootPane.getActionMap();

        // Cifre 0-9
        for (int i = 0; i <= 9; i++) {
            final String digit = String.valueOf(i);
            im.put(KeyStroke.getKeyStroke(digit), "digit" + digit);
            im.put(KeyStroke.getKeyStroke("NUMPAD" + digit), "digit" + digit);
            am.put("digit" + digit, new AbstractAction() {
                public void actionPerformed(ActionEvent e) { handleCommand(digit); }
            });
        }

        // Operatori
        im.put(KeyStroke.getKeyStroke('+'), "add");
        im.put(KeyStroke.getKeyStroke("ADD"), "add");
        am.put("add", new AbstractAction() { public void actionPerformed(ActionEvent e) { handleCommand("+"); } });

        im.put(KeyStroke.getKeyStroke('-'), "sub");
        im.put(KeyStroke.getKeyStroke("SUBTRACT"), "sub");
        am.put("sub", new AbstractAction() { public void actionPerformed(ActionEvent e) { handleCommand("−"); } });

        im.put(KeyStroke.getKeyStroke('*'), "mul");
        im.put(KeyStroke.getKeyStroke("MULTIPLY"), "mul");
        am.put("mul", new AbstractAction() { public void actionPerformed(ActionEvent e) { handleCommand("×"); } });

        im.put(KeyStroke.getKeyStroke('/'), "div");
        im.put(KeyStroke.getKeyStroke("DIVIDE"), "div");
        am.put("div", new AbstractAction() { public void actionPerformed(ActionEvent e) { handleCommand("÷"); } });

        // Enter / Egal
        im.put(KeyStroke.getKeyStroke("ENTER"), "eq");
        im.put(KeyStroke.getKeyStroke('='), "eq");
        am.put("eq", new AbstractAction() { public void actionPerformed(ActionEvent e) { handleCommand("="); } });

        // Backspace
        im.put(KeyStroke.getKeyStroke("BACK_SPACE"), "backspace");
        am.put("backspace", new AbstractAction() { public void actionPerformed(ActionEvent e) { handleCommand("⌫"); } });

        // Escape / Clear
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "clear");
        am.put("clear", new AbstractAction() { public void actionPerformed(ActionEvent e) { handleCommand("C"); } });

        // Decimal punct
        im.put(KeyStroke.getKeyStroke('.'), "dot");
        im.put(KeyStroke.getKeyStroke("DECIMAL"), "dot");
        am.put("dot", new AbstractAction() { public void actionPerformed(ActionEvent e) { handleCommand("."); } });
    }
"""

content = re.sub(r'private class ButtonClickListener', key_bindings + '\n    private class ButtonClickListener', content)

# Check if AbstractAction and KeyStroke are imported, if not add them
if 'import javax.swing.AbstractAction;' not in content:
    content = content.replace('import javax.swing.*;', 'import javax.swing.*;\nimport javax.swing.AbstractAction;\nimport javax.swing.KeyStroke;')

with open('lab1/java/src/calculator/CalculatorGUI.java', 'w') as f:
    f.write(content)
