package core;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Componentă Swing pentru afișarea stării de Reply (răspuns la un mesaj).
 * Apare deasupra câmpului de tastare atunci când utilizatorul alege să răspundă la un mesaj.
 */
public class ReplyPanel extends JPanel {
    private final JLabel replyLabel;
    private final JButton cancelButton;
    private Message targetMessage;
    private Runnable onCancelCallback;

    public ReplyPanel() {
        setLayout(new BorderLayout(8, 0));
        setBackground(new Color(236, 242, 252));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 4, 0, 0, new Color(41, 128, 185)),
                BorderFactory.createEmptyBorder(6, 10, 6, 8)
        ));
        setVisible(false);

        replyLabel = new JLabel();
        replyLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        replyLabel.setForeground(new Color(30, 40, 60));
        add(replyLabel, BorderLayout.CENTER);

        cancelButton = new JButton("✕");
        cancelButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        cancelButton.setForeground(new Color(120, 130, 140));
        cancelButton.setBorderPainted(false);
        cancelButton.setContentAreaFilled(false);
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.setToolTipText("Anulează răspunsul");
        cancelButton.addActionListener(e -> clearReply());
        add(cancelButton, BorderLayout.EAST);

        setPreferredSize(new Dimension(100, 36));
    }

    /**
     * Activează starea de reply pentru mesajul indicat.
     */
    public void setReply(Message message, Runnable onCancel) {
        this.targetMessage = message;
        this.onCancelCallback = onCancel;
        if (message == null) {
            clearReply();
            return;
        }

        String preview = message.isFile()
                ? "[Fișier: " + message.getFileAttachment().getFileName() + "]"
                : message.getContent();
        if (preview != null && preview.length() > 50) {
            preview = preview.substring(0, 47) + "...";
        }

        replyLabel.setText("<html>↩ <b>Răspuns către @" + message.getSender() + ":</b> <i>\"" +
                escapeHtml(preview) + "\"</i></html>");
        setVisible(true);
        revalidate();
        repaint();
    }

    /**
     * Anulează starea de reply și ascunde panoul.
     */
    public void clearReply() {
        this.targetMessage = null;
        setVisible(false);
        if (onCancelCallback != null) {
            onCancelCallback.run();
        }
        revalidate();
        repaint();
    }

    public Message getTargetMessage() {
        return targetMessage;
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}
