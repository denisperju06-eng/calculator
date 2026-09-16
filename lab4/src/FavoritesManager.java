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
 * Serviciu responsabil cu gestionarea și persistența în fișier a paginilor Favorite (Bookmarks).
 * Salvează datele în mod automat într-un fișier text configurabil (implicit favorites.txt).
 * Notifică componentele abonate conform șablonului Observer.
 */
public class FavoritesManager {

    private final List<FavoriteItem> favorites;
    private final List<FavoritesListener> listeners;
    private final File storageFile;

    public FavoritesManager() {
        this(new File("favorites.txt"));
    }

    public FavoritesManager(File storageFile) {
        this.storageFile = storageFile;
        this.favorites = new ArrayList<>();
        this.listeners = new ArrayList<>();
        loadFromFile();

        // Dacă fișierul este nou/gol, adăugăm câteva favorite implicite utile
        if (favorites.isEmpty()) {
            initDefaultFavorites();
        }
    }

    private void initDefaultFavorites() {
        favorites.add(new FavoriteItem("DuckDuckGo HTML", "https://html.duckduckgo.com"));
        favorites.add(new FavoriteItem("Wikipedia (Română)", "https://ro.wikipedia.org"));
        favorites.add(new FavoriteItem("Wikipedia (English)", "https://en.wikipedia.org"));
        favorites.add(new FavoriteItem("Java Documentation", "https://docs.oracle.com/en/java/"));
        favorites.add(new FavoriteItem("Example Domain", "https://example.com"));
        saveToFile();
    }

    /**
     * Adaugă o nouă pagină la favorite dacă nu este deja salvată.
     * @return true dacă a fost adăugată, false dacă exista deja
     */
    public synchronized boolean addFavorite(FavoriteItem item) {
        if (item == null || item.getUrl().isEmpty() || item.getUrl().startsWith("about:")) {
            return false;
        }

        // Verificăm dacă există deja acest URL
        for (FavoriteItem fav : favorites) {
            if (fav.getUrl().equalsIgnoreCase(item.getUrl())) {
                fav.setTitle(item.getTitle());
                saveToFile();
                notifyListeners();
                return false;
            }
        }

        favorites.add(item);
        saveToFile();
        notifyListeners();
        return true;
    }

    public synchronized boolean addFavorite(String title, String url) {
        return addFavorite(new FavoriteItem(title, url));
    }

    /**
     * Șterge un element favorit după index.
     */
    public synchronized boolean removeFavoriteAt(int index) {
        if (index >= 0 && index < favorites.size()) {
            favorites.remove(index);
            saveToFile();
            notifyListeners();
            return true;
        }
        return false;
    }

    /**
     * Șterge un favorit pe baza URL-ului.
     */
    public synchronized boolean removeFavoriteByUrl(String url) {
        if (url == null) return false;
        boolean removed = favorites.removeIf(fav -> fav.getUrl().equalsIgnoreCase(url.trim()));
        if (removed) {
            saveToFile();
            notifyListeners();
        }
        return removed;
    }

    /**
     * Verifică dacă un anumit URL este salvat la favorite.
     */
    public synchronized boolean isFavorite(String url) {
        if (url == null) return false;
        for (FavoriteItem fav : favorites) {
            if (fav.getUrl().equalsIgnoreCase(url.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returnează lista needitabilă de favorite.
     */
    public synchronized List<FavoriteItem> getFavorites() {
        return Collections.unmodifiableList(new ArrayList<>(favorites));
    }

    /**
     * Salvează lista de favorite în fișier.
     */
    public synchronized void saveToFile() {
        if (storageFile == null) return;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(storageFile, StandardCharsets.UTF_8))) {
            for (FavoriteItem item : favorites) {
                writer.write(item.toFileLine());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Eroare la salvarea favoritelor în fișier: " + e.getMessage());
        }
    }

    /**
     * Încarcă favoritele din fișier.
     */
    public synchronized void loadFromFile() {
        if (storageFile == null || !storageFile.exists()) {
            return;
        }

        favorites.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(storageFile, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                FavoriteItem item = FavoriteItem.fromFileLine(line);
                if (item != null) {
                    favorites.add(item);
                }
            }
        } catch (IOException e) {
            System.err.println("Eroare la încărcarea favoritelor din fișier: " + e.getMessage());
        }
    }

    // Gestionare Observer / Listeners
    public synchronized void addListener(FavoritesListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public synchronized void removeListener(FavoritesListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (FavoritesListener listener : new ArrayList<>(listeners)) {
            listener.onFavoritesChanged();
        }
    }

    public File getStorageFile() {
        return storageFile;
    }

    public int size() {
        return favorites.size();
    }
}
