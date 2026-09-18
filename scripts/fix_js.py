with open('lab1/js/ui/CalculatorUI.js', 'r') as f:
    content = f.read()

content = content.replace("this.clearHistoryBtn = document.getElementById('clearHistoryBtn');", "this.clearHistoryBtn = document.getElementById('clearHistoryBtn');\n        this.closeHistoryBtn = document.getElementById('closeHistoryBtn');")

close_logic = """if (this.clearHistoryBtn) {
            this.clearHistoryBtn.addEventListener('click', () => {
                this.engine.clear();
                this.updateView();
            });
        }
        
        if (this.closeHistoryBtn) {
            this.closeHistoryBtn.addEventListener('click', () => {
                this.historyModal.classList.remove('active');
            });
        }"""

content = content.replace("""if (this.clearHistoryBtn) {
            this.clearHistoryBtn.addEventListener('click', () => {
                this.engine.clear();
                // Aici ar trebui ideal curățat doar istoricul, dar metoda clear() e de bază
            });
        }""", close_logic)
        
content = content.replace("""if (this.clearHistoryBtn) {
            this.clearHistoryBtn.addEventListener('click', () => {
                this.engine.clear();
            });
        }""", close_logic)

with open('lab1/js/ui/CalculatorUI.js', 'w') as f:
    f.write(content)
