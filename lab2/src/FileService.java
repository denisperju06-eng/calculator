import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.awt.Component;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Serviciu responsabil cu operațiile I/O pe fișiere (deschidere, salvare, salvare ca).
 * Asigură salvarea stilurilor utilizând formatul RTF (Rich Text Format) conform cerinței (f).
 */
public class FileService {

    private final JFileChooser fileChooser;

    public FileService() {
        this.fileChooser = new JFileChooser();
        setupFileChooser();
    }

    private void setupFileChooser() {
        fileChooser.setAcceptAllFileFilterUsed(true);
        FileNameExtensionFilter rtfFilter = new FileNameExtensionFilter("Document Rich Text (*.rtf)", "rtf");
        FileNameExtensionFilter txtFilter = new FileNameExtensionFilter("Fișier Text Simplu (*.txt)", "txt");

        fileChooser.addChoosableFileFilter(txtFilter);
        fileChooser.addChoosableFileFilter(rtfFilter);
        fileChooser.setFileFilter(rtfFilter); // RTF implicit pentru păstrarea stilurilor
    }

    /**
     * Salvează conținutul tab-ului. Dacă este un fișier nou, declanșează 'Save As'.
     */
    public boolean save(TextEditorTab tab, Component parent) {
        if (tab == null) return false;

        if (tab.getCurrentFile() == null) {
            return saveAs(tab, parent);
        } else {
            return writeTabToFile(tab, tab.getCurrentFile(), parent);
        }
    }

    /**
     * Deschide dialogul de salvare și salvează conținutul tab-ului în fișierul ales.
     */
    public boolean saveAs(TextEditorTab tab, Component parent) {
        if (tab == null) return false;

        if (tab.getCurrentFile() != null) {
            fileChooser.setSelectedFile(tab.getCurrentFile());
        } else {
            fileChooser.setSelectedFile(new File(tab.getTitle() + ".rtf"));
        }

        int result = fileChooser.showSaveDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) {
            return false;
        }

        File selectedFile = fileChooser.getSelectedFile();
        if (selectedFile == null) return false;

        // Adăugăm extensia potrivită dacă utilizatorul nu a tastat-o
        String name = selectedFile.getName();
        if (!name.contains(".")) {
            if (fileChooser.getFileFilter().getDescription().contains("rtf")) {
                selectedFile = new File(selectedFile.getParentFile(), name + ".rtf");
            } else if (fileChooser.getFileFilter().getDescription().contains("txt")) {
                selectedFile = new File(selectedFile.getParentFile(), name + ".txt");
            } else {
                selectedFile = new File(selectedFile.getParentFile(), name + ".rtf");
            }
        }

        // Confirmare suprascriere
        if (selectedFile.exists() && (tab.getCurrentFile() == null || !selectedFile.equals(tab.getCurrentFile()))) {
            int overwrite = JOptionPane.showConfirmDialog(
                    parent,
                    "Fișierul '" + selectedFile.getName() + "' există deja.\nDoriți să îl suprascrieți?",
                    "Confirmare suprascriere",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (overwrite != JOptionPane.YES_OPTION) {
                return false;
            }
        }

        return writeTabToFile(tab, selectedFile, parent);
    }

    private boolean writeTabToFile(TextEditorTab tab, File file, Component parent) {
        StyledDocument doc = tab.getStyledDocument();
        boolean isRtf = file.getName().toLowerCase().endsWith(".rtf");

        try {
            if (isRtf) {
                try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                    tab.getRtfEditorKit().write(out, doc, 0, doc.getLength());
                }
            } else {
                // Text simplu UTF-8
                try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)), StandardCharsets.UTF_8)) {
                    String plainText = doc.getText(0, doc.getLength());
                    writer.write(plainText);
                }
            }

            tab.setCurrentFile(file);
            tab.setTitle(file.getName());
            tab.setModified(false);
            return true;

        } catch (IOException | BadLocationException e) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Eroare la salvarea fișierului:\n" + e.getMessage(),
                    "Eroare Salvare",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }

    /**
     * Deschide selectorul de fișiere pentru încărcare.
     */
    public File chooseFileToOpen(Component parent) {
        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    /**
     * Încarcă conținutul unui fișier în tab-ul specificat.
     */
    public boolean loadFileIntoTab(File file, TextEditorTab tab, Component parent) {
        if (file == null || !file.exists() || tab == null) {
            return false;
        }

        tab.setIgnoreModifications(true);
        StyledDocument doc = tab.getStyledDocument();

        try {
            // Golim conținutul anterior
            doc.remove(0, doc.getLength());

            boolean isRtf = isRtfFile(file);
            if (isRtf) {
                try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
                    tab.getRtfEditorKit().read(in, doc, 0);
                }
            } else {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    doc.insertString(0, sb.toString(), null);
                }
            }

            tab.setCurrentFile(file);
            tab.setTitle(file.getName());
            tab.resetUndo();
            tab.setModified(false);
            tab.getTextPane().setCaretPosition(0);
            return true;

        } catch (IOException | BadLocationException e) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Eroare la deschiderea fișierului:\n" + e.getMessage(),
                    "Eroare Deschidere",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        } finally {
            tab.setIgnoreModifications(false);
        }
    }

    private boolean isRtfFile(File file) {
        if (file.getName().toLowerCase().endsWith(".rtf")) {
            return true;
        }
        // Verificare magic bytes {\rtf
        try (InputStream in = new FileInputStream(file)) {
            byte[] header = new byte[5];
            int read = in.read(header);
            if (read >= 5 && new String(header).startsWith("{\\rtf")) {
                return true;
            }
        } catch (IOException ignored) {
        }
        return false;
    }

    /**
     * Verifică dacă tab-ul conține modificări și solicită utilizatorului salvarea.
     * @return true dacă se poate continua (salvat sau respins), false dacă utilizatorul a anulat
     */
    public boolean promptSaveIfModified(TextEditorTab tab, Component parent) {
        if (tab == null || !tab.isModified()) {
            return true;
        }

        int choice = JOptionPane.showConfirmDialog(
                parent,
                "Fișierul '" + tab.getTitle() + "' conține modificări nesalvate.\nDoriți să le salvați?",
                "Modificări nesalvate",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            return save(tab, parent);
        } else if (choice == JOptionPane.NO_OPTION) {
            return true;
        } else {
            // Cancel sau Close Dialog
            return false;
        }
    }
}
