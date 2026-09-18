with open('lab1/index.html', 'r') as f:
    content = f.read()

# Change emojis
content = content.replace("🧮", "✨")
content = content.replace("⏱", "📜")
content = content.replace("ℹ️", "💡")
content = content.replace("🗑️", "🧹")

# Add Back button to history modal
history_header_old = """<div class="history-header">
                <h3>Istoric Calcule</h3>
                <button id="clearHistoryBtn" class="clear-hist-btn" title="Golește istoricul">🧹</button>
            </div>"""

history_header_new = """<div class="history-header">
                <button id="closeHistoryBtn" class="clear-hist-btn" title="Înapoi" style="margin-right: 10px; font-size: 1.2rem;">⬅️</button>
                <h3 style="flex-grow: 1; margin: 0;">Istoric Calcule</h3>
                <button id="clearHistoryBtn" class="clear-hist-btn" title="Golește istoricul">🧹</button>
            </div>"""

content = content.replace(history_header_old, history_header_new)

with open('lab1/index.html', 'w') as f:
    f.write(content)
