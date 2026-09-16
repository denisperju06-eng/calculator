import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Interfață grafică Swing pentru administrarea și monitorizarea Serverului de Chat.
 * Oferă vizualizarea în timp real a conexiunilor, camerelor active și jurnalului de activitate.
 */
public class ServerGUI extends JFrame implements ServerEventListener {
    private Server server;
    private JTextField portField;
    private JButton toggleServerButton;
    private JLabel statusLabel;
    private JTextArea logArea;

    private DefaultListModel<String> roomsListModel;
    private JList<String> roomsList;
    private DefaultListModel<String> clientsListModel;
    private JList<String> clientsList;

    public ServerGUI() {
        super("Server Chat Rețea Locală - Panou de Control");
        setupUI();
    }

    private void setupUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 600);
        setMinimumSize(new Dimension(750, 480));
        setLocationRelativeTo(null);

        // Aspect vizual plăcut
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // ================= TOP BAR =================
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 215, 220)),
                BorderFactory.createEmptyBorder(0, 0, 10, 0)
        ));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controlPanel.add(new JLabel("Port Server:"));
        portField = new JTextField(String.valueOf(Server.DEFAULT_PORT), 6);
        controlPanel.add(portField);

        toggleServerButton = new JButton("▶ Pornește Server");
        toggleServerButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        toggleServerButton.setBackground(new Color(46, 139, 87));
        toggleServerButton.setForeground(Color.BLACK);
        toggleServerButton.addActionListener(e -> toggleServer());
        controlPanel.add(toggleServerButton);

        statusLabel = new JLabel("● Server Oprit", SwingConstants.RIGHT);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        statusLabel.setForeground(new Color(180, 40, 40));

        topBar.add(controlPanel, BorderLayout.WEST);
        topBar.add(statusLabel, BorderLayout.EAST);
        mainPanel.add(topBar, BorderLayout.NORTH);

        // ================= CENTER (SPLIT PANE) =================
        // Panou stânga: Camere și Utilizatori Conectați
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 10));

        JPanel roomsPanel = new JPanel(new BorderLayout(5, 5));
        roomsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(190, 200, 210)), "Camere de Chat Active"));
        roomsListModel = new DefaultListModel<>();
        roomsList = new JList<>(roomsListModel);
        roomsList.setFont(new Font("SansSerif", Font.PLAIN, 12));
        roomsPanel.add(new JScrollPane(roomsList), BorderLayout.CENTER);

        JPanel clientsPanel = new JPanel(new BorderLayout(5, 5));
        clientsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(190, 200, 210)), "Clienți Conectați"));
        clientsListModel = new DefaultListModel<>();
        clientsList = new JList<>(clientsListModel);
        clientsList.setFont(new Font("SansSerif", Font.PLAIN, 12));
        clientsPanel.add(new JScrollPane(clientsList), BorderLayout.CENTER);

        leftPanel.add(roomsPanel);
        leftPanel.add(clientsPanel);
        leftPanel.setPreferredSize(new Dimension(240, 400));

        // Panou dreapta: Jurnal evenimente (Log)
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(190, 200, 210)), "Jurnal Activitate Server"));
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(new Color(250, 252, 255));
        JScrollPane logScroll = new JScrollPane(logArea);
        rightPanel.add(logScroll, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(260);
        splitPane.setResizeWeight(0.25);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // ================= BOTTOM BAR =================
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton clearLogBtn = new JButton("Curăță Jurnal");
        clearLogBtn.addActionListener(e -> logArea.setText(""));

        JButton openHistoryBtn = new JButton("📁 Deschide Folder Istoric");
        openHistoryBtn.addActionListener(e -> openHistoryFolder());

        bottomBar.add(openHistoryBtn);
        bottomBar.add(clearLogBtn);
        mainPanel.add(bottomBar, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // Auto-start la pornirea interfeței grafice pentru confort maxim
        toggleServer();
    }

    private void toggleServer() {
        if (server == null || !server.isRunning()) {
            int port;
            try {
                port = Integer.parseInt(portField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Port invalid! Introduceți un număr între 1024 și 65535.",
                        "Eroare", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                server = new Server(port);
                server.addEventListener(this);
                server.start();

                portField.setEnabled(false);
                toggleServerButton.setText("⏹ Oprește Server");
                toggleServerButton.setBackground(new Color(220, 53, 69));
                refreshData();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Nu s-a putut porni serverul pe portul " + port + ":\n" + e.getMessage(),
                        "Eroare Pornire", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            server.stop();
            portField.setEnabled(true);
            toggleServerButton.setText("▶ Pornește Server");
            toggleServerButton.setBackground(new Color(46, 139, 87));
            clientsListModel.clear();
        }
    }

    private void openHistoryFolder() {
        if (server != null) {
            File dir = server.getHistoryManager().getStorageDir();
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(dir);
                } else {
                    JOptionPane.showMessageDialog(this, "Calea către istoric: " + dir.getAbsolutePath(),
                            "Folder Istoric", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Calea către istoric: " + dir.getAbsolutePath(),
                        "Folder Istoric", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void refreshData() {
        if (server != null && server.isRunning()) {
            roomsListModel.clear();
            for (String room : server.getRoomNames()) {
                ChatRoom cr = server.getRooms().get(room);
                int count = (cr != null) ? cr.getParticipantCount() : 0;
                roomsListModel.addElement(room + " (" + count + " utilizatori)");
            }
        }
    }

    // ================= IMPLEMENTARE ServerEventListener =================

    @Override
    public void onLog(String logMessage) {
        SwingUtilities.invokeLater(() -> {
            String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
            logArea.append("[" + time + "] " + logMessage + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    @Override
    public void onClientConnected(String username, String address) {
        SwingUtilities.invokeLater(() -> {
            clientsListModel.addElement(username + " [" + address + "]");
            refreshData();
        });
    }

    @Override
    public void onClientDisconnected(String username) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < clientsListModel.size(); i++) {
                if (clientsListModel.get(i).startsWith(username + " ")) {
                    clientsListModel.remove(i);
                    break;
                }
            }
            refreshData();
        });
    }

    @Override
    public void onRoomCreated(String roomName) {
        SwingUtilities.invokeLater(this::refreshData);
    }

    @Override
    public void onServerStatusChanged(boolean isRunning, int port) {
        SwingUtilities.invokeLater(() -> {
            if (isRunning) {
                statusLabel.setText("● Server Activ (Port " + port + ")");
                statusLabel.setForeground(new Color(34, 139, 34));
            } else {
                statusLabel.setText("● Server Oprit");
                statusLabel.setForeground(new Color(180, 40, 40));
                clientsListModel.clear();
            }
        });
    }
}
