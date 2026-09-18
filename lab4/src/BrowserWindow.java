import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Fereastra principală a browser-ului web (Internet Browser POO).
 * Integrează bara de unelte cu motoarele de căutare, bara de semne de carte,
 * sistemul de file multiple (JTabbedPane) și bara de stare.
 */
public class BrowserWindow extends JFrame implements TabListener, FavoritesListener {

    private static final long serialVersionUID = 1L;

    private final transient HistoryManager historyManager;
    private final transient FavoritesManager favoritesManager;
    private final transient SearchEngineRegistry searchEngineRegistry;

    // Componente UI Bara Superioară
    private JButton btnBack;
    private JButton btnForward;
    private JButton btnRefresh;
    private JButton btnStop;
    private JButton btnHome;
    private JTextField addressField;
    private JComboBox<SearchEngine> searchEngineCombo;
    private JButton btnGo;
    private JButton btnFavorite;
    private JButton btnHistory;
    private JButton btnNewTab;

    // Bara de Favorite
    private BookmarksBar bookmarksBar;

    // Centru: Tab-uri multiple
    private JTabbedPane tabbedPane;

    // Bara de stare inferioară
    private JLabel statusLabel;
    private JLabel tabCountLabel;
    private JProgressBar globalProgressBar;

    // Regex pentru recunoașterea adreselor de tip URL/domeniu
    private static final Pattern URL_PATTERN = Pattern.compile("^([a-zA-Z0-9\\-]+\\.)+[a-zA-Z]{2,}(:\\d+)?(/.*)?$");

    public BrowserWindow(HistoryManager historyManager, FavoritesManager favoritesManager, SearchEngineRegistry searchEngineRegistry) {
        super("Internet Browser POO");
        this.historyManager = historyManager;
        this.favoritesManager = favoritesManager;
        this.searchEngineRegistry = searchEngineRegistry;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);

        initMenuBar();
        initUI();
        initKeyboardShortcuts();

        favoritesManager.addListener(this);

        // Deschidem primul tab la inițializare
        addNewTab("about:home");
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Meniu Fișier
        JMenu fileMenu = new JMenu("Fișier");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem newTabItem = new JMenuItem("Tab Nou");
        newTabItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        newTabItem.addActionListener(e -> addNewTab("about:home"));
        fileMenu.add(newTabItem);

        JMenuItem closeTabItem = new JMenuItem("Închide Tab-ul Curent");
        closeTabItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        closeTabItem.addActionListener(e -> closeCurrentTab());
        fileMenu.add(closeTabItem);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Ieșire");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // Meniu Navigare
        JMenu navMenu = new JMenu("Navigare");
        navMenu.setMnemonic(KeyEvent.VK_N);

        JMenuItem backItem = new JMenuItem("Înapoi");
        backItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.ALT_MASK));
        backItem.addActionListener(e -> {
            BrowserTab active = getActiveTab();
            if (active != null) active.goBack();
        });
        navMenu.add(backItem);

        JMenuItem forwardItem = new JMenuItem("Înainte");
        forwardItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.ALT_MASK));
        forwardItem.addActionListener(e -> {
            BrowserTab active = getActiveTab();
            if (active != null) active.goForward();
        });
        navMenu.add(forwardItem);

        JMenuItem refreshItem = new JMenuItem("Reîmprospătare");
        refreshItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        refreshItem.addActionListener(e -> {
            BrowserTab active = getActiveTab();
            if (active != null) active.reload();
        });
        navMenu.add(refreshItem);

        JMenuItem stopItem = new JMenuItem("Oprire Încărcare");
        stopItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        stopItem.addActionListener(e -> {
            BrowserTab active = getActiveTab();
            if (active != null) active.stop();
        });
        navMenu.add(stopItem);

        JMenuItem homeItem = new JMenuItem("Pagină de Start");
        homeItem.addActionListener(e -> navigateCurrent("about:home"));
        navMenu.add(homeItem);

        // Meniu Favorite
        JMenu favMenu = new JMenu("Favorite");
        favMenu.setMnemonic(KeyEvent.VK_A);

        JMenuItem addFavItem = new JMenuItem("Adaugă Pagina Curentă la Favorite...");
        addFavItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        addFavItem.addActionListener(e -> addCurrentToFavorites());
        favMenu.add(addFavItem);

        JMenuItem manageFavItem = new JMenuItem("Gestionare Favorite...");
        manageFavItem.addActionListener(e -> openFavoritesDialog());
        favMenu.add(manageFavItem);

        // Meniu Istoric
        JMenu histMenu = new JMenu("Istoric");
        histMenu.setMnemonic(KeyEvent.VK_I);

        JMenuItem showHistItem = new JMenuItem("Afișează Tot Istoricul...");
        showHistItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        showHistItem.addActionListener(e -> openHistoryDialog());
        histMenu.add(showHistItem);

        JMenuItem clearHistItem = new JMenuItem("Golește Istoricul");
        clearHistItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Sigur doriți să ștergeți tot istoricul?", "Confirmare", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                historyManager.clearHistory();
                statusLabel.setText("Istoricul a fost curățat.");
            }
        });
        histMenu.add(clearHistItem);

        // Meniu Ajutor
        JMenu helpMenu = new JMenu("Ajutor");
        JMenuItem shortcutsItem = new JMenuItem("Scurtături Tastatură & Despre");
        shortcutsItem.addActionListener(e -> navigateCurrent("about:help"));
        helpMenu.add(shortcutsItem);

        JMenuItem aboutItem = new JMenuItem("Despre Aplicatie");
        aboutItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(
                    this,
                    "Internet Browser POO - Java Swing\n"
                    + "Laborator 4: Paradigma Orientată pe Obiecte\n\n"
                    + "Funcționalități:\n"
                    + "• Bară de adrese inteligentă cu motoare de căutare integrate (DuckDuckGo, Google, Bing, Wikipedia)\n"
                    + "• Butoane Back / Forward cu stive dedicate pe fiecare tab\n"
                    + "• Butoane Stop / Refresh pentru controlul încărcării\n"
                    + "• Istoric cu căutare în timp real și salvare în fișier (history.txt)\n"
                    + "• Favorite salvate automat în fișier (favorites.txt) cu bară rapidă\n"
                    + "• File multiple (Tabbed browsing) cu deschidere și închidere dinamică\n",
                    "Despre Internet Browser POO",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(navMenu);
        menuBar.add(favMenu);
        menuBar.add(histMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void initUI() {
        JPanel northContainer = new JPanel(new BorderLayout());

        // 1. Bara principală de unelte (Toolbar)
        JPanel toolbar = new JPanel(new BorderLayout(6, 4));
        toolbar.setBorder(BorderFactory.createEmptyBorder(6, 8, 4, 8));
        toolbar.setBackground(new Color(242, 243, 245));

        // Secțiunea din stânga a barei de unelte: Navigare (Back, Forward, Refresh, Stop, Home)
        JPanel navButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        navButtonsPanel.setOpaque(false);

        btnBack = createToolButton("⬅", "Navighează Înapoi (Alt + Stânga)");
        btnBack.setEnabled(false);
        btnBack.addActionListener(e -> {
            BrowserTab active = getActiveTab();
            if (active != null) active.goBack();
        });

        btnForward = createToolButton("➡", "Navighează Înainte (Alt + Dreapta)");
        btnForward.setEnabled(false);
        btnForward.addActionListener(e -> {
            BrowserTab active = getActiveTab();
            if (active != null) active.goForward();
        });

        btnRefresh = createToolButton("🔄", "Reîmprospătează pagina (F5 sau Ctrl+R)");
        btnRefresh.addActionListener(e -> {
            BrowserTab active = getActiveTab();
            if (active != null) active.reload();
        });

        btnStop = createToolButton("⏹", "Oprește încărcarea paginii (Esc)");
        btnStop.setEnabled(false);
        btnStop.addActionListener(e -> {
            BrowserTab active = getActiveTab();
            if (active != null) active.stop();
        });

        btnHome = createToolButton("🏠", "Pagina de Start");
        btnHome.addActionListener(e -> navigateCurrent("about:home"));

        navButtonsPanel.add(btnBack);
        navButtonsPanel.add(btnForward);
        navButtonsPanel.add(btnRefresh);
        navButtonsPanel.add(btnStop);
        navButtonsPanel.add(btnHome);

        toolbar.add(navButtonsPanel, BorderLayout.WEST);

        // Secțiunea centrală: Bara de Adresă și Căutare + Selector Motor de Căutare
        JPanel addressPanel = new JPanel(new BorderLayout(4, 0));
        addressPanel.setOpaque(false);

        addressField = new JTextField();
        addressField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addressField.setBackground(new java.awt.Color(255,255,255));
        addressField.setForeground(new Color(44, 62, 80));
        addressField.setCaretColor(new java.awt.Color(30,136,229));
        addressField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(20, 100, 170), 2, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        addressField.setToolTipText("Introduceți un URL sau cuvinte cheie de căutare");

        // Selectare totală la click pentru tastare comodă
        addressField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                addressField.selectAll();
            }
        });

        // La apăsarea tastei ENTER se navighează sau se caută
        addressField.addActionListener(e -> handleAddressSubmit());

        addressPanel.add(addressField, BorderLayout.CENTER);

        // Secțiunea dreaptă a barei de adrese: Selector motor căutare + Buton Go
        JPanel enginePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        enginePanel.setOpaque(false);

        searchEngineCombo = new JComboBox<>();
        for (SearchEngine engine : searchEngineRegistry.getEngines()) {
            searchEngineCombo.addItem(engine);
        }
        searchEngineCombo.setSelectedItem(searchEngineRegistry.getDefaultEngine());
        searchEngineCombo.setToolTipText("Selectați motorul de căutare integrat");
        searchEngineCombo.setBackground(Color.WHITE);

        btnGo = createToolButton("🔍", "Mergi la adresă / Caută cu motorul selectat (Enter)");
        btnGo.setFocusable(false);
        btnGo.addActionListener(e -> handleAddressSubmit());

        enginePanel.add(searchEngineCombo);
        enginePanel.add(btnGo);

        addressPanel.add(enginePanel, BorderLayout.EAST);
        toolbar.add(addressPanel, BorderLayout.CENTER);

        // Secțiunea din extrema dreaptă a barei de unelte: Favorite, Istoric, Tab Nou
        JPanel rightButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        rightButtonsPanel.setOpaque(false);

        btnFavorite = createToolButton("⭐", "Adaugă / Elimină pagina curentă din Favorite (Ctrl+D)");
        btnFavorite.addActionListener(e -> addCurrentToFavorites());

        btnHistory = createToolButton("🕒", "Deschide Istoricul de navigare (Ctrl+H)");
        btnHistory.addActionListener(e -> openHistoryDialog());

        btnNewTab = createToolButton("➕", "Deschide un Tab Nou (Ctrl+T)");
        btnNewTab.setFont(new Font("Dialog", Font.BOLD, 13));
        btnNewTab.addActionListener(e -> addNewTab("about:home"));

        rightButtonsPanel.add(btnFavorite);
        rightButtonsPanel.add(btnHistory);
        rightButtonsPanel.add(btnNewTab);

        toolbar.add(rightButtonsPanel, BorderLayout.EAST);
        northContainer.add(toolbar, BorderLayout.NORTH);

        // 2. Bara de semne de carte (Bookmarks Bar)
        bookmarksBar = new BookmarksBar(
                favoritesManager,
                this::navigateCurrent,
                this::addNewTab,
                this::openFavoritesDialog
        );
        northContainer.add(bookmarksBar, BorderLayout.SOUTH);

        add(northContainer, BorderLayout.NORTH);

        // 3. Tab-uri multiple (JTabbedPane)
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                onTabSelected();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);

        // 4. Bara de stare inferioară
        JPanel statusBar = new JPanel(new BorderLayout(8, 0));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        statusBar.setBackground(new Color(245, 245, 245));

        statusLabel = new JLabel("Gata");
        statusLabel.setFont(new Font("Dialog", Font.PLAIN, 12));

        JPanel statusRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        statusRightPanel.setOpaque(false);

        globalProgressBar = new JProgressBar();
        globalProgressBar.setPreferredSize(new Dimension(100, 14));
        globalProgressBar.setVisible(false);

        tabCountLabel = new JLabel("Tab-uri: 1");
        tabCountLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
        tabCountLabel.setForeground(Color.DARK_GRAY);

        statusRightPanel.add(globalProgressBar);
        statusRightPanel.add(tabCountLabel);

        statusBar.add(statusLabel, BorderLayout.CENTER);
        statusBar.add(statusRightPanel, BorderLayout.EAST);

        add(statusBar, BorderLayout.SOUTH);
    }

    private JButton createToolButton(String text, String tooltip) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Dialog", Font.PLAIN, 14));
        btn.setToolTipText(tooltip);
        btn.setFocusable(false);
        btn.setPreferredSize(new Dimension(34, 30));
        return btn;
    }

    private void initKeyboardShortcuts() {
        JComponent root = getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();

        int shortcutMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        // Ctrl/Cmd + L: Focus pe bara de adrese
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, shortcutMask), "focusAddress");
        am.put("focusAddress", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addressField.requestFocusInWindow();
                addressField.selectAll();
            }
        });

        // F5: Refresh
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "refreshPage");
        am.put("refreshPage", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BrowserTab active = getActiveTab();
                if (active != null) active.reload();
            }
        });
    }

    // ==========================================
    // Gestionare Navigare și Căutare
    // ==========================================
    private void handleAddressSubmit() {
        String input = addressField.getText().trim();
        if (input.isEmpty()) return;

        String targetUrl;

        // Dacă începe cu un protocol cunoscut sau cu prefixul "about:"
        if (input.startsWith("http://") || input.startsWith("https://") || input.startsWith("file://") || input.startsWith("about:")) {
            targetUrl = input;
        } else if (!input.contains(" ") && URL_PATTERN.matcher(input).matches()) {
            // Este un domeniu web valid fără protocol (ex: "wikipedia.org" sau "google.com")
            targetUrl = "https://" + input;
        } else {
            // Este o căutare web! Folosim motorul de căutare selectat
            SearchEngine engine = (SearchEngine) searchEngineCombo.getSelectedItem();
            if (engine == null) {
                engine = searchEngineRegistry.getDefaultEngine();
            }
            targetUrl = engine.buildSearchUrl(input);
            statusLabel.setText("Căutare pe " + engine.getName() + ": " + input);
        }

        navigateCurrent(targetUrl);
    }

    public void navigateCurrent(String url) {
        BrowserTab active = getActiveTab();
        if (active != null) {
            active.loadUrl(url);
        } else {
            addNewTab(url);
        }
    }

    // ==========================================
    // Gestionare File (Tabs)
    // ==========================================
    public void addNewTab(String initialUrl) {
        BrowserTab newTab = new BrowserTab(historyManager, favoritesManager, this);
        tabbedPane.addTab("Tab Nou", newTab);

        int index = tabbedPane.indexOfComponent(newTab);
        TabHeaderComponent header = new TabHeaderComponent(tabbedPane, newTab, () -> closeTab(newTab));
        tabbedPane.setTabComponentAt(index, header);

        tabbedPane.setSelectedComponent(newTab);
        updateTabCount();

        if (initialUrl != null && !initialUrl.isEmpty()) {
            newTab.loadUrl(initialUrl);
        }
    }

    public void closeCurrentTab() {
        BrowserTab active = getActiveTab();
        if (active != null) {
            closeTab(active);
        }
    }

    public void closeTab(BrowserTab tabToClose) {
        int index = tabbedPane.indexOfComponent(tabToClose);
        if (index != -1) {
            tabToClose.stop();
            tabbedPane.remove(index);

            // Dacă s-a închis ultimul tab, deschidem automat o pagină de start curată
            if (tabbedPane.getTabCount() == 0) {
                addNewTab("about:home");
            } else {
                updateTabCount();
            }
        }
    }

    private BrowserTab getActiveTab() {
        int selected = tabbedPane.getSelectedIndex();
        if (selected != -1 && selected < tabbedPane.getTabCount()) {
            return (BrowserTab) tabbedPane.getComponentAt(selected);
        }
        return null;
    }

    private void onTabSelected() {
        BrowserTab active = getActiveTab();
        if (active != null) {
            addressField.setText(active.getCurrentUrl() != null ? active.getCurrentUrl() : "");
            btnBack.setEnabled(active.canGoBack());
            btnForward.setEnabled(active.canGoForward());
            btnStop.setEnabled(active.isLoading());
            btnRefresh.setEnabled(!active.isLoading());
            updateWindowTitle(active.getTitle());
            updateFavoriteButtonState();
        }
    }

    private void updateTabCount() {
        tabCountLabel.setText("Tab-uri: " + tabbedPane.getTabCount());
    }

    private void updateWindowTitle(String pageTitle) {
        if (pageTitle == null || pageTitle.trim().isEmpty() || pageTitle.equalsIgnoreCase("about:home")) {
            setTitle("Internet Browser POO");
        } else {
            setTitle(pageTitle + " - Internet Browser POO");
        }
    }

    // ==========================================
    // Gestionare Favorite
    // ==========================================
    private void addCurrentToFavorites() {
        BrowserTab active = getActiveTab();
        if (active == null) return;

        String url = active.getCurrentUrl();
        if (url == null || url.isEmpty() || url.startsWith("about:")) {
            JOptionPane.showMessageDialog(this, "Paginile interne nu pot fi adăugate la favorite.", "Informație", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String title = active.getTitle();
        if (title == null || title.isEmpty()) {
            title = url;
        }

        if (favoritesManager.isFavorite(url)) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Pagina \"" + title + "\" se află deja la Favorite.\nDoriți să o eliminați din favorite?",
                    "Semn de carte existent",
                    JOptionPane.YES_NO_OPTION
            );
            if (choice == JOptionPane.YES_OPTION) {
                favoritesManager.removeFavoriteByUrl(url);
                statusLabel.setText("Eliminat din favorite: " + title);
            }
        } else {
            String customTitle = JOptionPane.showInputDialog(this, "Denumire pentru Semnul de Carte:", title);
            if (customTitle != null && !customTitle.trim().isEmpty()) {
                favoritesManager.addFavorite(customTitle.trim(), url);
                statusLabel.setText("Adăugat la favorite: " + customTitle.trim());
            }
        }
        updateFavoriteButtonState();
    }

    private void updateFavoriteButtonState() {
        BrowserTab active = getActiveTab();
        if (active != null && favoritesManager.isFavorite(active.getCurrentUrl())) {
            btnFavorite.setForeground(new Color(230, 160, 0));
            btnFavorite.setToolTipText("Pagina curentă este la Favorite! Click pentru a elimina.");
        } else {
            btnFavorite.setForeground(Color.DARK_GRAY);
            btnFavorite.setToolTipText("Adaugă pagina curentă la Favorite (Ctrl+D)");
        }
    }

    private void openFavoritesDialog() {
        FavoritesDialog dialog = new FavoritesDialog(this, favoritesManager, this::navigateCurrent, this::addNewTab);
        dialog.setVisible(true);
    }

    private void openHistoryDialog() {
        HistoryDialog dialog = new HistoryDialog(this, historyManager, this::navigateCurrent, this::addNewTab);
        dialog.setVisible(true);
    }

    // ==========================================
    // Implementare TabListener (Observer)
    // ==========================================
    @Override
    public void onUrlChanged(BrowserTab tab, String newUrl) {
        if (tab == getActiveTab()) {
            addressField.setText(newUrl);
            updateFavoriteButtonState();
        }
    }

    @Override
    public void onTitleChanged(BrowserTab tab, String newTitle) {
        int index = tabbedPane.indexOfComponent(tab);
        if (index != -1) {
            TabHeaderComponent header = (TabHeaderComponent) tabbedPane.getTabComponentAt(index);
            if (header != null) {
                header.updateTitle(newTitle);
            }
        }
        if (tab == getActiveTab()) {
            updateWindowTitle(newTitle);
        }
    }

    @Override
    public void onLoadingStateChanged(BrowserTab tab, boolean isLoading) {
        if (tab == getActiveTab()) {
            btnStop.setEnabled(isLoading);
            btnRefresh.setEnabled(!isLoading);
            globalProgressBar.setVisible(isLoading);
            globalProgressBar.setIndeterminate(isLoading);
        }
    }

    @Override
    public void onStatusMessage(BrowserTab tab, String message) {
        if (tab == getActiveTab()) {
            statusLabel.setText(message);
        }
    }

    @Override
    public void onNavigationStateChanged(BrowserTab tab, boolean canGoBack, boolean canGoForward) {
        if (tab == getActiveTab()) {
            btnBack.setEnabled(canGoBack);
            btnForward.setEnabled(canGoForward);
        }
    }

    @Override
    public void onRequestNewTab(String url) {
        addNewTab(url);
    }

    // ==========================================
    // Implementare FavoritesListener (Observer)
    // ==========================================
    @Override
    public void onFavoritesChanged() {
        updateFavoriteButtonState();
    }
}
