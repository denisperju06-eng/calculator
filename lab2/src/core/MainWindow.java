package core;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * Fereastra principală a aplicației Redactor Text.
 * Coordonează componentele principale (TabbedEditorPane, FormatToolBar, StatusBar, MenuBar).
 */
public class MainWindow extends JFrame {

    private final StyleService styleService;
    private final SearchService searchService;
    private final FileService fileService;

    private TabbedEditorPane tabbedPane;
    private FormatToolBar formatToolBar;
    private StatusBar statusBar;
    private FindReplaceDialog findReplaceDialog;

    public MainWindow() {
        super("Redactor Text");

        // Inițializare servicii
        this.styleService = new StyleService();
        this.searchService = new SearchService();
        this.fileService = new FileService();

        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(950, 700);
        setMinimumSize(new Dimension(650, 450));
        setLocationRelativeTo(null);

        // Componente principale
        this.tabbedPane = new TabbedEditorPane(fileService);
        this.formatToolBar = new FormatToolBar(styleService);
        this.statusBar = new StatusBar();
        this.findReplaceDialog = new FindReplaceDialog(this, searchService, tabbedPane);

        // Conectare evenimente între taburi, bara de formatare și bara de stare
        tabbedPane.addActiveTabChangeListener(this::onActiveTabChanged);

        // Construire Meniu
        setJMenuBar(createMenuBar());

        // Panoul superior: Toolbar cu acțiuni rapide și controale de formatare
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(createQuickActionsToolBar(), BorderLayout.NORTH);
        topPanel.add(formatToolBar, BorderLayout.SOUTH);

        // Layout fereastră
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        getContentPane().add(statusBar, BorderLayout.SOUTH);

        // Adăugăm primul tab inițial
        tabbedPane.addNewTab();

        // Gestionare închidere fereastră cu salvare
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });
    }

    private JToolBar createQuickActionsToolBar() {
        JToolBar toolBar = new JToolBar("Acțiuni rapide");
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 230, 235)));

        JButton newBtn = new JButton("Nou");
        newBtn.setToolTipText("Creează o filă nouă (Ctrl+T)");
        newBtn.addActionListener(e -> tabbedPane.addNewTab());
        toolBar.add(newBtn);

        JButton openBtn = new JButton("Deschide");
        openBtn.setToolTipText("Deschide fișier (Ctrl+O)");
        openBtn.addActionListener(e -> openFile());
        toolBar.add(openBtn);

        JButton saveBtn = new JButton("Salvează");
        saveBtn.setToolTipText("Salvează fișierul curent (Ctrl+S)");
        saveBtn.addActionListener(e -> saveCurrentFile());
        toolBar.add(saveBtn);

        toolBar.addSeparator();

        JButton undoBtn = new JButton("Undo");
        undoBtn.setToolTipText("Anulează ultima acțiune (Ctrl+Z)");
        undoBtn.addActionListener(e -> {
            TextEditorTab tab = tabbedPane.getActiveEditorTab();
            if (tab != null) tab.undo();
        });
        toolBar.add(undoBtn);

        JButton redoBtn = new JButton("Redo");
        redoBtn.setToolTipText("Refă acțiunea anulată (Ctrl+Y)");
        redoBtn.addActionListener(e -> {
            TextEditorTab tab = tabbedPane.getActiveEditorTab();
            if (tab != null) tab.redo();
        });
        toolBar.add(redoBtn);

        toolBar.addSeparator();

        JButton cutBtn = new JButton("Taie");
        cutBtn.setToolTipText("Taie textul selectat (Ctrl+X)");
        cutBtn.addActionListener(e -> {
            TextEditorTab tab = tabbedPane.getActiveEditorTab();
            if (tab != null) tab.getTextPane().cut();
        });
        toolBar.add(cutBtn);

        JButton copyBtn = new JButton("Copiază");
        copyBtn.setToolTipText("Copiază textul selectat (Ctrl+C)");
        copyBtn.addActionListener(e -> {
            TextEditorTab tab = tabbedPane.getActiveEditorTab();
            if (tab != null) tab.getTextPane().copy();
        });
        toolBar.add(copyBtn);

        JButton pasteBtn = new JButton("Lipește");
        pasteBtn.setToolTipText("Lipește textul din clipboard (Ctrl+V)");
        pasteBtn.addActionListener(e -> {
            TextEditorTab tab = tabbedPane.getActiveEditorTab();
            if (tab != null) tab.getTextPane().paste();
        });
        toolBar.add(pasteBtn);

        toolBar.addSeparator();

        JButton findBtn = new JButton("Caută & Înlocuiește");
        findBtn.setToolTipText("Deschide fereastra de căutare și înlocuire (Ctrl+F)");
        findBtn.addActionListener(e -> showFindReplaceDialog());
        toolBar.add(findBtn);

        return toolBar;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        int shortcutKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        // 1. Meniu FIȘIER
        JMenu fileMenu = new JMenu("Fișier");
        fileMenu.setMnemonic('F');

        JMenuItem newItem = new JMenuItem("Filă nouă");
        newItem.setAccelerator(KeyStroke.getKeyStroke('T', shortcutKey));
        newItem.addActionListener(e -> tabbedPane.addNewTab());
        fileMenu.add(newItem);

        JMenuItem openItem = new JMenuItem("Deschide fișier...");
        openItem.setAccelerator(KeyStroke.getKeyStroke('O', shortcutKey));
        openItem.addActionListener(e -> openFile());
        fileMenu.add(openItem);

        fileMenu.addSeparator();

        JMenuItem saveItem = new JMenuItem("Salvează");
        saveItem.setAccelerator(KeyStroke.getKeyStroke('S', shortcutKey));
        saveItem.addActionListener(e -> saveCurrentFile());
        fileMenu.add(saveItem);

        JMenuItem saveAsItem = new JMenuItem("Salvează ca...");
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke('S', shortcutKey | java.awt.event.InputEvent.SHIFT_DOWN_MASK));
        saveAsItem.addActionListener(e -> saveAsCurrentFile());
        fileMenu.add(saveAsItem);

        fileMenu.addSeparator();

        JMenuItem closeTabItem = new JMenuItem("Închide fila");
        closeTabItem.setAccelerator(KeyStroke.getKeyStroke('W', shortcutKey));
        closeTabItem.addActionListener(e -> tabbedPane.closeCurrentTab());
        fileMenu.add(closeTabItem);

        JMenuItem exitItem = new JMenuItem("Ieșire");
        exitItem.setAccelerator(KeyStroke.getKeyStroke('Q', shortcutKey));
        exitItem.addActionListener(e -> handleExit());
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        // 2. Meniu EDITARE
        JMenu editMenu = new JMenu("Editare");
        editMenu.setMnemonic('E');

        JMenuItem undoItem = new JMenuItem("Anulează (Undo)");
        undoItem.setAccelerator(KeyStroke.getKeyStroke('Z', shortcutKey));
        undoItem.addActionListener(e -> {
            TextEditorTab tab = tabbedPane.getActiveEditorTab();
            if (tab != null) tab.undo();
        });
        editMenu.add(undoItem);

        JMenuItem redoItem = new JMenuItem("Refă (Redo)");
        redoItem.setAccelerator(KeyStroke.getKeyStroke('Y', shortcutKey));
        redoItem.addActionListener(e -> {
            TextEditorTab tab = tabbedPane.getActiveEditorTab();
            if (tab != null) tab.redo();
        });
        editMenu.add(redoItem);

        editMenu.addSeparator();

        JMenuItem cutItem = new JMenuItem("Taie");
        cutItem.setAccelerator(KeyStroke.getKeyStroke('X', shortcutKey));
        cutItem.addActionListener(e -> {
            TextEditorTab tab = tabbedPane.getActiveEditorTab();
            if (tab != null) tab.getTextPane().cut();
        });
        editMenu.add(cutItem);

        JMenuItem copyItem = new JMenuItem("Copiază");
        copyItem.setAccelerator(KeyStroke.getKeyStroke('C', shortcutKey));
        copyItem.addActionListener(e -> {
            TextEditorTab tab = tabbedPane.getActiveEditorTab();
            if (tab != null) tab.getTextPane().copy();
        });
        editMenu.add(copyItem);

        JMenuItem pasteItem = new JMenuItem("Lipește");
        pasteItem.setAccelerator(KeyStroke.getKeyStroke('V', shortcutKey));
        pasteItem.addActionListener(e -> {
            TextEditorTab tab = tabbedPane.getActiveEditorTab();
            if (tab != null) tab.getTextPane().paste();
        });
        editMenu.add(pasteItem);

        editMenu.addSeparator();

        JMenuItem selectAllItem = new JMenuItem("Selectează tot");
        selectAllItem.setAccelerator(KeyStroke.getKeyStroke('A', shortcutKey));
        selectAllItem.addActionListener(e -> {
            TextEditorTab tab = tabbedPane.getActiveEditorTab();
            if (tab != null) tab.getTextPane().selectAll();
        });
        editMenu.add(selectAllItem);

        menuBar.add(editMenu);

        // 3. Meniu FORMAT
        JMenu formatMenu = new JMenu("Format");
        formatMenu.setMnemonic('R');

        JMenuItem boldItem = new JMenuItem("Aldin (Bold)");
        boldItem.setAccelerator(KeyStroke.getKeyStroke('B', shortcutKey));
        boldItem.addActionListener(e -> {
            TextEditorTab tab = tabbedPane.getActiveEditorTab();
            if (tab != null) {
                styleService.toggleBold(tab.getTextPane());
            }
        });
        formatMenu.add(boldItem);

        JMenuItem italicItem = new JMenuItem("Cursiv (Italic)");
        italicItem.setAccelerator(KeyStroke.getKeyStroke('I', shortcutKey));
        italicItem.addActionListener(e -> {
            TextEditorTab tab = tabbedPane.getActiveEditorTab();
            if (tab != null) {
                styleService.toggleItalic(tab.getTextPane());
            }
        });
        formatMenu.add(italicItem);

        JMenuItem underlineItem = new JMenuItem("Subliniat (Underline)");
        underlineItem.setAccelerator(KeyStroke.getKeyStroke('U', shortcutKey));
        underlineItem.addActionListener(e -> {
            TextEditorTab tab = tabbedPane.getActiveEditorTab();
            if (tab != null) {
                styleService.toggleUnderline(tab.getTextPane());
            }
        });
        formatMenu.add(underlineItem);

        formatMenu.addSeparator();

        JMenuItem colorItem = new JMenuItem("Culoare text...");
        colorItem.addActionListener(e -> {
            TextEditorTab tab = tabbedPane.getActiveEditorTab();
            if (tab != null) {
                Color c = JColorChooser.showDialog(this, "Alege culoarea textului", Color.BLACK);
                if (c != null) {
                    styleService.setForegroundColor(tab.getTextPane(), c);
                }
            }
        });
        formatMenu.add(colorItem);

        JMenuItem highlightItem = new JMenuItem("Culoare evidențiere...");
        highlightItem.addActionListener(e -> {
            TextEditorTab tab = tabbedPane.getActiveEditorTab();
            if (tab != null) {
                Color c = JColorChooser.showDialog(this, "Alege culoarea de evidențiere", Color.YELLOW);
                if (c != null) {
                    styleService.setBackgroundColor(tab.getTextPane(), c);
                }
            }
        });
        formatMenu.add(highlightItem);

        formatMenu.addSeparator();

        JMenuItem clearFormatItem = new JMenuItem("Elimină formatarea pe selecție");
        clearFormatItem.addActionListener(e -> {
            TextEditorTab tab = tabbedPane.getActiveEditorTab();
            if (tab != null) {
                styleService.clearFormatting(tab.getTextPane());
            }
        });
        formatMenu.add(clearFormatItem);

        menuBar.add(formatMenu);

        // 4. Meniu CĂUTARE
        JMenu searchMenu = new JMenu("Căutare");
        searchMenu.setMnemonic('C');

        JMenuItem findReplaceItem = new JMenuItem("Caută și Înlocuiește...");
        findReplaceItem.setAccelerator(KeyStroke.getKeyStroke('F', shortcutKey));
        findReplaceItem.addActionListener(e -> showFindReplaceDialog());
        searchMenu.add(findReplaceItem);

        JMenuItem clearHighItem = new JMenuItem("Curăță evidențierile de căutare");
        clearHighItem.addActionListener(e -> {
            TextEditorTab tab = tabbedPane.getActiveEditorTab();
            if (tab != null) {
                searchService.clearHighlights(tab.getTextPane());
                statusBar.setMessage("Evidențieri curățate.");
            }
        });
        searchMenu.add(clearHighItem);

        menuBar.add(searchMenu);

        // 5. Meniu AJUTOR
        JMenu helpMenu = new JMenu("Ajutor");
        helpMenu.setMnemonic('A');

        JMenuItem aboutItem = new JMenuItem("Despre Redactor Text");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(helpMenu);

        return menuBar;
    }

    private void onActiveTabChanged(TextEditorTab activeTab) {
        formatToolBar.setActiveTab(activeTab);
        statusBar.setActiveTab(activeTab);

        if (activeTab != null) {
            String title = activeTab.getTitle();
            setTitle(title + " - Redactor Text (POO)");
            statusBar.setMessage("Fila '" + title + "' este activă.");
        } else {
            setTitle("Redactor Text");
        }
    }

    private void openFile() {
        File file = fileService.chooseFileToOpen(this);
        if (file == null) return;

        // Dacă fila activă este goală, fără fișier și nemodificată, deschidem în ea
        TextEditorTab active = tabbedPane.getActiveEditorTab();
        if (active != null && active.getCurrentFile() == null && !active.isModified()
                && active.getStyledDocument().getLength() == 0) {
            boolean ok = fileService.loadFileIntoTab(file, active, this);
            if (ok) {
                onActiveTabChanged(active);
                statusBar.setMessage("Fișier încărcat: " + file.getName());
            }
        } else {
            // Altfel, creăm o filă nouă
            TextEditorTab newTab = tabbedPane.addNewTab(file.getName(), file);
            boolean ok = fileService.loadFileIntoTab(file, newTab, this);
            if (ok) {
                onActiveTabChanged(newTab);
                statusBar.setMessage("Fișier încărcat în filă nouă: " + file.getName());
            }
        }
    }

    private void saveCurrentFile() {
        TextEditorTab active = tabbedPane.getActiveEditorTab();
        if (active != null) {
            boolean ok = fileService.save(active, this);
            if (ok) {
                statusBar.setMessage("Salvat cu succes: " + active.getTitle());
                onActiveTabChanged(active);
            }
        }
    }

    private void saveAsCurrentFile() {
        TextEditorTab active = tabbedPane.getActiveEditorTab();
        if (active != null) {
            boolean ok = fileService.saveAs(active, this);
            if (ok) {
                statusBar.setMessage("Salvat ca: " + active.getTitle());
                onActiveTabChanged(active);
            }
        }
    }

    private void showFindReplaceDialog() {
        TextEditorTab active = tabbedPane.getActiveEditorTab();
        findReplaceDialog.showForTab(active);
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(
                this,
                "<html><h2>Redactor Text Bogat (Rich Text)</h2>" +
                "<p><b>Paradigma Orientată pe Obiecte - Lucrarea de laborator nr. 2</b></p>" +
                "<p>Funcționalități implementate conform cerințelor:</p>" +
                "<ul>" +
                "<li><b>a.</b> Redactare text, deschidere și salvare fișiere (RTF și Text)</li>" +
                "<li><b>b.</b> Căutare subșiruri cu direcție (Înainte / Înapoi), case-sensitive și wrap-around</li>" +
                "<li><b>c.</b> Înlocuire subșiruri cu posibilitatea de a selecta / evidenția toate aparițiile</li>" +
                "<li><b>d.</b> Modificare font (familie, mărime, culoare, bold, italic, underline) cu <b>stiluri suprapuse</b></li>" +
                "<li><b>e.</b> Lucrul cu mai multe fișiere în file separate (tab-uri) cu indicator de modificare</li>" +
                "<li><b>f.</b> Salvarea fișierelor cu păstrarea stilurilor în format RTF</li>" +
                "</ul></html>",
                "Despre Redactor Text",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void handleExit() {
        if (tabbedPane.closeAllTabs()) {
            dispose();
            System.exit(0);
        }
    }
}
