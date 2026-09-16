import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Fereastră de dialog pentru vizualizarea, căutarea și exportul istoricului mesajelor primite.
 * Realizează cerința (b) din laborator: afișarea istoriei mesajelor primite.
 */
public class HistoryDialog extends JDialog {
    private final String roomName;
    private final List<Message> fullHistory;
    private JTextField searchField;
    private JTextArea historyTextArea;
    private JLabel countLabel;

    public HistoryDialog(JFrame parent, String roomName, List<Message> history) {
        super(parent, "Istoric Mesaje - Camera: " + (roomName != null ? roomName : "General"), true);
        this.roomName = roomName;
        this.fullHistory = new ArrayList<>(history != null ? history : new ArrayList<>());

        setupUI();
        applyFilter("");
    }

    private void setupUI() {
        setSize(650, 500);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // ================= TOP: SEARCH BAR =================
        JPanel topPanel = new JPanel(new BorderLayout(8, 0));
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 225, 230)),
                BorderFactory.createEmptyBorder(0, 0, 8, 0)
        ));

        JPanel searchBox = new JPanel(new BorderLayout(6, 0));
        searchBox.add(new JLabel("🔍 Filtrează mesaje:"), BorderLayout.WEST);
        searchField = new JTextField();
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
            private void update() { applyFilter(searchField.getText().trim()); }
        });
        searchBox.add(searchField, BorderLayout.CENTER);

        countLabel = new JLabel("0 mesaje", JLabel.RIGHT);
        countLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        countLabel.setForeground(new Color(110, 120, 130));

        topPanel.add(searchBox, BorderLayout.CENTER);
        topPanel.add(countLabel, BorderLayout.EAST);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // ================= CENTER: HISTORY TEXT AREA =================
        historyTextArea = new JTextArea();
        historyTextArea.setEditable(false);
        historyTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        historyTextArea.setBackground(new Color(252, 253, 255));
        historyTextArea.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JScrollPane scrollPane = new JScrollPane(historyTextArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(210, 220, 230)));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // ================= BOTTOM: BUTTONS =================
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        JButton exportBtn = new JButton("💾 Exportă în Fișier (.txt)");
        exportBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        exportBtn.addActionListener(e -> exportHistory());

        JButton closeBtn = new JButton("Închide");
        closeBtn.addActionListener(e -> dispose());

        bottomPanel.add(exportBtn);
        bottomPanel.add(closeBtn);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void applyFilter(String query) {
        StringBuilder sb = new StringBuilder();
        int matched = 0;

        String lowerQuery = query.toLowerCase();

        for (Message msg : fullHistory) {
            String text = msg.getContent() != null ? msg.getContent() : "";
            String sender = msg.getSender() != null ? msg.getSender() : "";
            String fileName = (msg.isFile() && msg.getFileAttachment() != null) ? msg.getFileAttachment().getFileName() : "";

            boolean match = query.isEmpty() ||
                    text.toLowerCase().contains(lowerQuery) ||
                    sender.toLowerCase().contains(lowerQuery) ||
                    fileName.toLowerCase().contains(lowerQuery);

            if (match) {
                matched++;
                sb.append("[").append(msg.getFormattedDateTime()).append("] ");
                if (msg.getType() == MessageType.SYSTEM) {
                    sb.append("• ").append(msg.getContent()).append(" •\n");
                } else {
                    sb.append("<").append(msg.getSender()).append(">");
                    if (msg.isReply()) {
                        sb.append(" (Răspuns către @").append(msg.getReplyToSender()).append(": \"")
                          .append(msg.getReplyToContent()).append("\")");
                    }
                    sb.append(": ");
                    if (msg.isFile()) {
                        sb.append("[FIȘIER: ").append(fileName)
                          .append(" (").append(msg.getFileAttachment().getFormattedSize()).append(")]");
                    } else {
                        sb.append(msg.getContent());
                    }
                    sb.append("\n");
                }
            }
        }

        if (matched == 0) {
            sb.append(fullHistory.isEmpty() ? "Nu există mesaje în istoric pentru această cameră."
                                            : "Niciun mesaj nu corespunde filtrului de căutare.");
        }

        historyTextArea.setText(sb.toString());
        historyTextArea.setCaretPosition(0);
        countLabel.setText(matched + " / " + fullHistory.size() + " mesaje");
    }

    private void exportHistory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("istoric_chat_" + (roomName != null ? roomName : "general") + ".txt"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            try {
                HistoryManager.exportToTextFile(fullHistory, target, roomName);
                JOptionPane.showMessageDialog(this, "Istoricul a fost exportat cu succes în:\n" + target.getAbsolutePath(),
                        "Export Reușit", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Eroare la exportul fișierului: " + ex.getMessage(),
                        "Eroare", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
