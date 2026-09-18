with open('lab1/java/src/calculator/CalculatorGUI.java', 'r') as f:
    content = f.read()

# Fix the mess
content = content.replace("gui.setupKeyBindings();\n        setVisible(true);", "gui.setupKeyBindings();\n            gui.setVisible(true);")

with open('lab1/java/src/calculator/CalculatorGUI.java', 'w') as f:
    f.write(content)
