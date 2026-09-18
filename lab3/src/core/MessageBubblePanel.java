package core;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 * Componentă grafică Swing ce reprezintă un mesaj individual (Card / Bubble) în lista de chat.
 * Respectă cerințele laboratorului:
 * - Afișare text și oră
 * - Afișare răspuns (Reply) cu citarea mesajului la care s-a răspuns
 * - Afișare fișiere atașate cu buton de descărcare/salvare
 * - Buton direct de Reply și meniu contextual (click dreapta)
 */
public class MessageBubblePanel extends JPanel {
    private final Message message;
    private final boolean isOwnMessage;

    public MessageBubblePanel(Message message, boolean isOwnMessage,
                              Consumer<Message> onReplyRequested,
                              Consumer<FileAttachment> onSaveFileRequested) {
        this.message = message;
        this.isOwnMessage = isOwnMessage;

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        if (message.getType() == MessageType.SYSTEM) {
            buildSystemMessageUI();
        } else {
            buildChatMessageUI(onReplyRequested, onSaveFileRequested);
        }
    }

    private void buildSystemMessageUI() {
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.setOpaque(false);

        JLabel sysLabel = new JLabel("• " + message.getContent() + " (" + message.getFormattedTime() + ") •");
        sysLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        sysLabel.setForeground(new Color(110, 120, 130));
        sysLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 230), 1, true),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));
        centerPanel.add(sysLabel);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void buildChatMessageUI(Consumer<Message> onReplyRequested,
                                    Consumer<FileAttachment> onSaveFileRequested) {
        // Containerul principal al bulei de chat
        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));

        // Culori în funcție de expeditor (mesaj propriu vs mesaj primit)
        Color bgColor = isOwnMessage ? new Color(232, 244, 253) : new Color(255, 255, 255);
        Color borderColor = isOwnMessage ? new Color(187, 222, 251) : new Color(224, 224, 224);

        bubble.setBackground(bgColor);
        bubble.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        // ================= 1. HEADER (Expeditor, Timp, Buton Reply) =================
        JPanel headerPanel = new JPanel(new BorderLayout(8, 0));
        headerPanel.setOpaque(false);

        JLabel senderLabel = new JLabel(isOwnMessage ? "Tu (" + message.getSender() + ")" : message.getSender());
        senderLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        senderLabel.setForeground(isOwnMessage ? new Color(21, 101, 192) : new Color(46, 125, 50));

        JLabel timeLabel = new JLabel(message.getFormattedTime());
        timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        timeLabel.setForeground(new Color(130, 140, 150));

        JPanel senderAndTime = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        senderAndTime.setOpaque(false);
        senderAndTime.add(senderLabel);
        senderAndTime.add(timeLabel);

        // Buton discret de Reply
        JButton replyBtn = new JButton("↩ Răspunde");
        replyBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        replyBtn.setForeground(new Color(60, 100, 160));
        replyBtn.setBorderPainted(false);
        replyBtn.setContentAreaFilled(false);
        replyBtn.setFocusPainted(false);
        replyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        replyBtn.setToolTipText("Răspunde la acest mesaj");
        replyBtn.addActionListener(e -> {
            if (onReplyRequested != null) onReplyRequested.accept(message);
        });

        headerPanel.add(senderAndTime, BorderLayout.WEST);
        headerPanel.add(replyBtn, BorderLayout.EAST);
        bubble.add(headerPanel);

        // ================= 2. CITARE REPLY (Dacă mesajul este un răspuns) =================
        if (message.isReply()) {
            bubble.add(Box.createVerticalStrut(4));
            JPanel quotePanel = new JPanel(new BorderLayout());
            quotePanel.setBackground(new Color(240, 244, 248));
            quotePanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(33, 150, 243)),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)
            ));

            JLabel quoteSender = new JLabel("Răspuns către @" + message.getReplyToSender());
            quoteSender.setFont(new Font("SansSerif", Font.BOLD, 11));
            quoteSender.setForeground(new Color(70, 80, 95));

            JLabel quoteText = new JLabel("<html><i>\"" + escapeHtml(message.getReplyToContent()) + "\"</i></html>");
            quoteText.setFont(new Font("SansSerif", Font.PLAIN, 11));
            quoteText.setForeground(new Color(90, 100, 110));

            quotePanel.add(quoteSender, BorderLayout.NORTH);
            quotePanel.add(quoteText, BorderLayout.CENTER);
            bubble.add(quotePanel);
        }

        // ================= 3. CONȚINUT MESAJ (Text sau Fișier) =================
        bubble.add(Box.createVerticalStrut(6));

        if (message.isFile() && message.getFileAttachment() != null) {
            FileAttachment att = message.getFileAttachment();
            JPanel fileCard = new JPanel(new BorderLayout(10, 0));
            fileCard.setOpaque(false);
            fileCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 210, 225), 1, true),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
            ));

            JLabel fileIcon = new JLabel("📄");
            fileIcon.setFont(new Font("SansSerif", Font.PLAIN, 24));

            JPanel fileInfo = new JPanel(new GridLayout(2, 1, 0, 2));
            fileInfo.setOpaque(false);
            JLabel nameLabel = new JLabel(att.getFileName());
            nameLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            JLabel sizeLabel = new JLabel("Dimensiune: " + att.getFormattedSize());
            sizeLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
            sizeLabel.setForeground(new Color(100, 110, 120));
            fileInfo.add(nameLabel);
            fileInfo.add(sizeLabel);

            JButton downloadBtn = new JButton("💾 Salvează Fișier");
            downloadBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
            downloadBtn.setBackground(new Color(52, 152, 219));
            downloadBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            downloadBtn.addActionListener(e -> {
                if (onSaveFileRequested != null) onSaveFileRequested.accept(att);
            });

            fileCard.add(fileIcon, BorderLayout.WEST);
            fileCard.add(fileInfo, BorderLayout.CENTER);
            fileCard.add(downloadBtn, BorderLayout.EAST);
            bubble.add(fileCard);

        } else {
            // Mesaj text cu wrap automat
            JTextArea contentArea = new JTextArea(message.getContent());
            contentArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
            contentArea.setLineWrap(true);
            contentArea.setWrapStyleWord(true);
            contentArea.setEditable(false);
            contentArea.setOpaque(false);
            contentArea.setBorder(null);
            bubble.add(contentArea);
        }

        // ================= MENIU CONTEXTUAL (Click Dreapta) =================
        JPopupMenu contextMenu = new JPopupMenu();

        JMenuItem replyItem = new JMenuItem("↩ Răspunde (Reply)");
        replyItem.addActionListener(e -> {
            if (onReplyRequested != null) onReplyRequested.accept(message);
        });
        contextMenu.add(replyItem);

        JMenuItem copyItem = new JMenuItem("📋 Copiază text");
        copyItem.addActionListener(e -> {
            String textToCopy = message.isFile() ? message.getFileAttachment().getFileName() : message.getContent();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(textToCopy), null);
        });
        contextMenu.add(copyItem);

        if (message.isFile()) {
            JMenuItem saveItem = new JMenuItem("💾 Salvează fișier pe disc");
            saveItem.addActionListener(e -> {
                if (onSaveFileRequested != null) onSaveFileRequested.accept(message.getFileAttachment());
            });
            contextMenu.add(saveItem);
        }

        bubble.setComponentPopupMenu(contextMenu);

        // Poziționare: aliniat la dreapta pentru mesajele proprii, la stânga pentru ceilalți
        JPanel alignPanel = new JPanel(new FlowLayout(isOwnMessage ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        alignPanel.setOpaque(false);
        alignPanel.add(bubble);

        add(alignPanel, BorderLayout.CENTER);
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }

    public Message getMessage() {
        return message;
    }
}
