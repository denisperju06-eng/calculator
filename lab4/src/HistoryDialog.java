import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/**
 * Fereastră de dialog pentru vizualizarea, filtrarea și gestionarea istoricului de navigare.
 */
public class HistoryDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final transient HistoryManager historyManager;
    private final transient Consumer<String> onNavigateUrl;
    private final transient Consumer<String> onOpenInNewTab;

    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private transient List<HistoryItem> currentDisplayList;

    public HistoryDialog(Frame owner, HistoryManager historyManager,
                         Consumer<String> onNavigateUrl,
                         Consumer<String> onOpenInNewTab) {
        super(owner, "Istoric Navigare - Internet Browser", true);
        this.historyManager = historyManager;
        this.onNavigateUrl = onNavigateUrl;
        this.onOpenInNewTab = onOpenInNewTab;

        setSize(780, 480);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));

        initUI();
        refreshTable();
    }

    private void initUI() {
        // Panoul superior de filtrare / căutare
        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JLabel searchLabel = new JLabel("🔍 Filtrare istoric: ");
        searchField = new JTextField();
        searchField.setToolTipText("Introduceți un cuvânt cheie pentru a căuta în titluri sau URL-uri");

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filter(); }
        });

        topPanel.add(searchLabel, BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Tabelul cu istoricul
        String[] columnNames = {"Data & Ora", "Titlu Pagină", "Adresă URL"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(260);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(320);

        // Dublu click pentru navigare rapidă
        historyTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && historyTable.getSelectedRow() != -1) {
                    openSelected(false);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        add(scrollPane, BorderLayout.CENTER);

        // Panoul inferior de acțiuni
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));

        JButton openBtn = new JButton("Deschide");
        openBtn.addActionListener(e -> openSelected(false));

        JButton openNewTabBtn = new JButton("Deschide în Tab Nou");
        openNewTabBtn.addActionListener(e -> openSelected(true));

        JButton deleteBtn = new JButton("Șterge");
        deleteBtn.addActionListener(e -> deleteSelected());

        JButton clearAllBtn = new JButton("Golește Tot Istoricul");
        clearAllBtn.addActionListener(e -> clearAll());

        JButton closeBtn = new JButton("Închide");
        closeBtn.addActionListener(e -> dispose());

        bottomPanel.add(openBtn);
        bottomPanel.add(openNewTabBtn);
        bottomPanel.add(deleteBtn);
        bottomPanel.add(clearAllBtn);
        bottomPanel.add(closeBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void filter() {
        String query = searchField.getText();
        currentDisplayList = historyManager.search(query);
        populateModel(currentDisplayList);
    }

    private void refreshTable() {
        currentDisplayList = historyManager.getHistory();
        populateModel(currentDisplayList);
    }

    private void populateModel(List<HistoryItem> items) {
        tableModel.setRowCount(0);
        for (HistoryItem item : items) {
            tableModel.addRow(new Object[]{
                    item.getFormattedTimestamp(),
                    item.getTitle(),
                    item.getUrl()
            });
        }
    }

    private void openSelected(boolean inNewTab) {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow != -1 && currentDisplayList != null && selectedRow < currentDisplayList.size()) {
            HistoryItem item = currentDisplayList.get(selectedRow);
            if (inNewTab) {
                if (onOpenInNewTab != null) onOpenInNewTab.accept(item.getUrl());
            } else {
                if (onNavigateUrl != null) onNavigateUrl.accept(item.getUrl());
            }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Selectați o intrare din tabel.", "Atenție", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteSelected() {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow != -1 && currentDisplayList != null && selectedRow < currentDisplayList.size()) {
            HistoryItem item = currentDisplayList.get(selectedRow);
            historyManager.removeEntry(item);
            filter();
        } else {
            JOptionPane.showMessageDialog(this, "Selectați o intrare pe care doriți să o ștergeți.", "Atenție", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void clearAll() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Sigur doriți să ștergeți întregul istoric de navigare?",
                "Confirmare ștergere istoric",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            historyManager.clearHistory();
            filter();
        }
    }
}
