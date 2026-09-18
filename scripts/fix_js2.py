import re

with open('lab1/js/ui/CalculatorUI.js', 'r') as f:
    content = f.read()

new_block = """        if (this.clearHistoryBtn) {
            this.clearHistoryBtn.addEventListener('click', () => {
                this.engine.clear();
                if (this.historyList) this.historyList.innerHTML = '<div class="empty-history">Nu există calcule recente</div>';
            });
        }
        
        if (this.closeHistoryBtn) {
            this.closeHistoryBtn.addEventListener('click', () => {
                this.historyModal.classList.remove('active');
            });
        }"""

content = re.sub(r'        if \(this\.clearHistoryBtn\) \{.*?\n        \}', new_block, content, flags=re.DOTALL)

with open('lab1/js/ui/CalculatorUI.js', 'w') as f:
    f.write(content)
