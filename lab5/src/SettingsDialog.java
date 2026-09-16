import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

/**
 * Fereastră de dialog pentru configurarea intervalului de recitire și a combinațiilor de taste (hotkeys).
 * 
 * Cerința c: Setarea perioadei de recitire a informației din Internet.
 * Cerința e: Setarea combinațiilor de taste (hotkeys) active pentru aplicația rezident.
 */
public class SettingsDialog extends JDialog {

    private final HotkeyManager hotkeyManager;
    private final PeriodicScheduler scheduler;
    private final Runnable onSettingsChangedCallback;

    private JSpinner intervalSpinner;
    private JTable hotkeyTable;
    private DefaultTableModel tableModel;
    private JTextField keyCaptureField;
    private KeyStroke capturedKeyStroke = null;

    public SettingsDialog(Frame parent, HotkeyManager hotkeyManager, PeriodicScheduler scheduler, Runnable onSettingsChangedCallback) {
        super(parent, "Configurare Aplicație Rezidentă", true);
        this.hotkeyManager = hotkeyManager;
        this.scheduler = scheduler;
        this.onSettingsChangedCallback = onSettingsChangedCallback;

        initComponents();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. Secțiunea Interval Recitire (Cerința c)
        JPanel intervalPanel = new JPanel(new GridBagLayout());
        intervalPanel.setBorder(BorderFactory.createTitledBorder("Perioadă de Recitire (Cerința c)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        intervalPanel.add(new JLabel("Interval actualizare (secunde):"), gbc);

        int currentInterval = scheduler != null ? scheduler.getIntervalSeconds() : 30;
        intervalSpinner = new JSpinner(new SpinnerNumberModel(currentInterval, 5, 3600, 5));
        gbc.gridx = 1; gbc.gridy = 0;
        intervalPanel.add(intervalSpinner, gbc);

        JLabel presetLabel = new JLabel("Presetări rapide:");
        gbc.gridx = 0; gbc.gridy = 1;
        intervalPanel.add(presetLabel, gbc);

        String[] presets = {"10 secunde", "30 secunde", "1 minut", "5 minute", "15 minute"};
        int[] presetVals = {10, 30, 60, 300, 900};
        JComboBox<String> presetCombo = new JComboBox<>(presets);
        // Selectează presetul cel mai apropiat
        for (int i = 0; i < presetVals.length; i++) {
            if (presetVals[i] == currentInterval) presetCombo.setSelectedIndex(i);
        }
        presetCombo.addActionListener(e -> {
            int idx = presetCombo.getSelectedIndex();
            if (idx >= 0 && idx < presetVals.length) {
                intervalSpinner.setValue(presetVals[idx]);
            }
        });
        gbc.gridx = 1; gbc.gridy = 1;
        intervalPanel.add(presetCombo, gbc);

        contentPanel.add(intervalPanel);
        contentPanel.add(Box.createVerticalStrut(15));

        // 2. Secțiunea Combinatii de taste (Cerința e)
        JPanel hotkeyPanel = new JPanel(new BorderLayout(8, 8));
        hotkeyPanel.setBorder(BorderFactory.createTitledBorder("Combinatii de Taste Active - Hotkeys (Cerința e)"));

        String[] colNames = {"ID Comandă", "Acțiune", "Tastă Rapidă (KeyStroke)", "Descriere"};
        tableModel = new DefaultTableModel(colNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        refreshHotkeyTableData();

        hotkeyTable = new JTable(tableModel);
        hotkeyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        hotkeyTable.setRowHeight(24);
        JScrollPane scrollPane = new JScrollPane(hotkeyTable);
        scrollPane.setPreferredSize(new Dimension(540, 140));
        hotkeyPanel.add(scrollPane, BorderLayout.CENTER);

        // Editor de capturare a tastelor
        JPanel capturePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        capturePanel.add(new JLabel("Apasă combinația dorită:"));

        keyCaptureField = new JTextField(15);
        keyCaptureField.setEditable(false);
        keyCaptureField.setBackground(Color.WHITE);
        keyCaptureField.setFont(new Font("Monospaced", Font.BOLD, 12));
        keyCaptureField.setText("Clic aici & apasă tastele");

        keyCaptureField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                // Ignorăm apăsările izolate de taste modificatoare
                if (code == KeyEvent.VK_CONTROL || code == KeyEvent.VK_SHIFT || 
                    code == KeyEvent.VK_ALT || code == KeyEvent.VK_META) {
                    return;
                }

                int modifiers = e.getModifiersEx() & (KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK | 
                                                      KeyEvent.SHIFT_DOWN_MASK | KeyEvent.META_DOWN_MASK);

                capturedKeyStroke = KeyStroke.getKeyStroke(code, modifiers);
                HotkeyBinding temp = new HotkeyBinding("tmp", "tmp", "tmp", capturedKeyStroke, () -> {});
                keyCaptureField.setText(temp.getDisplayText());
                e.consume();
            }
        });
        capturePanel.add(keyCaptureField);

        JButton assignButton = new JButton("Asociază Acțiunii Selectate");
        assignButton.addActionListener(e -> {
            int selRow = hotkeyTable.getSelectedRow();
            if (selRow < 0) {
                JOptionPane.showMessageDialog(this, "Selectați mai întâi o acțiune din tabel!", 
                        "Atenție", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (capturedKeyStroke == null) {
                JOptionPane.showMessageDialog(this, "Apăsați o combinație de taste în câmpul de captură!", 
                        "Atenție", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String actionId = (String) tableModel.getValueAt(selRow, 0);
            hotkeyManager.rebindHotkey(actionId, capturedKeyStroke);
            refreshHotkeyTableData();
            JOptionPane.showMessageDialog(this, "Combinația a fost actualizată cu succes!", 
                    "Succes", JOptionPane.INFORMATION_MESSAGE);
        });
        capturePanel.add(assignButton);

        hotkeyPanel.add(capturePanel, BorderLayout.SOUTH);
        contentPanel.add(hotkeyPanel);

        add(contentPanel, BorderLayout.CENTER);

        // 3. Butoane de jos (Salvare / Închidere)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton saveButton = new JButton("Salvează & Aplică");
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        saveButton.addActionListener(e -> {
            int newInterval = (Integer) intervalSpinner.getValue();
            if (scheduler != null) {
                scheduler.setIntervalSeconds(newInterval);
            }
            if (onSettingsChangedCallback != null) {
                onSettingsChangedCallback.run();
            }
            dispose();
        });

        JButton closeButton = new JButton("Închide");
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void refreshHotkeyTableData() {
        tableModel.setRowCount(0);
        if (hotkeyManager != null) {
            List<HotkeyBinding> list = hotkeyManager.getAllBindings();
            for (HotkeyBinding b : list) {
                tableModel.addRow(new Object[]{
                        b.getId(),
                        b.getName(),
                        b.getDisplayText(),
                        b.getDescription()
                });
            }
        }
    }
}
