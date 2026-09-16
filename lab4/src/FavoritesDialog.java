import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
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
import javax.swing.table.DefaultTableModel;

/**
 * Fereastră de dialog pentru gestionarea completă a paginilor Favorite (Bookmarks).
 * Permite adăugarea manuală, editarea titlului, ștergerea și deschiderea semnelor de carte.
 */
public class FavoritesDialog extends JDialog implements FavoritesListener {

    private static final long serialVersionUID = 1L;

    private final transient FavoritesManager favoritesManager;
    private final transient Consumer<String> onNavigateUrl;
    private final transient Consumer<String> onOpenInNewTab;

    private JTable favoritesTable;
    private DefaultTableModel tableModel;
    private transient List<FavoriteItem> currentList;

    public FavoritesDialog(Frame owner, FavoritesManager favoritesManager,
                           Consumer<String> onNavigateUrl,
                           Consumer<String> onOpenInNewTab) {
        super(owner, "Gestionare Favorite (Bookmarks) - Internet Browser", true);
        this.favoritesManager = favoritesManager;
        this.onNavigateUrl = onNavigateUrl;
        this.onOpenInNewTab = onOpenInNewTab;

        setSize(750, 420);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));

        favoritesManager.addListener(this);

        initUI();
        refreshTable();
    }

    private void initUI() {
        // Antet cu descriere și fișier de salvare
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        String fileName = (favoritesManager.getStorageFile() != null) ? favoritesManager.getStorageFile().getName() : "favorites.txt";
        JLabel infoLabel = new JLabel("⭐ Favoritele sunt salvate automat în fișierul: " + fileName);
        headerPanel.add(infoLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Tabelul cu favoritele
        String[] columnNames = {"Titlu Semn de Carte", "Adresă URL", "Data Adăugării"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        favoritesTable = new JTable(tableModel);
        favoritesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        favoritesTable.getColumnModel().getColumn(0).setPreferredWidth(220);
        favoritesTable.getColumnModel().getColumn(1).setPreferredWidth(340);
        favoritesTable.getColumnModel().getColumn(2).setPreferredWidth(140);

        // Dublu-click pentru navigare rapidă
        favoritesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && favoritesTable.getSelectedRow() != -1) {
                    openSelected(false);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(favoritesTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        add(scrollPane, BorderLayout.CENTER);

        // Panoul de butoane jos
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));

        JButton openBtn = new JButton("Deschide");
        openBtn.addActionListener(e -> openSelected(false));

        JButton openNewTabBtn = new JButton("Deschide în Tab Nou");
        openNewTabBtn.addActionListener(e -> openSelected(true));

        JButton addBtn = new JButton("Adaugă Manual...");
        addBtn.addActionListener(e -> showAddDialog());

        JButton editBtn = new JButton("Editează Titlu");
        editBtn.addActionListener(e -> editSelectedTitle());

        JButton deleteBtn = new JButton("Șterge");
        deleteBtn.addActionListener(e -> deleteSelected());

        JButton closeBtn = new JButton("Închide");
        closeBtn.addActionListener(e -> {
            favoritesManager.removeListener(this);
            dispose();
        });

        bottomPanel.add(openBtn);
        bottomPanel.add(openNewTabBtn);
        bottomPanel.add(addBtn);
        bottomPanel.add(editBtn);
        bottomPanel.add(deleteBtn);
        bottomPanel.add(closeBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void refreshTable() {
        currentList = favoritesManager.getFavorites();
        tableModel.setRowCount(0);
        for (FavoriteItem fav : currentList) {
            tableModel.addRow(new Object[]{
                    fav.getTitle(),
                    fav.getUrl(),
                    fav.getFormattedDateAdded()
            });
        }
    }

    private void openSelected(boolean inNewTab) {
        int selectedRow = favoritesTable.getSelectedRow();
        if (selectedRow != -1 && currentList != null && selectedRow < currentList.size()) {
            FavoriteItem item = currentList.get(selectedRow);
            if (inNewTab) {
                if (onOpenInNewTab != null) onOpenInNewTab.accept(item.getUrl());
            } else {
                if (onNavigateUrl != null) onNavigateUrl.accept(item.getUrl());
            }
            favoritesManager.removeListener(this);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Selectați un semn de carte din tabel.", "Atenție", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showAddDialog() {
        JTextField titleField = new JTextField();
        JTextField urlField = new JTextField("https://");

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        formPanel.add(new JLabel("Titlu:"));
        formPanel.add(titleField);
        formPanel.add(new JLabel("URL:"));
        formPanel.add(urlField);

        int result = JOptionPane.showConfirmDialog(this, formPanel, "Adaugă Favorit Nou", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            String url = urlField.getText().trim();
            if (!url.isEmpty() && !url.equals("https://")) {
                if (title.isEmpty()) title = url;
                favoritesManager.addFavorite(title, url);
            }
        }
    }

    private void editSelectedTitle() {
        int selectedRow = favoritesTable.getSelectedRow();
        if (selectedRow != -1 && currentList != null && selectedRow < currentList.size()) {
            FavoriteItem item = currentList.get(selectedRow);
            String newTitle = JOptionPane.showInputDialog(this, "Modificați titlul favoritului:", item.getTitle());
            if (newTitle != null && !newTitle.trim().isEmpty()) {
                item.setTitle(newTitle.trim());
                favoritesManager.saveToFile();
                refreshTable();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selectați un semn de carte pentru editare.", "Atenție", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteSelected() {
        int selectedRow = favoritesTable.getSelectedRow();
        if (selectedRow != -1 && currentList != null && selectedRow < currentList.size()) {
            FavoriteItem item = currentList.get(selectedRow);
            favoritesManager.removeFavoriteByUrl(item.getUrl());
        } else {
            JOptionPane.showMessageDialog(this, "Selectați un semn de carte pentru ștergere.", "Atenție", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public void onFavoritesChanged() {
        refreshTable();
    }
}
