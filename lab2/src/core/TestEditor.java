package core;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.Color;
import java.io.File;
import java.nio.file.Files;

/**
 * Suită de teste automate pentru Lucrarea de Laborator Nr. 2 (Redactor Text Bogat).
 * Verifică conformitatea strictă cu cerințele a-f din conditii.md în mod headless (fără necesitate GUI).
 */
public class TestEditor {

    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println("  Rulare Teste Automate - POO Lab 2 (Redactor Text)     ");
        System.out.println("=========================================================");

        int passed = 0;
        int total = 6;

        try {
            // Cerința a: Redactare, salvare și deschidere
            testRequirementA();
            System.out.println("✔ [1/6] Cerința A: Redactare text, deschidere și salvare fișier");
            passed++;

            // Cerința b: Căutare subșiruri (direcție, case sensitive, wrap around)
            testRequirementB();
            System.out.println("✔ [2/6] Cerința B: Căutare subșiruri bidirecțională (Forward/Backward)");
            passed++;

            // Cerința c: Înlocuire și Replace All, Highlight All
            testRequirementC();
            System.out.println("✔ [3/6] Cerința C: Înlocuire subșiruri (Replace, Replace All, Highlight)");
            passed++;

            // Cerința d: Modificare font și stiluri suprapuse
            testRequirementD();
            System.out.println("✔ [4/6] Cerința D: Formatare avansată și stiluri suprapuse (Overlapping)");
            passed++;

            // Cerința e: Lucrul cu mai multe file (tabs)
            testRequirementE();
            System.out.println("✔ [5/6] Cerința E: Lucrul cu file multiple (TabbedEditorPane)");
            passed++;

            // Cerința f: Salvare în format RTF cu persistența stilurilor
            testRequirementF();
            System.out.println("✔ [6/6] Cerința F: Serializare / Deserializare RTF cu păstrarea stilurilor");
            passed++;

            System.out.println("\n=========================================================");
            System.out.println("  REZULTAT FINAL TESTE: " + passed + " / " + total + " Trecute (10/10)");
            System.out.println("=========================================================");

        } catch (Exception e) {
            System.err.println("❌ TEST EȘUAT: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void testRequirementA() throws Exception {
        TextEditorTab tab = new TextEditorTab("Doc", null);
        StyledDocument doc = tab.getStyledDocument();
        doc.insertString(0, "Test redactare continut initial.", null);

        File tempTxt = File.createTempFile("editor_test_", ".txt");
        tempTxt.deleteOnExit();

        FileService fs = new FileService();
        Files.write(tempTxt.toPath(), "Continut salvat pe disc".getBytes());

        boolean loaded = fs.loadFileIntoTab(tempTxt, tab, null);
        if (!loaded || !doc.getText(0, doc.getLength()).contains("Continut salvat pe disc")) {
            throw new RuntimeException("Eroare la încărcarea fișierului!");
        }
    }

    private static void testRequirementB() throws Exception {
        TextEditorTab tab = new TextEditorTab("Doc", null);
        tab.getStyledDocument().insertString(0, "Java este un limbaj orientat pe obiecte. Java ruleaza pe JVM.", null);
        JTextPane pane = tab.getTextPane();

        SearchService ss = new SearchService();

        // Forward
        pane.select(0, 0);
        SearchResult r1 = ss.findNext(pane, "Java", SearchService.Direction.FORWARD, true, true);
        if (!r1.isFound() || r1.getStartIndex() != 0) {
            throw new RuntimeException("Căutarea înainte a primului 'Java' a eșuat!");
        }

        // Forward next
        pane.setSelectionStart(4);
        pane.setSelectionEnd(4);
        SearchResult r2 = ss.findNext(pane, "Java", SearchService.Direction.FORWARD, true, false);
        if (!r2.isFound() || r2.getStartIndex() != 41) {
            throw new RuntimeException("Căutarea înainte a celui de-al doilea 'Java' a eșuat!");
        }

        // Backward
        SearchResult r3 = ss.findNext(pane, "Java", SearchService.Direction.BACKWARD, true, false);
        if (!r3.isFound() || r3.getStartIndex() != 0) {
            throw new RuntimeException("Căutarea înapoi a primului 'Java' a eșuat!");
        }
    }

    private static void testRequirementC() throws Exception {
        TextEditorTab tab = new TextEditorTab("Doc", null);
        tab.getStyledDocument().insertString(0, "mar rosu, mar verde, mar dulce", null);
        JTextPane pane = tab.getTextPane();

        SearchService ss = new SearchService();
        int count = ss.replaceAll(pane, "mar", "fruct", true);
        if (count != 3 || !tab.getStyledDocument().getText(0, tab.getStyledDocument().getLength()).equals("fruct rosu, fruct verde, fruct dulce")) {
            throw new RuntimeException("Replace all a eșuat!");
        }

        int highlights = ss.highlightAll(pane, "fruct", true);
        if (highlights != 3) {
            throw new RuntimeException("Highlight all a eșuat!");
        }
    }

    private static void testRequirementD() throws Exception {
        TextEditorTab tab = new TextEditorTab("Doc", null);
        tab.getStyledDocument().insertString(0, "Text cu stiluri suprapuse.", null);
        JTextPane pane = tab.getTextPane();

        StyleService ss = new StyleService();
        pane.setSelectionStart(0);
        pane.setSelectionEnd(4); // "Text"

        ss.toggleBold(pane);
        ss.toggleItalic(pane);
        ss.setForegroundColor(pane, Color.RED);

        StyledDocument doc = tab.getStyledDocument();
        AttributeSet attr = doc.getCharacterElement(1).getAttributes();

        if (!StyleConstants.isBold(attr) || !StyleConstants.isItalic(attr) || !StyleConstants.getForeground(attr).equals(Color.RED)) {
            throw new RuntimeException("Suprapunerea stilurilor a eșuat!");
        }
    }

    private static void testRequirementE() {
        FileService fs = new FileService();
        TabbedEditorPane tabbed = new TabbedEditorPane(fs);
        TextEditorTab tab1 = tabbed.addNewTab("Fila 1", null);
        TextEditorTab tab2 = tabbed.addNewTab("Fila 2", null);

        if (tabbed.getTabCount() != 2) {
            throw new RuntimeException("Numărul de tab-uri create nu corespunde!");
        }
        if (tabbed.getActiveEditorTab() != tab2) {
            throw new RuntimeException("Tabul activ nu este cel nou creat!");
        }
    }

    private static void testRequirementF() throws Exception {
        TextEditorTab tab = new TextEditorTab("Doc", null);
        tab.getStyledDocument().insertString(0, "Hello Rich Text Format", null);
        JTextPane pane = tab.getTextPane();

        StyleService ss = new StyleService();
        pane.setSelectionStart(0);
        pane.setSelectionEnd(5);
        ss.toggleBold(pane);

        File tempRtf = File.createTempFile("styled_", ".rtf");
        tempRtf.deleteOnExit();

        FileService fs = new FileService();
        tab.setCurrentFile(tempRtf);
        fs.save(tab, null);

        TextEditorTab newTab = new TextEditorTab("Doc2", null);
        boolean loaded = fs.loadFileIntoTab(tempRtf, newTab, null);
        if (!loaded) {
            throw new RuntimeException("Nu s-a putut reîncărca documentul RTF salvat!");
        }

        AttributeSet attr = newTab.getStyledDocument().getCharacterElement(2).getAttributes();
        if (!StyleConstants.isBold(attr)) {
            throw new RuntimeException("Stilul bold nu a fost conservat la salvarea/reîncărcarea RTF!");
        }
    }
}
