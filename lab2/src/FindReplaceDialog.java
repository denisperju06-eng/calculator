import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Fereastră de dialog (nemodală) pentru Căutare și Înlocuire.
 * Acoperă cerințele (b) căutarea cu direcție și (c) înlocuirea cu selectarea tuturor aparițiilor.
 */
public class FindReplaceDialog extends JDialog {

    private final SearchService searchService;
    private final TabbedEditorPane tabbedPane;

    private JTextField findField;
    private JTextField replaceField;
    private JRadioButton forwardRadio;
    private JRadioButton backwardRadio;
    private JCheckBox matchCaseCheck;
    private JCheckBox wrapAroundCheck;
    private JLabel statusLabel;

    public FindReplaceDialog(Frame owner, SearchService searchService, TabbedEditorPane tabbedPane) {
        super(owner, "Căutare și Înlocuire", false);
        this.searchService = searchService;
        this.tabbedPane = tabbedPane;

        initUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(12, 14, 12, 14));

        // Panoul central cu câmpurile de text și opțiuni
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Rând 0: Caută
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Caută:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        findField = new JTextField(22);
        formPanel.add(findField, gbc);

        // Rând 1: Înlocuiește cu
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Înlocuiește cu:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        replaceField = new JTextField(22);
        formPanel.add(replaceField, gbc);

        // Rând 2: Direcție de căutare
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Direcție:"), gbc);

        JPanel directionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        forwardRadio = new JRadioButton("Înainte (În jos)", true);
        backwardRadio = new JRadioButton("Înapoi (În sus)", false);
        ButtonGroup dirGroup = new ButtonGroup();
        dirGroup.add(forwardRadio);
        dirGroup.add(backwardRadio);
        directionPanel.add(forwardRadio);
        directionPanel.add(backwardRadio);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(directionPanel, gbc);

        // Rând 3: Opțiuni
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Opțiuni:"), gbc);

        JPanel optionsPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        matchCaseCheck = new JCheckBox("Potrivește litere mari/mici (Case sensitive)");
        wrapAroundCheck = new JCheckBox("Reia de la capăt (Wrap around)", true);
        optionsPanel.add(matchCaseCheck);
        optionsPanel.add(wrapAroundCheck);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(optionsPanel, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Panoul cu butoane de acțiune în partea dreaptă
        JPanel buttonsPanel = new JPanel(new GridLayout(6, 1, 4, 6));

        JButton findNextBtn = new JButton("Caută următorul");
        JButton replaceBtn = new JButton("Înlocuiește");
        JButton replaceAllBtn = new JButton("Înlocuiește tot");
        JButton highlightAllBtn = new JButton("Evidențiază toate");
        JButton clearHighlightsBtn = new JButton("Curăță marcajele");
        JButton closeBtn = new JButton("Închide");

        buttonsPanel.add(findNextBtn);
        buttonsPanel.add(replaceBtn);
        buttonsPanel.add(replaceAllBtn);
        buttonsPanel.add(highlightAllBtn);
        buttonsPanel.add(clearHighlightsBtn);
        buttonsPanel.add(closeBtn);

        mainPanel.add(buttonsPanel, BorderLayout.EAST);

        // Panoul de stare la baza dialogului
        statusLabel = new JLabel("Introduceți un termen pentru căutare.");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(90, 90, 90));
        statusLabel.setBorder(new EmptyBorder(6, 4, 0, 4));
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // Legare evenimente
        findNextBtn.addActionListener(e -> performFindNext());
        replaceBtn.addActionListener(e -> performReplace());
        replaceAllBtn.addActionListener(e -> performReplaceAll());
        highlightAllBtn.addActionListener(e -> performHighlightAll());
        clearHighlightsBtn.addActionListener(e -> performClearHighlights());
        closeBtn.addActionListener(e -> setVisible(false));

        // Tasta Enter în câmpul de căutare rulează Caută următorul
        findField.addActionListener(e -> performFindNext());
        // Tasta Enter în câmpul de înlocuire rulează Înlocuiește
        replaceField.addActionListener(e -> performReplace());

        // Tasta ESC închide dialogul
        getRootPane().registerKeyboardAction(
                e -> setVisible(false),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private TextEditorTab getActiveTab() {
        return tabbedPane.getActiveEditorTab();
    }

    private void performFindNext() {
        TextEditorTab tab = getActiveTab();
        if (tab == null) {
            statusLabel.setText("Nu există niciun document deschis.");
            return;
        }

        String query = findField.getText();
        if (query.isEmpty()) {
            statusLabel.setText("Introduceți textul căutat.");
            return;
        }

        SearchService.Direction dir = forwardRadio.isSelected() ?
                SearchService.Direction.FORWARD : SearchService.Direction.BACKWARD;
        boolean matchCase = matchCaseCheck.isSelected();
        boolean wrapAround = wrapAroundCheck.isSelected();

        SearchResult res = searchService.findNext(tab.getTextPane(), query, dir, matchCase, wrapAround);
        statusLabel.setText(res.getMessage());
    }

    private void performReplace() {
        TextEditorTab tab = getActiveTab();
        if (tab == null) {
            statusLabel.setText("Nu există niciun document deschis.");
            return;
        }

        String query = findField.getText();
        String replacement = replaceField.getText();
        if (query.isEmpty()) {
            statusLabel.setText("Introduceți textul căutat.");
            return;
        }

        SearchService.Direction dir = forwardRadio.isSelected() ?
                SearchService.Direction.FORWARD : SearchService.Direction.BACKWARD;
        boolean matchCase = matchCaseCheck.isSelected();
        boolean wrapAround = wrapAroundCheck.isSelected();

        SearchResult res = searchService.replace(tab.getTextPane(), query, replacement, dir, matchCase, wrapAround);
        statusLabel.setText(res.getMessage());
    }

    private void performReplaceAll() {
        TextEditorTab tab = getActiveTab();
        if (tab == null) {
            statusLabel.setText("Nu există niciun document deschis.");
            return;
        }

        String query = findField.getText();
        String replacement = replaceField.getText();
        if (query.isEmpty()) {
            statusLabel.setText("Introduceți textul căutat.");
            return;
        }

        boolean matchCase = matchCaseCheck.isSelected();
        int count = searchService.replaceAll(tab.getTextPane(), query, replacement, matchCase);
        if (count > 0) {
            statusLabel.setText("Au fost înlocuite " + count + " apariții.");
        } else {
            statusLabel.setText("Nu a fost găsită nicio apariție de înlocuit.");
        }
    }

    private void performHighlightAll() {
        TextEditorTab tab = getActiveTab();
        if (tab == null) {
            statusLabel.setText("Nu există niciun document deschis.");
            return;
        }

        String query = findField.getText();
        if (query.isEmpty()) {
            statusLabel.setText("Introduceți textul căutat.");
            return;
        }

        boolean matchCase = matchCaseCheck.isSelected();
        int count = searchService.highlightAll(tab.getTextPane(), query, matchCase);
        if (count > 0) {
            statusLabel.setText("Au fost găsite și evidențiate " + count + " apariții.");
        } else {
            statusLabel.setText("Nu a fost găsită nicio apariție.");
        }
    }

    private void performClearHighlights() {
        TextEditorTab tab = getActiveTab();
        if (tab != null) {
            searchService.clearHighlights(tab.getTextPane());
            statusLabel.setText("Evidențierile au fost curățate.");
        }
    }

    public void showForTab(TextEditorTab tab) {
        if (tab != null) {
            // Dacă există selecție în editor, o precompletăm în câmpul de căutare
            String selected = tab.getTextPane().getSelectedText();
            if (selected != null && !selected.isEmpty() && !selected.contains("\n")) {
                findField.setText(selected);
                findField.selectAll();
            }
        }
        setVisible(true);
        findField.requestFocusInWindow();
    }
}
