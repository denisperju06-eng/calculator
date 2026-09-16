import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.swing.text.rtf.RTFEditorKit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Reprezintă o filă (tab) individuală a redactorului de text.
 * Încapsulează un JTextPane, StyledDocument cu RTFEditorKit, UndoManager,
 * starea de modificare (dirty flag) și referința către fișierul asociat de pe disc.
 */
public class TextEditorTab extends JPanel {

    public interface ModificationListener {
        void onModificationChanged(TextEditorTab tab, boolean isModified);
    }

    private final JTextPane textPane;
    private final RTFEditorKit rtfEditorKit;
    private final UndoManager undoManager;
    private final JScrollPane scrollPane;
    private final LineNumberView lineNumberView;
    private final List<ModificationListener> modificationListeners;

    private File currentFile;
    private String title;
    private boolean isModified;
    private boolean ignoreModifications;

    public TextEditorTab(String title, File file) {
        super(new BorderLayout());
        this.title = title != null ? title : "Fără titlu";
        this.currentFile = file;
        this.isModified = false;
        this.ignoreModifications = false;
        this.modificationListeners = new ArrayList<>();

        // Inițializare editor cu suport RTF
        this.textPane = new JTextPane();
        this.rtfEditorKit = new RTFEditorKit();
        this.textPane.setEditorKit(rtfEditorKit);
        this.textPane.setFont(new Font("Arial", Font.PLAIN, 14));
        this.textPane.setMargin(new Insets(15, 20, 15, 20));

        // Undo / Redo Manager
        this.undoManager = new UndoManager();
        this.undoManager.setLimit(500);

        attachListeners();

        // Scroll pane cu numerotarea liniilor
        this.scrollPane = new JScrollPane(textPane);
        this.lineNumberView = new LineNumberView(textPane);
        this.scrollPane.setRowHeaderView(lineNumberView);
        this.scrollPane.setBorder(BorderFactory.createEmptyBorder());

        add(scrollPane, BorderLayout.CENTER);
    }

    private void attachListeners() {
        StyledDocument doc = textPane.getStyledDocument();

        // Urmărire Undo / Redo
        doc.addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                if (!ignoreModifications) {
                    undoManager.addEdit(e.getEdit());
                }
            }
        });

        // Urmărire modificări (dirty state)
        doc.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                markModified();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                markModified();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                markModified();
            }
        });
    }

    private void markModified() {
        if (!ignoreModifications && !isModified) {
            setModified(true);
        }
    }

    public void addModificationListener(ModificationListener listener) {
        if (listener != null && !modificationListeners.contains(listener)) {
            modificationListeners.add(listener);
        }
    }

    public void removeModificationListener(ModificationListener listener) {
        modificationListeners.remove(listener);
    }

    public boolean isModified() {
        return isModified;
    }

    public void setModified(boolean modified) {
        if (this.isModified != modified) {
            this.isModified = modified;
            for (ModificationListener l : modificationListeners) {
                l.onModificationChanged(this, modified);
            }
        }
    }

    public boolean isIgnoreModifications() {
        return ignoreModifications;
    }

    public void setIgnoreModifications(boolean ignoreModifications) {
        this.ignoreModifications = ignoreModifications;
    }

    public String getText() {
        try {
            StyledDocument doc = textPane.getStyledDocument();
            return doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            return "";
        }
    }

    public JTextPane getTextPane() {
        return textPane;
    }

    public StyledDocument getStyledDocument() {
        return textPane.getStyledDocument();
    }

    public RTFEditorKit getRtfEditorKit() {
        return rtfEditorKit;
    }

    public File getCurrentFile() {
        return currentFile;
    }

    public void setCurrentFile(File currentFile) {
        this.currentFile = currentFile;
        if (currentFile != null) {
            this.title = currentFile.getName();
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void undo() {
        try {
            if (undoManager.canUndo()) {
                undoManager.undo();
            }
        } catch (CannotUndoException ignored) {
        }
    }

    public void redo() {
        try {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }
        } catch (CannotRedoException ignored) {
        }
    }

    public boolean canUndo() {
        return undoManager.canUndo();
    }

    public boolean canRedo() {
        return undoManager.canRedo();
    }

    public void resetUndo() {
        undoManager.discardAllEdits();
    }
}
