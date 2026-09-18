package core;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Document;
import javax.swing.text.Element;
import java.awt.*;

/**
 * Bară de stare (Status Bar) afișată la baza ferestrei principale.
 * Afișează linia, coloana curentă, numărul de caractere selectate/totale și mesaje de sistem.
 */
public class StatusBar extends JPanel implements CaretListener {

    private final JLabel statusMsgLabel;
    private final JLabel lineColLabel;
    private final JLabel selectionLabel;
    private final JLabel totalCharsLabel;
    private final JLabel formatLabel;

    private TextEditorTab currentTab;

    public StatusBar() {
        super(new BorderLayout());
        setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(215, 220, 225)),
                new EmptyBorder(4, 10, 4, 10)
        ));
        setBackground(new Color(245, 246, 248));

        this.statusMsgLabel = new JLabel("Gata");
        this.statusMsgLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        this.statusMsgLabel.setForeground(new Color(80, 80, 80));

        this.lineColLabel = new JLabel("Linia: 1, Col: 1");
        this.lineColLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        this.selectionLabel = new JLabel("Selectat: 0");
        this.selectionLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        this.totalCharsLabel = new JLabel("Caractere: 0");
        this.totalCharsLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        this.formatLabel = new JLabel("RTF");
        this.formatLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        this.formatLabel.setForeground(new Color(40, 100, 180));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(selectionLabel);
        rightPanel.add(totalCharsLabel);
        rightPanel.add(lineColLabel);
        rightPanel.add(formatLabel);

        add(statusMsgLabel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }

    public void setActiveTab(TextEditorTab tab) {
        if (this.currentTab != null) {
            this.currentTab.getTextPane().removeCaretListener(this);
        }

        this.currentTab = tab;

        if (this.currentTab != null) {
            this.currentTab.getTextPane().addCaretListener(this);
            updateInfo();
        } else {
            lineColLabel.setText("Linia: -, Col: -");
            selectionLabel.setText("Selectat: 0");
            totalCharsLabel.setText("Caractere: 0");
            formatLabel.setText("");
        }
    }

    public void setMessage(String message) {
        statusMsgLabel.setText(message != null ? message : "Gata");
    }

    private void updateInfo() {
        if (currentTab == null) return;

        JTextPane pane = currentTab.getTextPane();
        Document doc = pane.getDocument();
        int dot = pane.getCaretPosition();
        Element root = doc.getDefaultRootElement();

        int line = root.getElementIndex(dot);
        Element lineElem = root.getElement(line);
        int col = dot - lineElem.getStartOffset() + 1;

        int selStart = pane.getSelectionStart();
        int selEnd = pane.getSelectionEnd();
        int selLen = Math.abs(selEnd - selStart);

        lineColLabel.setText(String.format("Linia: %d, Col: %d", line + 1, col));
        selectionLabel.setText(String.format("Selectat: %d", selLen));
        totalCharsLabel.setText(String.format("Caractere: %d", doc.getLength()));

        if (currentTab.getCurrentFile() != null) {
            String name = currentTab.getCurrentFile().getName().toLowerCase();
            if (name.endsWith(".txt")) {
                formatLabel.setText("Text Simplu (UTF-8)");
            } else {
                formatLabel.setText("Rich Text (RTF)");
            }
        } else {
            formatLabel.setText("Rich Text (RTF)");
        }
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        updateInfo();
    }
}
