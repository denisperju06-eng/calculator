package core;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Interfața grafică principală (Swing) pentru clientul de Chat în rețea locală.
 * Îndeplinește toate cerințele din laboratorul de POO:
 * a. Transmiterea și recepționarea mesajelor prin rețea
 * b. Afișarea istoriei mesajelor primite și dialog dedicat cu căutare/export
 * c. Posibilitatea de a răspunde la mesajele primite (Reply) cu citare vizuală
 * d. Transmiterea și recepționarea fișierelor binare cu salvare pe disc
 * e. Camere de chat multiple (Chat Rooms) cu creare dinamică și listă utilizatori activi
 */
public class ClientGUI extends JFrame implements ClientListener {
    private final Client client;

    // Componente de conexiune
    private JTextField hostField;
    private JTextField portField;
    private JTextField usernameField;
    private JButton connectButton;
    private JLabel statusLabel;

    // Componente Camere și Utilizatori (Sidebar)
    private DefaultListModel<String> roomsListModel;
    private JList<String> roomsList;
    private DefaultListModel<String> usersListModel;
    private JList<String> usersList;
    private JLabel currentRoomTitle;

    // Componente Mesagerie și Chat
    private JPanel messagesContainer;
    private JScrollPane messagesScrollPane;
    private ReplyPanel replyPanel;
    private JTextField messageInputField;
    private JButton sendButton;
    private JButton attachFileButton;

    // Istoric mesaje primite în camera curentă
    private final List<Message> currentRoomMessages = new ArrayList<>();

    public ClientGUI() {
        super("Chat Rețea Locală - Client POO");
        this.client = new Client();
        this.client.addListener(this);

        setupUI();
    }

    private void setupUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 680);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        JPanel rootPanel = new JPanel(new BorderLayout(0, 0));

        // ================= 1. BARA DE CONEXIUNE (SUS) =================
        JPanel connectionBar = buildConnectionBar();
        rootPanel.add(connectionBar, BorderLayout.NORTH);

        // ================= 2. ZONA CENTRALĂ (SIDEBAR + CHAT) =================
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(260);
        splitPane.setResizeWeight(0.25);

        JPanel sidebar = buildSidebar();
        JPanel chatPanel = buildChatPanel();

        splitPane.setLeftComponent(sidebar);
        splitPane.setRightComponent(chatPanel);
        rootPanel.add(splitPane, BorderLayout.CENTER);

        setContentPane(rootPanel);

        // Gestionare închidere fereastră: deconectare curată
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (client.isConnected()) {
                    client.disconnect("Închidere aplicație");
                }
            }
        });
    }

    /**
     * Construiește panoul superior de conectare la server.
     */
    private JPanel buildConnectionBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(new Color(245, 247, 250));
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(215, 220, 228)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JPanel inputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        inputs.setOpaque(false);

        inputs.add(new JLabel("Server:"));
        hostField = new JTextField("localhost", 9);
        inputs.add(hostField);

        inputs.add(new JLabel("Port:"));
        portField = new JTextField(String.valueOf(Server.DEFAULT_PORT), 5);
        inputs.add(portField);

        inputs.add(new JLabel("Nume Utilizator:"));
        usernameField = new JTextField("Student_" + (int)(Math.random() * 900 + 100), 10);
        inputs.add(usernameField);

        connectButton = new JButton("Conectare");
        connectButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        connectButton.setBackground(new Color(41, 128, 185));
        connectButton.setForeground(Color.BLACK);
        connectButton.addActionListener(e -> toggleConnection());
        inputs.add(connectButton);

        statusLabel = new JLabel("● Neconectat", SwingConstants.RIGHT);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLabel.setForeground(new Color(180, 50, 50));

        bar.add(inputs, BorderLayout.WEST);
        bar.add(statusLabel, BorderLayout.EAST);
        return bar;
    }

    /**
     * Construiește panoul lateral (Sidebar) pentru Camere de Chat și Utilizatori Activi.
     */
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 8));
        sidebar.setBackground(new Color(248, 250, 252));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Secțiunea Camere de Chat
        JPanel roomsSection = new JPanel(new BorderLayout(0, 6));
        roomsSection.setOpaque(false);

        JPanel roomsHeader = new JPanel(new BorderLayout());
        roomsHeader.setOpaque(false);
        JLabel roomsTitle = new JLabel("Camere de Chat");
        roomsTitle.setFont(new Font("SansSerif", Font.BOLD, 12));

        JButton addRoomBtn = new JButton("+ Cameră nouă");
        addRoomBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        addRoomBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addRoomBtn.addActionListener(e -> promptCreateRoom());
        roomsHeader.add(roomsTitle, BorderLayout.WEST);
        roomsHeader.add(addRoomBtn, BorderLayout.EAST);
        roomsSection.add(roomsHeader, BorderLayout.NORTH);

        roomsListModel = new DefaultListModel<>();
        roomsList = new JList<>(roomsListModel);
        roomsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomsList.setFont(new Font("SansSerif", Font.PLAIN, 12));
        roomsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = roomsList.getSelectedValue();
                if (selected != null && !selected.equals(client.getCurrentRoom())) {
                    client.joinRoom(selected);
                }
            }
        });
        roomsSection.add(new JScrollPane(roomsList), BorderLayout.CENTER);

        // Secțiunea Utilizatori în camera curentă
        JPanel usersSection = new JPanel(new BorderLayout(0, 6));
        usersSection.setOpaque(false);
        JLabel usersTitle = new JLabel("Utilizatori în cameră");
        usersTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        usersSection.add(usersTitle, BorderLayout.NORTH);

        usersListModel = new DefaultListModel<>();
        usersList = new JList<>(usersListModel);
        usersList.setFont(new Font("SansSerif", Font.PLAIN, 12));
        usersSection.add(new JScrollPane(usersList), BorderLayout.CENTER);

        // Butoane suplimentare pentru Istoric și Info
        JPanel sidebarBottom = new JPanel(new GridLayout(2, 1, 0, 5));
        sidebarBottom.setOpaque(false);

        JButton viewHistoryBtn = new JButton("📜 Istoric Mesaje / Căutare");
        viewHistoryBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        viewHistoryBtn.addActionListener(e -> openHistoryDialog());

        sidebarBottom.add(viewHistoryBtn);

        JSplitPane innerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, roomsSection, usersSection);
        innerSplit.setDividerLocation(180);
        innerSplit.setResizeWeight(0.5);

        sidebar.add(innerSplit, BorderLayout.CENTER);
        sidebar.add(sidebarBottom, BorderLayout.SOUTH);
        return sidebar;
    }

    /**
     * Construiește zona de chat: Header, Lista de mesaje, Reply preview și Input bar.
     */
    private JPanel buildChatPanel() {
        JPanel chatPanel = new JPanel(new BorderLayout(0, 0));
        chatPanel.setBackground(new Color(245, 247, 250));

        // 1. HEADER CHAT
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 225, 230)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        currentRoomTitle = new JLabel("# General");
        currentRoomTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        currentRoomTitle.setForeground(new Color(30, 40, 55));
        headerPanel.add(currentRoomTitle, BorderLayout.WEST);

        chatPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. CONTAINER MESAJE (Scrollabil)
        messagesContainer = new JPanel();
        messagesContainer.setLayout(new BoxLayout(messagesContainer, BoxLayout.Y_AXIS));
        messagesContainer.setBackground(new Color(245, 247, 250));

        messagesScrollPane = new JScrollPane(messagesContainer);
        messagesScrollPane.setBorder(null);
        messagesScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        chatPanel.add(messagesScrollPane, BorderLayout.CENTER);

        // 3. ZONA DE INTRARE (REPLY BAR + TEXT INPUT + BUTOANE)
        JPanel bottomArea = new JPanel(new BorderLayout(0, 0));
        bottomArea.setBackground(Color.WHITE);
        bottomArea.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 230)));

        replyPanel = new ReplyPanel();
        bottomArea.add(replyPanel, BorderLayout.NORTH);

        JPanel inputBar = new JPanel(new BorderLayout(8, 0));
        inputBar.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        inputBar.setOpaque(false);

        attachFileButton = new JButton("📎 Fișier");
        attachFileButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        attachFileButton.setToolTipText("Atașează și trimite un fișier pe rețea");
        attachFileButton.setEnabled(false);
        attachFileButton.addActionListener(e -> selectAndSendFile());
        inputBar.add(attachFileButton, BorderLayout.WEST);

        messageInputField = new JTextField();
        messageInputField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        messageInputField.setEnabled(false);
        messageInputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendTextMessage();
                }
            }
        });
        inputBar.add(messageInputField, BorderLayout.CENTER);

        sendButton = new JButton("Trimite");
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        sendButton.setBackground(new Color(39, 174, 96));
        sendButton.setForeground(Color.BLACK);
        sendButton.setEnabled(false);
        sendButton.addActionListener(e -> sendTextMessage());
        inputBar.add(sendButton, BorderLayout.EAST);

        bottomArea.add(inputBar, BorderLayout.CENTER);
        chatPanel.add(bottomArea, BorderLayout.SOUTH);

        return chatPanel;
    }

    // ================= ACȚIUNI UTILIZATOR =================

    private void toggleConnection() {
        if (!client.isConnected()) {
            String host = hostField.getText().trim();
            int port;
            try {
                port = Integer.parseInt(portField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Portul specificat este invalid!", "Eroare", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Introduceți un nume de utilizator!", "Atenție", JOptionPane.WARNING_MESSAGE);
                return;
            }

            connectButton.setEnabled(false);
            connectButton.setText("Conectare...");
            client.connect(host, port, username);
        } else {
            client.disconnect("Deconectat la cererea utilizatorului");
        }
    }

    private void sendTextMessage() {
        String text = messageInputField.getText().trim();
        if (text.isEmpty()) return;

        Message replyTarget = replyPanel.getTargetMessage();
        boolean success = client.sendTextMessage(text, replyTarget);
        if (success) {
            messageInputField.setText("");
            replyPanel.clearReply();
            messageInputField.requestFocusInWindow();
        }
    }

    private void selectAndSendFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selectați fișierul de transmis pe rețea");
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            Message replyTarget = replyPanel.getTargetMessage();

            // Trimitere fișier pe fir separat pentru a nu bloca GUI
            new Thread(() -> {
                try {
                    client.sendFile(selectedFile, replyTarget);
                    SwingUtilities.invokeLater(() -> {
                        replyPanel.clearReply();
                    });
                } catch (IOException ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Eroare la transmiterea fișierului:\n" + ex.getMessage(),
                                "Eroare Fișier", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }, "File-Send-Thread").start();
        }
    }

    private void saveFileToDisk(FileAttachment attachment) {
        if (attachment == null || attachment.getData() == null) {
            JOptionPane.showMessageDialog(this, "Datele fișierului nu sunt disponibile.", "Eroare", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Salvare fișier recepționat");
        chooser.setSelectedFile(new File(attachment.getFileName()));

        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File targetFile = chooser.getSelectedFile();
            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                fos.write(attachment.getData());
                fos.flush();
                JOptionPane.showMessageDialog(this,
                        "Fișier salvat cu succes!\nLocație: " + targetFile.getAbsolutePath() +
                        "\nDimensiune: " + attachment.getFormattedSize(),
                        "Fișier Salvat", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Eroare la scrierea fișierului pe disc:\n" + ex.getMessage(),
                        "Eroare Salvare", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void promptCreateRoom() {
        if (!client.isConnected()) {
            JOptionPane.showMessageDialog(this, "Trebuie să fiți conectat la server pentru a crea o cameră!",
                    "Atenție", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String roomName = JOptionPane.showInputDialog(this, "Introduceți numele noii camere de chat:",
                "Creare Cameră de Chat", JOptionPane.QUESTION_MESSAGE);
        if (roomName != null && !roomName.trim().isEmpty()) {
            client.createRoom(roomName.trim());
        }
    }

    private void openHistoryDialog() {
        HistoryDialog dlg = new HistoryDialog(this, client.getCurrentRoom(), currentRoomMessages);
        dlg.setVisible(true);
    }

    private void initiateReply(Message message) {
        replyPanel.setReply(message, () -> messageInputField.requestFocusInWindow());
        messageInputField.requestFocusInWindow();
    }

    private void addMessageToChatUI(Message msg) {
        boolean isOwn = msg.getSender() != null && msg.getSender().equalsIgnoreCase(client.getUsername());
        MessageBubblePanel bubble = new MessageBubblePanel(
                msg,
                isOwn,
                this::initiateReply,
                this::saveFileToDisk
        );

        messagesContainer.add(bubble);
        messagesContainer.revalidate();
        messagesContainer.repaint();

        // Auto-scroll către ultimul mesaj sosit
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = messagesScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void clearMessagesUI() {
        messagesContainer.removeAll();
        messagesContainer.revalidate();
        messagesContainer.repaint();
    }

    // ================= IMPLEMENTARE ClientListener =================

    @Override
    public void onConnected(String host, int port, String username) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("● Conectat ca " + username);
            statusLabel.setForeground(new Color(34, 139, 34));

            connectButton.setText("Deconectare");
            connectButton.setEnabled(true);
            connectButton.setBackground(new Color(231, 76, 60));

            hostField.setEnabled(false);
            portField.setEnabled(false);
            usernameField.setEnabled(false);

            messageInputField.setEnabled(true);
            sendButton.setEnabled(true);
            attachFileButton.setEnabled(true);
            messageInputField.requestFocusInWindow();
        });
    }

    @Override
    public void onDisconnected(String reason) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("● Deconectat");
            statusLabel.setForeground(new Color(180, 50, 50));

            connectButton.setText("Conectare");
            connectButton.setEnabled(true);
            connectButton.setBackground(new Color(41, 128, 185));

            hostField.setEnabled(true);
            portField.setEnabled(true);
            usernameField.setEnabled(true);

            messageInputField.setEnabled(false);
            sendButton.setEnabled(false);
            attachFileButton.setEnabled(false);
            replyPanel.clearReply();

            usersListModel.clear();
        });
    }

    @Override
    public void onMessageReceived(Message message) {
        SwingUtilities.invokeLater(() -> {
            // Dacă mesajul aparține camerei curente, îl afișăm
            if (message.getRoom() == null || message.getRoom().equalsIgnoreCase(client.getCurrentRoom())) {
                currentRoomMessages.add(message);
                addMessageToChatUI(message);
            }
        });
    }

    @Override
    public void onHistoryReceived(String room, List<Message> history) {
        SwingUtilities.invokeLater(() -> {
            currentRoomMessages.clear();
            clearMessagesUI();

            if (history != null) {
                currentRoomMessages.addAll(history);
                for (Message msg : history) {
                    addMessageToChatUI(msg);
                }
            }

            currentRoomTitle.setText("# " + room);
        });
    }

    @Override
    public void onRoomListUpdated(List<String> rooms) {
        SwingUtilities.invokeLater(() -> {
            roomsListModel.clear();
            for (String r : rooms) {
                roomsListModel.addElement(r);
            }
            // Selectează camera curentă în listă
            roomsList.setSelectedValue(client.getCurrentRoom(), true);
        });
    }

    @Override
    public void onUserListUpdated(String room, List<String> users) {
        SwingUtilities.invokeLater(() -> {
            if (room != null && room.equalsIgnoreCase(client.getCurrentRoom())) {
                usersListModel.clear();
                for (String u : users) {
                    usersListModel.addElement(u);
                }
            }
        });
    }

    @Override
    public void onError(String errorMessage) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, errorMessage, "Eroare Rețea", JOptionPane.ERROR_MESSAGE);
        });
    }
}
