import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestionează colecția de file (tabs) din redactorul de text.
 * Implementează cerința (e) privind lucrul cu mai multe fișiere în file noi.
 */
public class TabbedEditorPane extends JTabbedPane {

    public interface ActiveTabChangeListener {
        void onActiveTabChanged(TextEditorTab activeTab);
    }

    private final FileService fileService;
    private final List<ActiveTabChangeListener> activeTabChangeListeners;
    private int untitledCounter = 1;

    public TabbedEditorPane(FileService fileService) {
        super(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        this.fileService = fileService;
        this.activeTabChangeListeners = new ArrayList<>();

        addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                fireActiveTabChanged();
            }
        });
    }

    public void addActiveTabChangeListener(ActiveTabChangeListener listener) {
        if (listener != null && !activeTabChangeListeners.contains(listener)) {
            activeTabChangeListeners.add(listener);
        }
    }

    private void fireActiveTabChanged() {
        TextEditorTab activeTab = getActiveEditorTab();
        for (ActiveTabChangeListener l : activeTabChangeListeners) {
            l.onActiveTabChanged(activeTab);
        }
    }

    /**
     * Creează și adaugă o filă nouă fără fișier asociat (ex: "Document 1").
     */
    public TextEditorTab addNewTab() {
        String title = "Document " + (untitledCounter++);
        return addNewTab(title, null);
    }

    /**
     * Adaugă o filă nouă cu titlul și fișierul specificat.
     */
    public TextEditorTab addNewTab(String title, File file) {
        TextEditorTab tab = new TextEditorTab(title, file);
        addTab(title, tab);

        int index = indexOfComponent(tab);
        TabHeaderComponent header = new TabHeaderComponent(tab, this, this::closeTab);
        setTabComponentAt(index, header);
        setSelectedComponent(tab);

        // Dacă titlul se schimbă, actualizăm și headerul
        tab.addModificationListener((t, isModified) -> {
            header.updateTitle();
        });

        fireActiveTabChanged();
        return tab;
    }

    /**
     * Închide fila specificată, cerând confirmare dacă există modificări nesalvate.
     * @return true dacă fila a fost închisă, false dacă acțiunea a fost anulată
     */
    public boolean closeTab(TextEditorTab tab) {
        if (tab == null) return false;

        int index = indexOfComponent(tab);
        if (index == -1) return false;

        if (!fileService.promptSaveIfModified(tab, this)) {
            return false; // Utilizatorul a anulat
        }

        removeTabAt(index);

        // Dacă nu mai există nicio filă deschisă, creăm automat una nouă goală
        if (getTabCount() == 0) {
            addNewTab();
        }

        fireActiveTabChanged();
        return true;
    }

    /**
     * Închide fila activă curentă.
     */
    public boolean closeCurrentTab() {
        return closeTab(getActiveEditorTab());
    }

    /**
     * Închide toate filele, verificând salvarea pentru fiecare.
     * @return true dacă toate au putut fi închise, false dacă utilizatorul a anulat vreuna
     */
    public boolean closeAllTabs() {
        while (getTabCount() > 0) {
            TextEditorTab tab = getEditorTabAt(0);
            if (!fileService.promptSaveIfModified(tab, this)) {
                return false;
            }
            removeTabAt(0);
        }
        return true;
    }

    /**
     * Returnează instanța TextEditorTab aflată în prim-plan.
     */
    public TextEditorTab getActiveEditorTab() {
        Component c = getSelectedComponent();
        if (c instanceof TextEditorTab) {
            return (TextEditorTab) c;
        }
        return null;
    }

    /**
     * Returnează tab-ul de la indexul specificat.
     */
    public TextEditorTab getEditorTabAt(int index) {
        if (index >= 0 && index < getTabCount()) {
            Component c = getComponentAt(index);
            if (c instanceof TextEditorTab) {
                return (TextEditorTab) c;
            }
        }
        return null;
    }
}
