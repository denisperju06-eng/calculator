import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Serviciu ce gestionează istoricul global de navigare al utilizatorului.
 * Asigură stocarea în memorie și persistența într-un fișier text (history.txt).
 */
public class HistoryManager {

    private final List<HistoryItem> historyList;
    private final File storageFile;
    private static final int MAX_HISTORY_ITEMS = 1000;

    public HistoryManager() {
        this(new File("history.txt"));
    }

    public HistoryManager(File storageFile) {
        this.storageFile = storageFile;
        this.historyList = new ArrayList<>();
        loadFromFile();
    }

    /**
     * Înregistrează o pagină vizitată în istoric.
     * Cele mai recente intrări sunt plasate la începutul listei.
     */
    public synchronized void addEntry(String url, String title) {
        if (url == null || url.trim().isEmpty() || url.startsWith("about:")) {
            return;
        }

        // Eliminăm duplicatele consecutive imediate
        if (!historyList.isEmpty()) {
            HistoryItem last = historyList.get(0);
            if (last.getUrl().equalsIgnoreCase(url.trim())) {
                last.setTitle(title);
                saveToFile();
                return;
            }
        }

        historyList.add(0, new HistoryItem(url, title));

        // Limităm dimensiunea maximă a istoricului
        if (historyList.size() > MAX_HISTORY_ITEMS) {
            historyList.remove(historyList.size() - 1);
        }

        saveToFile();
    }

    /**
     * Returnează o copie needitabilă a istoricului.
     */
    public synchronized List<HistoryItem> getHistory() {
        return Collections.unmodifiableList(new ArrayList<>(historyList));
    }

    /**
     * Caută intrări în istoric care conțin cuvântul cheie în URL sau titlu.
     */
    public synchronized List<HistoryItem> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getHistory();
        }
        String lower = query.trim().toLowerCase();
        List<HistoryItem> results = new ArrayList<>();
        for (HistoryItem item : historyList) {
            if (item.getTitle().toLowerCase().contains(lower) || item.getUrl().toLowerCase().contains(lower)) {
                results.add(item);
            }
        }
        return results;
    }

    /**
     * Șterge o intrare specifică din istoric.
     */
    public synchronized boolean removeEntry(HistoryItem item) {
        boolean removed = historyList.remove(item);
        if (removed) {
            saveToFile();
        }
        return removed;
    }

    /**
     * Curăță întregul istoric de navigare.
     */
    public synchronized void clearHistory() {
        historyList.clear();
        saveToFile();
    }

    /**
     * Salvează istoricul în fișierul specificat.
     */
    public synchronized void saveToFile() {
        if (storageFile == null) return;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(storageFile, StandardCharsets.UTF_8))) {
            for (HistoryItem item : historyList) {
                writer.write(item.toFileLine());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Eroare la salvarea istoricului în fișier: " + e.getMessage());
        }
    }

    /**
     * Încarcă istoricul din fișierul specificat.
     */
    public synchronized void loadFromFile() {
        if (storageFile == null || !storageFile.exists()) {
            return;
        }

        historyList.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(storageFile, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                HistoryItem item = HistoryItem.fromFileLine(line);
                if (item != null) {
                    historyList.add(item);
                }
            }
        } catch (IOException e) {
            System.err.println("Eroare la încărcarea istoricului din fișier: " + e.getMessage());
        }
    }

    public File getStorageFile() {
        return storageFile;
    }

    public int size() {
        return historyList.size();
    }
}
