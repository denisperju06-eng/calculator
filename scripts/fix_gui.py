import re

with open('lab1/java/src/calculator/CalculatorGUI.java', 'r') as f:
    content = f.read()

# 1. Main Panel Background
content = content.replace("mainPanel.setBackground(new Color(243, 243, 243));", "mainPanel.setBackground(new Color(32, 32, 32));")

# 2. Display Panel Background
content = content.replace("displayPanel.setBackground(Color.WHITE);", "displayPanel.setBackground(new Color(24, 24, 24));")
content = content.replace("displayPanel.setBorder(BorderFactory.createCompoundBorder(\n                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),", "displayPanel.setBorder(BorderFactory.createCompoundBorder(\n                BorderFactory.createLineBorder(new Color(60, 60, 60), 1),")

# 3. Display Field
content = content.replace("displayField.setBackground(Color.WHITE);", "displayField.setBackground(new Color(24, 24, 24));\n        displayField.setForeground(Color.WHITE);\n        displayField.setCaretColor(Color.WHITE);")

# 4. Base Panel
content = content.replace("basePanel.setBackground(new Color(230, 235, 245));", "basePanel.setBackground(new Color(15, 15, 15));")
content = content.replace("hexLabel.setFont(baseFont);", "hexLabel.setFont(baseFont);\n        hexLabel.setForeground(new Color(100, 200, 255));\n        decLabel.setForeground(new Color(100, 200, 255));\n        octLabel.setForeground(new Color(100, 200, 255));\n        binLabel.setForeground(new Color(100, 200, 255));")

# 5. Buttons
content = content.replace("btn.setBackground(new Color(0, 103, 192));", "btn.setBackground(new Color(255, 140, 0));")

content = content.replace("btn.setBackground(Color.WHITE);\n            btn.setForeground(Color.BLACK);", "btn.setBackground(new Color(60, 60, 60));\n            btn.setForeground(Color.WHITE);")

content = content.replace("btn.setBackground(new Color(248, 249, 250));\n            btn.setForeground(new Color(30, 30, 30));", "btn.setBackground(new Color(45, 45, 45));\n            btn.setForeground(new Color(200, 200, 200));")

# 6. Deg/Rad button
content = content.replace("degRadBtn.setFont(new Font(\"Segoe UI\", Font.BOLD, 11));", "degRadBtn.setFont(new Font(\"Segoe UI\", Font.BOLD, 11));\n        degRadBtn.setForeground(new Color(150, 150, 150));\n        degRadBtn.setBackground(new Color(40, 40, 40));\n        degRadBtn.setOpaque(true);\n        degRadBtn.setBorderPainted(false);")

with open('lab1/java/src/calculator/CalculatorGUI.java', 'w') as f:
    f.write(content)
