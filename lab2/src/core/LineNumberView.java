package core;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Componentă vizuală Swing pentru afișarea numerelor de linie în stânga editorului de text.
 * Se integrează ca RowHeaderView într-un JScrollPane.
 */
public class LineNumberView extends JComponent implements DocumentListener, CaretListener {

    private final JTextPane textPane;
    private final Font font;
    private final Color currentLineBg = new Color(230, 235, 245);
    private final Color currentLineNumberColor = new Color(30, 60, 150);
    private final Color defaultLineNumberColor = new Color(130, 140, 155);
    private final Color separatorColor = new Color(210, 215, 225);
    private final Color bgColor = new Color(248, 249, 250);

    private int lastDigits = 0;
    private int currentCaretLine = 0;

    public LineNumberView(JTextPane textPane) {
        this.textPane = textPane;
        this.font = new Font("Monospaced", Font.PLAIN, 12);
        setFont(font);

        textPane.getDocument().addDocumentListener(this);
        textPane.addCaretListener(this);
        updateDimensions();
    }

    private void updateDimensions() {
        Element root = textPane.getDocument().getDefaultRootElement();
        int lineCount = Math.max(1, root.getElementCount());
        int digits = String.valueOf(lineCount).length();

        if (digits != lastDigits) {
            lastDigits = digits;
            FontMetrics fm = getFontMetrics(font);
            int width = fm.stringWidth("9".repeat(digits)) + 20;
            Dimension d = new Dimension(Math.max(36, width), 0);
            setPreferredSize(d);
            revalidate();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Fundal
        g2.setColor(bgColor);
        g2.fillRect(0, 0, width, height);

        // Linie separatoare
        g2.setColor(separatorColor);
        g2.drawLine(width - 1, 0, width - 1, height);

        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();

        Document doc = textPane.getDocument();
        Element root = doc.getDefaultRootElement();
        int totalLines = root.getElementCount();

        Rectangle clip = g2.getClipBounds();
        Insets insets = textPane.getInsets();

        for (int i = 0; i < totalLines; i++) {
            Element lineElement = root.getElement(i);
            int startOffset = lineElement.getStartOffset();

            try {
                Rectangle2D r2d = textPane.modelToView2D(startOffset);
                if (r2d == null) continue;

                int y = (int) r2d.getY();
                int h = (int) r2d.getHeight();

                if (y + h < clip.y) continue;
                if (y > clip.y + clip.height) break;

                boolean isCurrentLine = (i == currentCaretLine);
                if (isCurrentLine) {
                    g2.setColor(currentLineBg);
                    g2.fillRect(0, y, width - 1, h);
                    g2.setColor(currentLineNumberColor);
                } else {
                    g2.setColor(defaultLineNumberColor);
                }

                String lineStr = String.valueOf(i + 1);
                int strWidth = fm.stringWidth(lineStr);
                int drawX = width - strWidth - 8;
                int drawY = y + fm.getAscent() + (h - fm.getHeight()) / 2;

                g2.drawString(lineStr, drawX, drawY);

            } catch (BadLocationException ignored) {
            }
        }

        g2.dispose();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        updateDimensions();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateDimensions();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        updateDimensions();
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        int dot = e.getDot();
        Element root = textPane.getDocument().getDefaultRootElement();
        int newLine = root.getElementIndex(dot);
        if (newLine != currentCaretLine) {
            currentCaretLine = newLine;
            repaint();
        }
    }
}
